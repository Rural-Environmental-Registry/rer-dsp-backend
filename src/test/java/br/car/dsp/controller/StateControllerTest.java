package br.car.dsp.controller;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.StateResponse;
import br.car.dsp.service.StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateControllerTest {

	@Mock
	private StateService stateService;

	@InjectMocks
	private StateController stateController;

	private List<StateResponse> states;
	private List<CityResponse> cities;

	@BeforeEach
	void setUp() {
		states = List.of(new StateResponse("DF", "Distrito Federal", "CW", null));
		cities = List.of(new CityResponse(5300108, "Brasília", null));
	}

	@Test
	void getAll_ShouldDelegateToService() {
		// Given
		when(stateService.getAllUf()).thenReturn(states);

		// When
		List<StateResponse> result = stateController.getAll();

		// Then
		assertEquals(states, result);
		verify(stateService).getAllUf();
	}

	@Test
	void getCitiesByUf_ShouldDelegateToService() {
		// Given
		when(stateService.getCitiesByUfId("DF")).thenReturn(cities);

		// When
		List<CityResponse> result = stateController.getCitiesByUf("DF");

		// Then
		assertEquals(cities, result);
		verify(stateService).getCitiesByUfId("DF");
	}

	@Test
	void getUfsByRegion_ShouldDelegateToService() {
		// Given
		when(stateService.getUfsByRegion("CW")).thenReturn(states);

		// When
		List<StateResponse> result = stateController.getUfsByRegion("CW");

		// Then
		assertEquals(states, result);
		verify(stateService).getUfsByRegion("CW");
	}
}
