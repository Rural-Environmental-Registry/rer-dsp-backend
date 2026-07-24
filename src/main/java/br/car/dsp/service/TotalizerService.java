package br.car.dsp.service;

import br.car.dsp.dto.DetailByIdentifierResponse;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TotalizerService {

	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	private final AreaOfInterestRepository areaOfInterestRepository;

	public List<TotalizerResponse> getTotalizers(TotalizerFilterRequest filter) {
		String idState = filter != null ? filter.getIdState() : null;
		List<Integer> idsCities = filter != null ? filter.getIdsCities() : List.of();
		return LocationMockData.buildTotalizers(idState, idsCities);
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

		Centroid centroid = resolveCentroid(areaOfInterest.getGeometry());

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

	private static Centroid resolveCentroid(Geometry geometry) {
		if (geometry == null || geometry.isEmpty()) {
			return Centroid.empty();
		}
		Point point = geometry.getCentroid();
		if (point == null || point.isEmpty()) {
			return Centroid.empty();
		}
		return new Centroid(formatCoordinate(point.getY()), formatCoordinate(point.getX()));
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
