package br.car.dsp.service;

import br.car.dsp.dto.RegionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoServicesServiceTest {

	private GeoServicesService geoServicesService;

	@BeforeEach
	void setUp() {
		geoServicesService = new GeoServicesService();
	}

	@Test
	void getRegions_ShouldReturnFiveRegionsWithStates() {
		// When
		List<RegionResponse> result = geoServicesService.getRegions();

		// Then
		assertEquals(5, result.size());
		assertNotNull(result.getFirst().name());
		assertFalse(result.getFirst().states().isEmpty());
	}
}
