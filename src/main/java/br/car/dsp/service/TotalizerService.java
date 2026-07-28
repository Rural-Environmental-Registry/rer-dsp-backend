package br.car.dsp.service;

import br.car.dsp.dto.AreaOfInterestAggregate;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.HomeKpisConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.dto.KpiCardConfigResponse;
import br.car.dsp.dto.TerritoryLevelRefResponse;
import br.car.dsp.dto.TerritoryLevelsResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import br.car.dsp.mock.LocationMockData;
import br.car.dsp.model.AreaOfInterest;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.AreaOfInterestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TotalizerService {

	public static final String CODE_AREA_OF_INTEREST = "AREA_OF_INTEREST";

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	private final AreaOfInterestRepository areaOfInterestRepository;
	private final InstallationConfigService installationConfigService;

	@Transactional(readOnly = true)
	public List<TotalizerResponse> getTotalizers(TotalizerFilterRequest filter) {
		String level2Id = filter != null ? filter.getLevel2Id() : null;
		List<String> level3Ids = filter != null ? filter.getLevel3Ids() : List.of();

		List<TotalizerResponse> totalizers = new ArrayList<>(LocationMockData.buildTotalizers(level2Id, level3Ids));
		AreaOfInterestAggregate aggregate = resolveAggregate(level2Id, level3Ids);
		TotalizerResponse areaOfInterest = toAreaOfInterestTotalizer(
				aggregate,
				installationConfigService.getInstallationConfig()
		);

		for (int i = 0; i < totalizers.size(); i++) {
			if (CODE_AREA_OF_INTEREST.equals(totalizers.get(i).code())) {
				totalizers.set(i, areaOfInterest);
				return totalizers;
			}
		}
		totalizers.addFirst(areaOfInterest);
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

		TerritoryLevel3 level3 = areaOfInterest.getTerritoryLevel3();
		TerritoryLevel2 level2 = level3 != null ? level3.getParent() : null;

		Centroid centroid = resolveCentroid(areaOfInterest.getCentroidCoordinates());

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
				areaOfInterest.getArea()
		);
	}

	private AreaOfInterestAggregate resolveAggregate(String level2Id, List<String> level3Ids) {
		List<String> ids = normalizeLevel3Ids(level3Ids);
		if (!ids.isEmpty()) {
			return areaOfInterestRepository.aggregateByLevel3Ids(ids);
		}
		if (level2Id != null && !level2Id.isBlank()) {
			return areaOfInterestRepository.aggregateByLevel2Id(level2Id.trim());
		}
		return areaOfInterestRepository.aggregateAll();
	}

	private static List<String> normalizeLevel3Ids(List<String> level3Ids) {
		if (level3Ids == null || level3Ids.isEmpty()) {
			return List.of();
		}
		return level3Ids.stream()
				.filter(id -> id != null && !id.isBlank())
				.map(String::trim)
				.toList();
	}

	private static TotalizerResponse toAreaOfInterestTotalizer(
			AreaOfInterestAggregate aggregate,
			InstallationConfigResponse config
	) {
		long count = aggregate != null && aggregate.getCount() != null
				? aggregate.getCount()
				: 0L;
		BigDecimal totalArea = aggregate != null && aggregate.getTotalArea() != null
				? aggregate.getTotalArea()
				: BigDecimal.ZERO;
		long areaSum = totalArea.setScale(0, RoundingMode.HALF_UP).longValue();
		KpiCardConfigResponse card = resolveAreaOfInterestCard(config);
		return new TotalizerResponse(
				card.label(),
				CODE_AREA_OF_INTEREST,
				(double) count,
				card.optionalLabel(),
				areaSum,
				card.unitOfMeasurement()
		);
	}

	private static KpiCardConfigResponse resolveAreaOfInterestCard(InstallationConfigResponse config) {
		HomeKpisConfigResponse kpis = config != null ? config.kpis() : null;
		if (kpis != null && kpis.cards() != null) {
			for (KpiCardConfigResponse card : kpis.cards()) {
				if (card != null && CODE_AREA_OF_INTEREST.equals(card.code())) {
					return card;
				}
			}
			String primaryCode = kpis.primaryCode();
			if (primaryCode != null && !primaryCode.isBlank()) {
				for (KpiCardConfigResponse card : kpis.cards()) {
					if (card != null && primaryCode.equals(card.code())) {
						return card;
					}
				}
			}
		}

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

	private static Centroid resolveCentroid(Point centroidCoordinates) {
		if (centroidCoordinates == null || centroidCoordinates.isEmpty()) {
			return Centroid.empty();
		}
		return new Centroid(
				formatCoordinate(centroidCoordinates.getY()),
				formatCoordinate(centroidCoordinates.getX())
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
