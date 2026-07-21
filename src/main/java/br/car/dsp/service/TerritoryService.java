package br.car.dsp.service;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.RegionResponse;
import br.car.dsp.dto.StateResponse;
import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.mock.LocationMockData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Expõe opções territoriais por nível genérico (level1/2/3).
 * Internamente ainda usa o mock Brasil (região/UF/município).
 */
@Service
public class TerritoryService {

	public List<TerritoryOptionResponse> getOptions(String level, String parentId) {
		String normalized = normalizeLevel(level);
		return switch (normalized) {
			case "level1" -> getLevel1Options();
			case "level2" -> getLevel2Options(parentId);
			case "level3" -> getLevel3Options(parentId);
			default -> throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Unsupported territory level: " + level
			);
		};
	}

	private List<TerritoryOptionResponse> getLevel1Options() {
		return LocationMockData.getRegions().stream()
				.sorted(Comparator.comparing(RegionResponse::name, String.CASE_INSENSITIVE_ORDER))
				.map(region -> new TerritoryOptionResponse(String.valueOf(region.id()), region.name()))
				.toList();
	}

	private List<TerritoryOptionResponse> getLevel2Options(String parentId) {
		if (parentId == null || parentId.isBlank()) {
			return LocationMockData.getAllStates().stream()
					.sorted(Comparator.comparing(StateResponse::name, String.CASE_INSENSITIVE_ORDER))
					.map(state -> new TerritoryOptionResponse(state.id(), formatStateLabel(state)))
					.toList();
		}

		RegionResponse region = findRegion(parentId);
		List<StateResponse> states = region.states() != null && !region.states().isEmpty()
				? region.states()
				: LocationMockData.getStatesByRegionCode(region.code());

		return states.stream()
				.sorted(Comparator.comparing(StateResponse::name, String.CASE_INSENSITIVE_ORDER))
				.map(state -> new TerritoryOptionResponse(state.id(), formatStateLabel(state)))
				.toList();
	}

	private List<TerritoryOptionResponse> getLevel3Options(String parentId) {
		if (parentId == null || parentId.isBlank()) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"parentId is required for level3"
			);
		}

		return LocationMockData.getCitiesByState(parentId).stream()
				.sorted(Comparator.comparing(CityResponse::name, String.CASE_INSENSITIVE_ORDER))
				.map(city -> new TerritoryOptionResponse(String.valueOf(city.id()), city.name()))
				.toList();
	}

	private RegionResponse findRegion(String parentId) {
		return LocationMockData.getRegions().stream()
				.filter(region ->
						String.valueOf(region.id()).equals(parentId)
								|| region.code().equalsIgnoreCase(parentId))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Territory parent not found: " + parentId
				));
	}

	private static String formatStateLabel(StateResponse state) {
		return state.id() + " - " + state.name();
	}

	private static String normalizeLevel(String level) {
		if (level == null || level.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "level is required");
		}
		return level.trim().toLowerCase(Locale.ROOT);
	}
}
