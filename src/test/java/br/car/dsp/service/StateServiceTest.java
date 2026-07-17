package br.car.dsp.service;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.StateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateServiceTest {

	private StateService stateService;

	@BeforeEach
	void setUp() {
		stateService = new StateService();
	}

	@Test
	void getAllUf_ShouldReturnStates() {
		// When
		List<StateResponse> result = stateService.getAllUf();

		// Then
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void getCitiesByUfId_WhenDf_ShouldReturnCities() {
		// When
		List<CityResponse> result = stateService.getCitiesByUfId("DF");

		// Then
		assertFalse(result.isEmpty());
		assertEquals("Brasília", result.getFirst().name());
	}

	@Test
	void getUfsByRegion_WhenSe_ShouldReturnSoutheastStates() {
		// When
		List<StateResponse> result = stateService.getUfsByRegion("SE");

		// Then
		assertFalse(result.isEmpty());
		assertTrue(result.stream().allMatch(state -> "SE".equals(state.region())));
	}
}
