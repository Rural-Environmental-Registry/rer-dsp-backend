package br.car.dsp.service;

import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.model.TerritoryLevel1;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.TerritoryLevel1Repository;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
