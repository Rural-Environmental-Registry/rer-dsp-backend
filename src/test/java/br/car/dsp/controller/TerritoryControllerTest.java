package br.car.dsp.controller;

import br.car.dsp.dto.TerritoryBoundaryBoxResponse;
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

	@Test
	void getBoundaryBox_ShouldDelegateToService() {
		TerritoryBoundaryBoxResponse expected = new TerritoryBoundaryBoxResponse(-48.2, -16.0, -47.3, -15.5);
		List<String> level1Ids = List.of();
		List<String> level2Ids = List.of("DF");
		List<String> level3Ids = List.of("5300108", "5300109");
		when(territoryService.getBoundaryBox(level1Ids, level2Ids, level3Ids)).thenReturn(expected);

		TerritoryBoundaryBoxResponse result = territoryController.getBoundaryBox(level1Ids, level2Ids, level3Ids);

		assertEquals(expected, result);
		verify(territoryService).getBoundaryBox(level1Ids, level2Ids, level3Ids);
	}
}
