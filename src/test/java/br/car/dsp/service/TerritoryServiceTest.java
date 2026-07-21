package br.car.dsp.service;

import br.car.dsp.dto.TerritoryOptionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerritoryServiceTest {

	private final TerritoryService territoryService = new TerritoryService();

	@Test
	void getOptions_Level1_ShouldReturnRegions() {
		List<TerritoryOptionResponse> options = territoryService.getOptions("level1", null);

		assertFalse(options.isEmpty());
		assertTrue(options.stream().anyMatch(option -> "Centro-Oeste".equals(option.name())));
	}

	@Test
	void getOptions_Level2WithoutParent_ShouldReturnAllStates() {
		List<TerritoryOptionResponse> options = territoryService.getOptions("level2", null);

		assertTrue(options.stream().anyMatch(option -> "DF".equals(option.id())));
	}

	@Test
	void getOptions_Level2WithParent_ShouldReturnStatesOfRegion() {
		List<TerritoryOptionResponse> options = territoryService.getOptions("level2", "3");

		assertTrue(options.stream().anyMatch(option -> "DF".equals(option.id())));
		assertFalse(options.stream().anyMatch(option -> "SP".equals(option.id())));
	}

	@Test
	void getOptions_Level3_ShouldReturnCities() {
		List<TerritoryOptionResponse> options = territoryService.getOptions("level3", "DF");

		assertTrue(options.stream().anyMatch(option -> "Brasília".equals(option.name())));
	}

	@Test
	void getOptions_Level3WithoutParent_ShouldFail() {
		assertThrows(ResponseStatusException.class, () -> territoryService.getOptions("level3", null));
	}

	@Test
	void getOptions_InvalidLevel_ShouldFail() {
		assertThrows(ResponseStatusException.class, () -> territoryService.getOptions("level9", null));
	}
}
