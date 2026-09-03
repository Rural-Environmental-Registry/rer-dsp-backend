package br.car.dsp.controller;

import br.car.dsp.service.MapConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapControllerTest {

	@Mock
	private MapConfigService mapConfigService;

	@InjectMocks
	private MapController mapController;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void getBaseMaps_ShouldDelegateToService() {
		JsonNode expected = objectMapper.createObjectNode().put("ok", true);
		when(mapConfigService.getBaseMaps()).thenReturn(expected);

		JsonNode result = mapController.getBaseMaps();

		assertEquals(expected, result);
		verify(mapConfigService).getBaseMaps();
	}

	@Test
	void getLayers_ShouldDelegateToService() {
		JsonNode expected = objectMapper.createObjectNode().put("ok", true);
		when(mapConfigService.getLayers()).thenReturn(expected);

		JsonNode result = mapController.getLayers();

		assertEquals(expected, result);
		verify(mapConfigService).getLayers();
	}
}
