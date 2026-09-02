package br.car.dsp.mock;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.RegionResponse;
import br.car.dsp.dto.StateResponse;
import br.car.dsp.dto.TotalizerResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationMockDataTest {

	@Test
	void getAllStates_ShouldReturnNonEmptyList() {
		List<StateResponse> states = LocationMockData.getAllStates();

		assertFalse(states.isEmpty());
		assertTrue(states.stream().anyMatch(state -> "DF".equals(state.id())));
	}

	@Test
	void getCitiesByState_WhenDf_ShouldReturnBrasilia() {
		List<CityResponse> cities = LocationMockData.getCitiesByState("DF");

		assertFalse(cities.isEmpty());
		assertEquals("Brasília", cities.getFirst().name());
	}

	@Test
	void getCitiesByState_WhenUnknown_ShouldReturnEmptyList() {
		assertTrue(LocationMockData.getCitiesByState("XX").isEmpty());
	}

	@Test
	void getStatesByRegionCode_WhenCw_ShouldReturnCentroOesteStates() {
		List<StateResponse> states = LocationMockData.getStatesByRegionCode("CW");

		assertFalse(states.isEmpty());
		assertTrue(states.stream().allMatch(state -> "CW".equals(state.region())));
	}

	@Test
	void getRegions_ShouldIncludeNestedStates() {
		List<RegionResponse> regions = LocationMockData.getRegions();

		assertEquals(5, regions.size());
		assertFalse(regions.getFirst().states().isEmpty());
	}

	@Test
	void buildTotalizers_WhenNoFilter_ShouldReturnFiveItems() {
		List<TotalizerResponse> totalizers = LocationMockData.buildTotalizers(null, List.of());

		assertEquals(5, totalizers.size());
		assertEquals("AREA_OF_INTEREST", totalizers.getFirst().code());
	}

	@Test
	void buildTotalizers_WhenStateFilter_ShouldReduceValues() {
		List<TotalizerResponse> national = LocationMockData.buildTotalizers(null, List.of());
		List<TotalizerResponse> byState = LocationMockData.buildTotalizers("DF", List.of());

		assertTrue(byState.getFirst().value() < national.getFirst().value());
	}
}
