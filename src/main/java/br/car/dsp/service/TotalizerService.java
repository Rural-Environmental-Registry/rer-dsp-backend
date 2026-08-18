package br.car.dsp.service;

import br.car.dsp.dto.AreaOfInterestAggregate;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.CentroidWgs84Projection;
import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.HomeKpisConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.dto.KpiCardConfigResponse;
import br.car.dsp.dto.TerritoryLevelRefResponse;
import br.car.dsp.dto.TerritoryLevelsResponse;
import br.car.dsp.dto.ThemeTotalsAggregate;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import br.car.dsp.model.AreaOfInterest;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.AreaOfInterestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TotalizerService {

	public static final String CODE_AREA_OF_INTEREST = "AREA_OF_INTEREST";
	public static final String CODE_THEME_1 = "THEME_1";
	public static final String CODE_THEME_2 = "THEME_2";
	public static final String CODE_THEME_3 = "THEME_3";
	public static final String CODE_THEME_4 = "THEME_4";

	private static final int DEFAULT_MAX_CARDS = 5;

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	private final AreaOfInterestRepository areaOfInterestRepository;
	private final InstallationConfigService installationConfigService;

	@Transactional(readOnly = true)
	public List<TotalizerResponse> getTotalizers(TotalizerFilterRequest filter) {
		List<String> level2Ids = filter != null ? filter.getLevel2Ids() : List.of();
		List<String> level3Ids = filter != null ? filter.getLevel3Ids() : List.of();

		InstallationConfigResponse config = installationConfigService.getInstallationConfig();
		List<KpiCardConfigResponse> cards = resolveCards(config);
		AreaOfInterestAggregate aggregate = resolveAggregate(level2Ids, level3Ids);
		ThemeTotalsAggregate themes = needsThemeTotals(cards)
				? resolveThemeTotals(level2Ids, level3Ids)
				: ThemeTotalsAggregate.empty();

		List<TotalizerResponse> totalizers = new ArrayList<>(cards.size());
		for (KpiCardConfigResponse card : cards) {
			if (card == null || card.code() == null || card.code().isBlank()) {
				continue;
			}
			totalizers.add(toTotalizer(card, aggregate, themes));
		}
		return totalizers;
	}

	@Transactional(readOnly = true)
	public DetailByIdentifierResponse getDetailByIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Identifier not found");
		}

		AreaOfInterest areaOfInterest = areaOfInterestRepository.findById(identifier.trim())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Identifier not found"
				));

		Centroid centroid = resolveCentroid(areaOfInterestRepository.findCentroidWgs84(areaOfInterest.getId()));
		return toDetailResponse(areaOfInterest, centroid, List.of());
	}

	@Transactional(readOnly = true)
	public DetailByIdentifierResponse getDetailsByCoordinates(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Identifier not found");
		}

		List<String> matchingIds = areaOfInterestRepository.findIdsContainingPoint(latitude, longitude);
		if (matchingIds == null || matchingIds.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Identifier not found");
		}

		List<String> shuffledIds = new ArrayList<>(matchingIds);
		Collections.shuffle(shuffledIds);

		String selectedId = shuffledIds.getFirst();
		List<String> otherIds = shuffledIds.stream().skip(1).toList();

		AreaOfInterest areaOfInterest = areaOfInterestRepository.findById(selectedId)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Identifier not found"
				));

		Centroid centroid = resolveCentroid(areaOfInterestRepository.findCentroidWgs84(areaOfInterest.getId()));
		return toDetailResponse(areaOfInterest, centroid, otherIds);
	}

	private static DetailByIdentifierResponse toDetailResponse(
			AreaOfInterest areaOfInterest,
			Centroid centroid,
			List<String> otherIds
	) {
		TerritoryLevel3 level3 = areaOfInterest.getTerritoryLevel3();
		TerritoryLevel2 level2 = level3 != null ? level3.getParent() : null;

		return new DetailByIdentifierResponse(
				areaOfInterest.getId(),
				centroid.latitude(),
				centroid.longitude(),
				new TerritoryLevelsResponse(
						toLevelRef(level2 != null ? level2.getId() : null,
								level2 != null ? level2.getName() : null),
						toLevelRef(level3 != null ? level3.getId() : null,
								level3 != null ? level3.getName() : null)
				),
				formatDate(areaOfInterest.getRegistrationDate()),
				formatDate(areaOfInterest.getAlterationDate()),
				areaOfInterest.getArea(),
				otherIds != null ? otherIds : List.of()
		);
	}

	private static boolean needsThemeTotals(List<KpiCardConfigResponse> cards) {
		return cards.stream().anyMatch(card -> {
			String code = card.code() == null ? "" : card.code().trim();
			return CODE_THEME_1.equals(code)
					|| CODE_THEME_2.equals(code)
					|| CODE_THEME_3.equals(code)
					|| CODE_THEME_4.equals(code);
		});
	}

	private List<KpiCardConfigResponse> resolveCards(InstallationConfigResponse config) {
		HomeKpisConfigResponse kpis = config != null ? config.kpis() : null;
		if (kpis == null || kpis.cards() == null || kpis.cards().isEmpty()) {
			return List.of(fallbackPrimaryCard(config));
		}

		int maxCards = kpis.maxCards() > 0 ? Math.min(kpis.maxCards(), DEFAULT_MAX_CARDS) : DEFAULT_MAX_CARDS;
		return kpis.cards().stream()
				.filter(card -> card != null && card.code() != null && !card.code().isBlank())
				.sorted(Comparator.comparingInt(card -> card.order() > 0 ? card.order() : Integer.MAX_VALUE))
				.limit(maxCards)
				.toList();
	}

	private static KpiCardConfigResponse fallbackPrimaryCard(InstallationConfigResponse config) {
		AreaOfInterestMeasuresConfigResponse measures = config != null && config.areaOfInterest() != null
				? config.areaOfInterest()
				: AreaOfInterestMeasuresConfigResponse.defaults();
		return new KpiCardConfigResponse(
				CODE_AREA_OF_INTEREST,
				CODE_AREA_OF_INTEREST,
				measures.areaUnitLabel(),
				measures.areaUnit(),
				null,
				1,
				true
		);
	}

	private TotalizerResponse toTotalizer(
			KpiCardConfigResponse card,
			AreaOfInterestAggregate aggregate,
			ThemeTotalsAggregate themes
	) {
		String code = card.code().trim();
		if (CODE_AREA_OF_INTEREST.equals(code)) {
			return toAreaOfInterestTotalizer(aggregate, card);
		}
		BigDecimal themeSum = themeSumForCode(code, themes);
		long value = themeSum.setScale(0, RoundingMode.HALF_UP).longValue();
		return new TotalizerResponse(
				card.label(),
				code,
				(double) value,
				null,
				null,
				card.unitOfMeasurement()
		);
	}

	private static TotalizerResponse toAreaOfInterestTotalizer(
			AreaOfInterestAggregate aggregate,
			KpiCardConfigResponse card
	) {
		long count = aggregate != null && aggregate.getCount() != null
				? aggregate.getCount()
				: 0L;
		BigDecimal totalArea = aggregate != null && aggregate.getTotalArea() != null
				? aggregate.getTotalArea()
				: BigDecimal.ZERO;
		long areaSum = totalArea.setScale(0, RoundingMode.HALF_UP).longValue();
		return new TotalizerResponse(
				card.label(),
				CODE_AREA_OF_INTEREST,
				(double) count,
				card.optionalLabel(),
				areaSum,
				card.unitOfMeasurement()
		);
	}

	private static BigDecimal themeSumForCode(String code, ThemeTotalsAggregate themes) {
		if (themes == null) {
			return BigDecimal.ZERO;
		}
		return switch (code) {
			case CODE_THEME_1 -> nullToZero(themes.getTheme1());
			case CODE_THEME_2 -> nullToZero(themes.getTheme2());
			case CODE_THEME_3 -> nullToZero(themes.getTheme3());
			case CODE_THEME_4 -> nullToZero(themes.getTheme4());
			default -> BigDecimal.ZERO;
		};
	}

	private static BigDecimal nullToZero(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	private AreaOfInterestAggregate resolveAggregate(List<String> level2Ids, List<String> level3Ids) {
		List<String> normalizedLevel3Ids = normalizeIds(level3Ids);
		if (!normalizedLevel3Ids.isEmpty()) {
			return areaOfInterestRepository.aggregateByLevel3Ids(normalizedLevel3Ids);
		}
		List<String> normalizedLevel2Ids = normalizeIds(level2Ids);
		if (!normalizedLevel2Ids.isEmpty()) {
			return areaOfInterestRepository.aggregateByLevel2Ids(normalizedLevel2Ids);
		}
		return areaOfInterestRepository.aggregateAll();
	}

	private ThemeTotalsAggregate resolveThemeTotals(List<String> level2Ids, List<String> level3Ids) {
		List<String> normalizedLevel3Ids = normalizeIds(level3Ids);
		if (!normalizedLevel3Ids.isEmpty()) {
			return areaOfInterestRepository.sumThemesByLevel3Ids(normalizedLevel3Ids);
		}
		List<String> normalizedLevel2Ids = normalizeIds(level2Ids);
		if (!normalizedLevel2Ids.isEmpty()) {
			return areaOfInterestRepository.sumThemesByLevel2Ids(normalizedLevel2Ids);
		}
		return areaOfInterestRepository.sumThemesAll();
	}

	private static List<String> normalizeIds(List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return ids.stream()
				.filter(id -> id != null && !id.isBlank())
				.map(String::trim)
				.toList();
	}

	private static TerritoryLevelRefResponse toLevelRef(String id, String name) {
		if (id == null && name == null) {
			return null;
		}
		return new TerritoryLevelRefResponse(id, name);
	}

	private static String formatDate(LocalDateTime value) {
		if (value == null) {
			return null;
		}
		return value.toLocalDate().format(ISO_DATE);
	}

	private static String formatDate(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.toLocalDate().format(ISO_DATE);
	}

	private static Centroid resolveCentroid(Optional<CentroidWgs84Projection> projection) {
		if (projection.isEmpty()) {
			return Centroid.empty();
		}
		CentroidWgs84Projection centroid = projection.get();
		if (centroid.getLatitude() == null || centroid.getLongitude() == null) {
			return Centroid.empty();
		}
		return new Centroid(
				formatCoordinate(centroid.getLatitude()),
				formatCoordinate(centroid.getLongitude())
		);
	}

	private static String formatCoordinate(double value) {
		return BigDecimal.valueOf(value)
				.setScale(6, RoundingMode.HALF_UP)
				.stripTrailingZeros()
				.toPlainString();
	}

	private record Centroid(String latitude, String longitude) {
		static Centroid empty() {
			return new Centroid(null, null);
		}
	}
}
