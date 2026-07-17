package br.car.dsp.controller;

import br.car.dsp.dto.RegionResponse;
import br.car.dsp.service.GeoServicesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoServicesControllerTest {

	@Mock
	private GeoServicesService geoServicesService;

	@InjectMocks
	private GeoServicesController geoServicesController;

	@Test
	void getRegions_ShouldDelegateToService() {
		// Given
		List<RegionResponse> regions = List.of(
				new RegionResponse(1L, "Norte", "N", List.of())
		);
		when(geoServicesService.getRegions()).thenReturn(regions);

		// When
		List<RegionResponse> result = geoServicesController.getRegions();

		// Then
		assertEquals(regions, result);
		verify(geoServicesService).getRegions();
	}
}
