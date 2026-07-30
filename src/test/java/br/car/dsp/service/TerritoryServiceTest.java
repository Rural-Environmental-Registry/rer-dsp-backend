package br.car.dsp.service;

import br.car.dsp.dto.TerritoryBoundaryBoxResponse;
import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.model.TerritoryLevel1;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.TerritoryLevel1Repository;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
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
		unit.setName("Centro-Oeste");
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
		unit.setName("Distrito Federal");
		when(level2Repository.findAll()).thenReturn(List.of(unit));

		List<TerritoryOptionResponse> options = territoryService.getOptions("level2", null);

		assertTrue(options.stream().anyMatch(option -> "DF".equals(option.id())));
	}

	@Test
	void getOptions_Level2WithParent_ShouldReturnChildrenOfParent() {
		TerritoryLevel2 df = new TerritoryLevel2();
		df.setId("DF");
		df.setName("Distrito Federal");
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
		city.setName("Brasília");
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

	@Test
	void getBoundaryBox_Level3_ShouldReturnEnvelope() {
		TerritoryLevel3 city = new TerritoryLevel3();
		city.setId("5300108");
		city.setName("Brasília");
		city.setBoundaryBox(rectangle(-48.2, -16.0, -47.3, -15.5));
		when(level3Repository.findAllById(List.of("5300108"))).thenReturn(List.of(city));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(
				null,
				List.of("DF"),
				List.of("5300108")
		);

		assertEquals(-48.2, bbox.minX(), 1e-9);
		assertEquals(-16.0, bbox.minY(), 1e-9);
		assertEquals(-47.3, bbox.maxX(), 1e-9);
		assertEquals(-15.5, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_MultipleLevel3_ShouldReturnUnifiedEnvelope() {
		TerritoryLevel3 a = new TerritoryLevel3();
		a.setId("5300108");
		a.setName("Brasília");
		a.setBoundaryBox(rectangle(-48.2, -16.0, -47.3, -15.5));
		TerritoryLevel3 b = new TerritoryLevel3();
		b.setId("5300109");
		b.setName("Other");
		b.setBoundaryBox(rectangle(-47.0, -15.8, -46.5, -15.2));
		when(level3Repository.findAllById(List.of("5300108", "5300109"))).thenReturn(List.of(a, b));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(
				null,
				List.of("DF"),
				List.of("5300108", "5300109")
		);

		assertEquals(-48.2, bbox.minX(), 1e-9);
		assertEquals(-16.0, bbox.minY(), 1e-9);
		assertEquals(-46.5, bbox.maxX(), 1e-9);
		assertEquals(-15.2, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level2_ShouldReturnEnvelope() {
		TerritoryLevel2 df = new TerritoryLevel2();
		df.setId("DF");
		df.setName("Distrito Federal");
		df.setBoundaryBox(rectangle(-48.3, -16.1, -47.2, -15.4));
		when(level2Repository.findAllById(List.of("DF"))).thenReturn(List.of(df));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, List.of("DF"), null);

		assertEquals(-48.3, bbox.minX(), 1e-9);
		assertEquals(-16.1, bbox.minY(), 1e-9);
		assertEquals(-47.2, bbox.maxX(), 1e-9);
		assertEquals(-15.4, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_MultipleLevel2_ShouldReturnUnifiedEnvelope() {
		TerritoryLevel2 df = new TerritoryLevel2();
		df.setId("DF");
		df.setName("Distrito Federal");
		df.setBoundaryBox(rectangle(-48.3, -16.1, -47.2, -15.4));
		TerritoryLevel2 go = new TerritoryLevel2();
		go.setId("GO");
		go.setName("Goiás");
		go.setBoundaryBox(rectangle(-50.0, -19.0, -46.0, -13.0));
		when(level2Repository.findAllById(List.of("DF", "GO"))).thenReturn(List.of(df, go));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, List.of("DF", "GO"), List.of());

		assertEquals(-50.0, bbox.minX(), 1e-9);
		assertEquals(-19.0, bbox.minY(), 1e-9);
		assertEquals(-46.0, bbox.maxX(), 1e-9);
		assertEquals(-13.0, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_NoParams_ShouldReturnAllLevel1Envelope() {
		TerritoryLevel1 a = new TerritoryLevel1();
		a.setId("1");
		a.setName("Norte");
		a.setBoundaryBox(rectangle(-70.0, -5.0, -50.0, 5.0));
		TerritoryLevel1 b = new TerritoryLevel1();
		b.setId("2");
		b.setName("Sul");
		b.setBoundaryBox(rectangle(-55.0, -34.0, -48.0, -22.0));
		when(level1Repository.findAll()).thenReturn(List.of(a, b));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, null, null);

		assertEquals(-70.0, bbox.minX(), 1e-9);
		assertEquals(-34.0, bbox.minY(), 1e-9);
		assertEquals(-48.0, bbox.maxX(), 1e-9);
		assertEquals(5.0, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level1Ids_ShouldReturnEnvelope() {
		TerritoryLevel1 a = new TerritoryLevel1();
		a.setId("1");
		a.setName("Norte");
		a.setBoundaryBox(rectangle(-70.0, -5.0, -50.0, 5.0));
		when(level1Repository.findAllById(List.of("1"))).thenReturn(List.of(a));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(List.of("1"), null, null);

		assertEquals(-70.0, bbox.minX(), 1e-9);
		assertEquals(-5.0, bbox.minY(), 1e-9);
		assertEquals(-50.0, bbox.maxX(), 1e-9);
		assertEquals(5.0, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level1WithoutGeometry_ShouldFallbackToLevel2() {
		TerritoryLevel1 a = new TerritoryLevel1();
		a.setId("1");
		a.setName("Norte");
		when(level1Repository.findAll()).thenReturn(List.of(a));

		TerritoryLevel2 df = new TerritoryLevel2();
		df.setId("DF");
		df.setName("Distrito Federal");
		df.setBoundaryBox(rectangle(-48.3, -16.1, -47.2, -15.4));
		when(level2Repository.findAll()).thenReturn(List.of(df));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, null, null);

		assertEquals(-48.3, bbox.minX(), 1e-9);
		assertEquals(-16.1, bbox.minY(), 1e-9);
		assertEquals(-47.2, bbox.maxX(), 1e-9);
		assertEquals(-15.4, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level1AndLevel2WithoutGeometry_ShouldFallbackToLevel3() {
		when(level1Repository.findAll()).thenReturn(List.of());
		when(level2Repository.findAll()).thenReturn(List.of());

		TerritoryLevel3 city = new TerritoryLevel3();
		city.setId("5300108");
		city.setName("Brasília");
		city.setBoundaryBox(rectangle(-48.2, -16.0, -47.3, -15.5));
		when(level3Repository.findAll()).thenReturn(List.of(city));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, null, null);

		assertEquals(-48.2, bbox.minX(), 1e-9);
		assertEquals(-16.0, bbox.minY(), 1e-9);
		assertEquals(-47.3, bbox.maxX(), 1e-9);
		assertEquals(-15.5, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_NoGeometryAnywhere_ShouldFail() {
		when(level1Repository.findAll()).thenReturn(List.of());
		when(level2Repository.findAll()).thenReturn(List.of());
		when(level3Repository.findAll()).thenReturn(List.of());

		assertThrows(ResponseStatusException.class, () -> territoryService.getBoundaryBox(null, null, null));
	}

	@Test
	void getBoundaryBox_UnknownLevel3_ShouldFail() {
		when(level3Repository.findAllById(List.of("999"))).thenReturn(List.of());

		assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getBoundaryBox(null, null, List.of("999"))
		);
	}

	private static Polygon rectangle(double minX, double minY, double maxX, double maxY) {
		GeometryFactory factory = new GeometryFactory();
		return factory.createPolygon(new Coordinate[]{
				new Coordinate(minX, minY),
				new Coordinate(maxX, minY),
				new Coordinate(maxX, maxY),
				new Coordinate(minX, maxY),
				new Coordinate(minX, minY)
		});
	}
}
