package br.car.dsp.service;

import br.car.dsp.dto.TerritoryBoundaryBoxResponse;
import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.model.TerritoryLevel1;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.TerritoryLevel1Repository;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/**
 * Exposes territorial options by generic level (level1/2/3) from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TerritoryService {

	private final TerritoryLevel1Repository level1Repository;
	private final TerritoryLevel2Repository level2Repository;
	private final TerritoryLevel3Repository level3Repository;

	public List<TerritoryOptionResponse> getOptions(String level, String parentId) {
		String normalized = normalizeLevel(level);
		try {
			return switch (normalized) {
				case "level1" -> getLevel1Options();
				case "level2" -> getLevel2Options(parentId);
				case "level3" -> getLevel3Options(parentId);
				default -> throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Unsupported territory level: " + level
				);
			};
		} catch (ResponseStatusException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error(
					"Failed to query territory level={} parentId={}",
					normalized,
					parentId,
					ex
			);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to query territory options",
					ex
			);
		}
	}

	public TerritoryBoundaryBoxResponse getBoundaryBox(
			List<String> level1Ids,
			List<String> level2Ids,
			List<String> level3Ids
	) {
		List<String> normalizedLevel3Ids = normalizeIds(level3Ids);
		if (!normalizedLevel3Ids.isEmpty()) {
			Envelope envelope = unionLevel3(normalizedLevel3Ids, true);
			if (envelope != null) {
				return toResponse(envelope);
			}
			return fallbackFromLevel2();
		}

		List<String> normalizedLevel2Ids = normalizeIds(level2Ids);
		if (!normalizedLevel2Ids.isEmpty()) {
			Envelope envelope = unionLevel2(normalizedLevel2Ids, true);
			if (envelope != null) {
				return toResponse(envelope);
			}
			return fallbackFromLevel2();
		}

		List<String> normalizedLevel1Ids = normalizeIds(level1Ids);
		if (!normalizedLevel1Ids.isEmpty()) {
			Envelope envelope = unionLevel1(normalizedLevel1Ids, true);
			if (envelope != null) {
				return toResponse(envelope);
			}
			return fallbackFromLevel2();
		}

		// Sem params: todos os L1, com fallback L2 → L3.
		Envelope envelope = unionLevel1(List.of(), false);
		if (envelope != null) {
			return toResponse(envelope);
		}
		return fallbackFromLevel2();
	}

	private TerritoryBoundaryBoxResponse fallbackFromLevel2() {
		Envelope level2 = unionLevel2(List.of(), false);
		if (level2 != null) {
			return toResponse(level2);
		}
		Envelope level3 = unionLevel3(List.of(), false);
		if (level3 != null) {
			return toResponse(level3);
		}
		throw new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Territory boundary_box not found for level1/level2/level3"
		);
	}

	private Envelope unionLevel1(List<String> ids, boolean requireAllFound) {
		List<TerritoryLevel1> units = ids.isEmpty()
				? level1Repository.findAll()
				: level1Repository.findAllById(ids);
		if (requireAllFound && !ids.isEmpty()) {
			ensureAllFound(ids, units.stream().map(TerritoryLevel1::getId).toList(), "level1");
		}
		return unionPolygons(units, TerritoryLevel1::getBoundaryBox);
	}

	private Envelope unionLevel2(List<String> ids, boolean requireAllFound) {
		List<TerritoryLevel2> units = ids.isEmpty()
				? level2Repository.findAll()
				: level2Repository.findAllById(ids);
		if (requireAllFound && !ids.isEmpty()) {
			ensureAllFound(ids, units.stream().map(TerritoryLevel2::getId).toList(), "level2");
		}
		return unionPolygons(units, TerritoryLevel2::getBoundaryBox);
	}

	private Envelope unionLevel3(List<String> ids, boolean requireAllFound) {
		List<TerritoryLevel3> units = ids.isEmpty()
				? level3Repository.findAll()
				: level3Repository.findAllById(ids);
		if (requireAllFound && !ids.isEmpty()) {
			ensureAllFound(ids, units.stream().map(TerritoryLevel3::getId).toList(), "level3");
		}
		return unionPolygons(units, TerritoryLevel3::getBoundaryBox);
	}

	private static <T> Envelope unionPolygons(List<T> units, Function<T, Polygon> boundaryGetter) {
		Envelope union = null;
		for (T unit : units) {
			union = expandUnion(union, boundaryGetter.apply(unit));
		}
		return union;
	}

	private static void ensureAllFound(List<String> requestedIds, List<String> foundIds, String level) {
		Set<String> found = new HashSet<>(foundIds);
		for (String id : requestedIds) {
			if (!found.contains(id)) {
				throw new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Territory " + level + " not found: " + id
				);
			}
		}
	}

	private static Envelope expandUnion(Envelope union, Polygon boundaryBox) {
		if (boundaryBox == null || boundaryBox.isEmpty()) {
			return union;
		}
		Envelope envelope = boundaryBox.getEnvelopeInternal();
		if (union == null) {
			return new Envelope(envelope);
		}
		union.expandToInclude(envelope);
		return union;
	}

	private static TerritoryBoundaryBoxResponse toResponse(Envelope envelope) {
		return new TerritoryBoundaryBoxResponse(
				envelope.getMinX(),
				envelope.getMinY(),
				envelope.getMaxX(),
				envelope.getMaxY()
		);
	}

	private static List<String> normalizeIds(List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		List<String> normalized = new ArrayList<>();
		for (String id : ids) {
			if (id != null && !id.isBlank()) {
				normalized.add(id.trim());
			}
		}
		return normalized;
	}

	private List<TerritoryOptionResponse> getLevel1Options() {
		return level1Repository.findAll().stream()
				.sorted(Comparator.comparing(TerritoryLevel1::getName, String.CASE_INSENSITIVE_ORDER))
				.map(unit -> new TerritoryOptionResponse(unit.getId(), unit.getName()))
				.toList();
	}

	private List<TerritoryOptionResponse> getLevel2Options(String parentId) {
		List<TerritoryLevel2> units;
		if (parentId == null || parentId.isBlank()) {
			units = level2Repository.findAll();
		} else {
			if (!level1Repository.existsById(parentId)) {
				throw new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Territory parent not found: " + parentId
				);
			}
			units = level2Repository.findByParent_Id(parentId);
		}

		return units.stream()
				.sorted(Comparator.comparing(TerritoryLevel2::getName, String.CASE_INSENSITIVE_ORDER))
				.map(unit -> new TerritoryOptionResponse(unit.getId(), unit.getName()))
				.toList();
	}

	private List<TerritoryOptionResponse> getLevel3Options(String parentId) {
		if (parentId == null || parentId.isBlank()) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"parentId is required for level3"
			);
		}

		return level3Repository.findByParent_Id(parentId).stream()
				.sorted(Comparator.comparing(TerritoryLevel3::getName, String.CASE_INSENSITIVE_ORDER))
				.map(unit -> new TerritoryOptionResponse(unit.getId(), unit.getName()))
				.toList();
	}

	private static String normalizeLevel(String level) {
		if (level == null || level.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "level is required");
		}
		return level.trim().toLowerCase(Locale.ROOT);
	}
}
