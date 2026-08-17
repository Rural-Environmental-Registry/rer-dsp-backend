package br.car.dsp.service;

import br.car.dsp.dto.TerritoryBoundaryBoxResponse;
import br.car.dsp.dto.TerritoryEnvelopeProjection;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
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
	void getOptions_Level2WithUnknownParent_ShouldReturnNotFound() {
		when(level1Repository.existsById("999")).thenReturn(false);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getOptions("level2", "999")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Territory parent not found: 999", exception.getReason());
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
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getOptions("level3", null)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("parentId is required for level3", exception.getReason());
	}

	@Test
	void getOptions_InvalidLevel_ShouldReturnBadRequest() {
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getOptions("level9", null)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("Unsupported territory level: level9", exception.getReason());
	}

	@Test
	void getOptions_ShouldRequireLevel() {
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getOptions(null, null)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("level is required", exception.getReason());
	}

	@Test
	void getOptions_ShouldNormalizeLevelIgnoringCaseAndWhitespace() {
		TerritoryLevel1 unit = new TerritoryLevel1();
		unit.setId("1");
		unit.setName("Centro-Oeste");
		when(level1Repository.findAll()).thenReturn(List.of(unit));

		List<TerritoryOptionResponse> options = territoryService.getOptions("  LEVEL1  ", null);

		assertEquals(1, options.size());
		assertEquals("1", options.getFirst().id());
	}

	@Test
	void getOptions_Level1_ShouldSortOptionsByNameCaseInsensitive() {
		TerritoryLevel1 south = new TerritoryLevel1();
		south.setId("2");
		south.setName("sul");

		TerritoryLevel1 north = new TerritoryLevel1();
		north.setId("1");
		north.setName("Norte");

		when(level1Repository.findAll()).thenReturn(List.of(south, north));

		List<TerritoryOptionResponse> options = territoryService.getOptions("level1", null);

		assertEquals(List.of("Norte", "sul"), options.stream().map(TerritoryOptionResponse::name).toList());
	}

	@Test
	void getOptions_Level2WithBlankParent_ShouldReturnAllUnits() {
		TerritoryLevel2 unit = new TerritoryLevel2();
		unit.setId("DF");
		unit.setName("Distrito Federal");
		when(level2Repository.findAll()).thenReturn(List.of(unit));

		List<TerritoryOptionResponse> options = territoryService.getOptions("level2", "   ");

		assertEquals(1, options.size());
		assertEquals("DF", options.getFirst().id());
		verify(level2Repository).findAll();
	}

	@Test
	void getOptions_Level3WithBlankParent_ShouldFail() {
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getOptions("level3", "   ")
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("parentId is required for level3", exception.getReason());
	}

	@Test
	void getOptions_ShouldReturnInternalServerErrorWhenRepositoryFails() {
		when(level1Repository.findAll()).thenThrow(new RuntimeException("database unavailable"));

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getOptions("level1", null)
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Failed to query territory options", exception.getReason());
	}

	@Test
	void getBoundaryBox_Level3_ShouldReturnEnvelope() {
		lenient().when(level3Repository.findIdsPresent(List.of("5300108"))).thenReturn(List.of("5300108"));
		when(level3Repository.findEnvelopeByIds(List.of("5300108")))
				.thenReturn(envelope(-48.2, -16.0, -47.3, -15.5));

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
		lenient().when(level3Repository.findIdsPresent(List.of("5300108", "5300109")))
				.thenReturn(List.of("5300108", "5300109"));
		when(level3Repository.findEnvelopeByIds(List.of("5300108", "5300109")))
				.thenReturn(envelope(-48.2, -16.0, -46.5, -15.2));

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
		lenient().when(level2Repository.findIdsPresent(List.of("DF"))).thenReturn(List.of("DF"));
		when(level2Repository.findEnvelopeByIds(List.of("DF")))
				.thenReturn(envelope(-48.3, -16.1, -47.2, -15.4));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, List.of("DF"), null);

		assertEquals(-48.3, bbox.minX(), 1e-9);
		assertEquals(-16.1, bbox.minY(), 1e-9);
		assertEquals(-47.2, bbox.maxX(), 1e-9);
		assertEquals(-15.4, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_MultipleLevel2_ShouldReturnUnifiedEnvelope() {
		lenient().when(level2Repository.findIdsPresent(List.of("DF", "GO")))
				.thenReturn(List.of("DF", "GO"));
		when(level2Repository.findEnvelopeByIds(List.of("DF", "GO")))
				.thenReturn(envelope(-50.0, -19.0, -46.0, -13.0));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, List.of("DF", "GO"), List.of());

		assertEquals(-50.0, bbox.minX(), 1e-9);
		assertEquals(-19.0, bbox.minY(), 1e-9);
		assertEquals(-46.0, bbox.maxX(), 1e-9);
		assertEquals(-13.0, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_NoParams_ShouldReturnAllLevel1Envelope() {
		when(level1Repository.findEnvelopeAll()).thenReturn(envelope(-70.0, -34.0, -48.0, 5.0));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, null, null);

		assertEquals(-70.0, bbox.minX(), 1e-9);
		assertEquals(-34.0, bbox.minY(), 1e-9);
		assertEquals(-48.0, bbox.maxX(), 1e-9);
		assertEquals(5.0, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level1Ids_ShouldReturnEnvelope() {
		lenient().when(level1Repository.findIdsPresent(List.of("1"))).thenReturn(List.of("1"));
		when(level1Repository.findEnvelopeByIds(List.of("1")))
				.thenReturn(envelope(-70.0, -5.0, -50.0, 5.0));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(List.of("1"), null, null);

		assertEquals(-70.0, bbox.minX(), 1e-9);
		assertEquals(-5.0, bbox.minY(), 1e-9);
		assertEquals(-50.0, bbox.maxX(), 1e-9);
		assertEquals(5.0, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level1WithoutGeometry_ShouldFallbackToLevel2() {
		when(level1Repository.findEnvelopeAll()).thenReturn(emptyEnvelope());
		when(level2Repository.findEnvelopeAll()).thenReturn(envelope(-48.3, -16.1, -47.2, -15.4));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, null, null);

		assertEquals(-48.3, bbox.minX(), 1e-9);
		assertEquals(-16.1, bbox.minY(), 1e-9);
		assertEquals(-47.2, bbox.maxX(), 1e-9);
		assertEquals(-15.4, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_Level1AndLevel2WithoutGeometry_ShouldFallbackToLevel3() {
		when(level1Repository.findEnvelopeAll()).thenReturn(emptyEnvelope());
		when(level2Repository.findEnvelopeAll()).thenReturn(emptyEnvelope());
		when(level3Repository.findEnvelopeAll()).thenReturn(envelope(-48.2, -16.0, -47.3, -15.5));

		TerritoryBoundaryBoxResponse bbox = territoryService.getBoundaryBox(null, null, null);

		assertEquals(-48.2, bbox.minX(), 1e-9);
		assertEquals(-16.0, bbox.minY(), 1e-9);
		assertEquals(-47.3, bbox.maxX(), 1e-9);
		assertEquals(-15.5, bbox.maxY(), 1e-9);
	}

	@Test
	void getBoundaryBox_NoGeometryAnywhere_ShouldFail() {
		when(level1Repository.findEnvelopeAll()).thenReturn(emptyEnvelope());
		when(level2Repository.findEnvelopeAll()).thenReturn(emptyEnvelope());
		when(level3Repository.findEnvelopeAll()).thenReturn(emptyEnvelope());

		assertThrows(ResponseStatusException.class, () -> territoryService.getBoundaryBox(null, null, null));
	}

	@Test
	void getBoundaryBox_UnknownLevel3_ShouldFail() {
		when(level3Repository.findIdsPresent(anyCollection())).thenReturn(List.of());

		assertThrows(
				ResponseStatusException.class,
				() -> territoryService.getBoundaryBox(null, null, List.of("999"))
		);
	}

	private static TerritoryEnvelopeProjection envelope(double minX, double minY, double maxX, double maxY) {
		return new TerritoryEnvelopeProjection() {
			@Override
			public Double getMinX() {
				return minX;
			}

			@Override
			public Double getMinY() {
				return minY;
			}

			@Override
			public Double getMaxX() {
				return maxX;
			}

			@Override
			public Double getMaxY() {
				return maxY;
			}
		};
	}

	private static TerritoryEnvelopeProjection emptyEnvelope() {
		return new TerritoryEnvelopeProjection() {
			@Override
			public Double getMinX() {
				return null;
			}

			@Override
			public Double getMinY() {
				return null;
			}

			@Override
			public Double getMaxX() {
				return null;
			}

			@Override
			public Double getMaxY() {
				return null;
			}
		};
	}
}
