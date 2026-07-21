package br.car.dsp.controller;

import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.service.TerritoryService;
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
class TerritoryControllerTest {

	@Mock
	private TerritoryService territoryService;

	@InjectMocks
	private TerritoryController territoryController;

	@Test
	void getOptions_ShouldDelegateToService() {
		List<TerritoryOptionResponse> expected = List.of(new TerritoryOptionResponse("DF", "DF - Distrito Federal"));
		when(territoryService.getOptions("level2", null)).thenReturn(expected);

		List<TerritoryOptionResponse> result = territoryController.getOptions("level2", null);

		assertEquals(expected, result);
		verify(territoryService).getOptions("level2", null);
	}
}
