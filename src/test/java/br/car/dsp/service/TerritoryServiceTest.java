package br.car.dsp.service;

import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.model.TerritoryLevel1;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.TerritoryLevel1Repository;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerritoryServiceTest {

	@Mock
	private TerritoryLevel1Repository level1Repository;

	@Mock
	private TerritoryLevel2Repository level2Repository;

	@Mock
	private TerritoryLevel3Repository level3Repository;

	@InjectMocks
	private TerritoryService territoryService;

	@Test
	void getOptions_Level1_ShouldReturnUnitsFromDatabase() {
		TerritoryLevel1 unit = new TerritoryLevel1();
		unit.setId("1");
		unit.setLabel("Centro-Oeste");
		when(level1Repository.findAll()).thenReturn(List.of(unit));

		List<TerritoryOptionResponse> options = territoryService.getOptions("level1", null);

		assertEquals(1, options.size());
		assertEquals("1", options.getFirst().id());
		assertEquals("Centro-Oeste", options.getFirst().name());
	}

	@Test
	void getOptions_Level2WithoutParent_ShouldReturnAllUnits() {
		TerritoryLevel2 unit = new TerritoryLevel2();
		unit.setId("DF");
		unit.setLabel("Distrito Federal");
		when(level2Repository.findAll()).thenReturn(List.of(unit));

		List<TerritoryOptionResponse> options = territoryService.getOptions("level2", null);

		assertTrue(options.stream().anyMatch(option -> "DF".equals(option.id())));
	}

	@Test
	void getOptions_Level2WithParent_ShouldReturnChildrenOfParent() {
		TerritoryLevel2 df = new TerritoryLevel2();
		df.setId("DF");
		df.setLabel("Distrito Federal");
		when(level1Repository.existsById("3")).thenReturn(true);
		when(level2Repository.findByParent_Id("3")).thenReturn(List.of(df));

		List<TerritoryOptionResponse> options = territoryService.getOptions("level2", "3");

		assertEquals(1, options.size());
		assertEquals("DF", options.getFirst().id());
	}

	@Test
	void getOptions_Level2WithUnknownParent_ShouldFail() {
		when(level1Repository.existsById("999")).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> territoryService.getOptions("level2", "999"));
	}

	@Test
	void getOptions_Level3_ShouldReturnCities() {
		TerritoryLevel3 city = new TerritoryLevel3();
		city.setId("5300108");
		city.setLabel("Brasília");
		when(level3Repository.findByParent_Id("DF")).thenReturn(List.of(city));

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
