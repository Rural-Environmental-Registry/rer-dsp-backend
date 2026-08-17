package br.car.dsp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.car.dsp.config.download.DownloadTerritoryFilterConfig;
import br.car.dsp.config.download.DownloadThemeConfig;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.AreaOfInterestRepository;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DownloadTerritoryFilterBuilderTest {

	@Mock
	private TerritoryLevel2Repository level2Repository;

	@Mock
	private TerritoryLevel3Repository level3Repository;

	@Mock
	private AreaOfInterestRepository areaOfInterestRepository;

	@InjectMocks
	private DownloadTerritoryFilterBuilder filterBuilder;

	private DownloadThemeConfig areaTheme;
	private DownloadThemeConfig linkedTheme;

	@BeforeEach
	void setUp() {
		areaTheme = new DownloadThemeConfig(
				"area_of_interest",
				"Demo properties",
				"dsp:area-of-interest",
				List.of("csv"),
				true,
				new DownloadTerritoryFilterConfig("direct", "territory_level_3_id", null)
		);
		linkedTheme = new DownloadThemeConfig(
				"generic_layer",
				"Generic layer",
				"dsp:generic-layer",
				List.of("csv"),
				true,
				new DownloadTerritoryFilterConfig("aoi_linked", null, "area_of_interest_id")
		);
	}

	@Test
	void buildAoiScopedCqlFilter_ShouldUsePrimaryKeyForDirectStrategy() {
		assertEquals(
				"id = 'DEMO-001'",
				filterBuilder.buildAoiScopedCqlFilter(areaTheme, "DEMO-001")
		);
	}

	@Test
	void buildAoiScopedCqlFilter_ShouldEscapeSingleQuotesInAoiId() {
		assertEquals(
				"id = 'AOI-''001'",
				filterBuilder.buildAoiScopedCqlFilter(areaTheme, "AOI-'001")
		);
	}

	@Test
	void buildAoiScopedCqlFilter_ShouldUseLinkFieldForAoiLinkedStrategy() {
		assertEquals(
				"area_of_interest_id = 'DEMO-001'",
				filterBuilder.buildAoiScopedCqlFilter(linkedTheme, "DEMO-001")
		);
	}

	@Test
	void buildAoiScopedCqlFilter_ShouldRequireAoiId() {
		assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.buildAoiScopedCqlFilter(areaTheme, " ")
		);
	}

	@Test
	void validateTerritory_ShouldRequireLevel2() {
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.validateTerritory(null, null)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("Level 2 is required to search downloads", exception.getReason());
	}

	@Test
	void validateTerritory_ShouldFailWhenLevel2DoesNotExist() {
		when(level2Repository.existsById("DF")).thenReturn(false);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.validateTerritory("DF", null)
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Territory level 2 not found", exception.getReason());
	}

	@Test
	void validateTerritory_ShouldAcceptLevel2WithoutLevel3() {
		when(level2Repository.existsById("DF")).thenReturn(true);

		assertDoesNotThrow(() -> filterBuilder.validateTerritory("DF", null));
		assertDoesNotThrow(() -> filterBuilder.validateTerritory("  DF  ", "   "));
	}

	@Test
	void validateTerritory_ShouldFailWhenLevel3DoesNotExist() {
		when(level2Repository.existsById("DF")).thenReturn(true);
		when(level3Repository.existsById("5300108")).thenReturn(false);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.validateTerritory("DF", "5300108")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Territory level 3 not found", exception.getReason());
	}

	@Test
	void validateTerritory_ShouldFailWhenLevel3DoesNotBelongToLevel2() {
		when(level2Repository.existsById("DF")).thenReturn(true);
		when(level3Repository.existsById("3200607")).thenReturn(true);

		TerritoryLevel3 otherUnit = new TerritoryLevel3();
		otherUnit.setId("5300108");
		when(level3Repository.findByParent_Id("DF")).thenReturn(List.of(otherUnit));

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.validateTerritory("DF", "3200607")
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("Level 3 does not belong to the selected level 2", exception.getReason());
	}

	@Test
	void validateTerritory_ShouldAcceptValidLevel2AndLevel3Hierarchy() {
		when(level2Repository.existsById("DF")).thenReturn(true);
		when(level3Repository.existsById("5300108")).thenReturn(true);

		TerritoryLevel3 brasilia = new TerritoryLevel3();
		brasilia.setId("5300108");
		when(level3Repository.findByParent_Id("DF")).thenReturn(List.of(brasilia));

		assertDoesNotThrow(() -> filterBuilder.validateTerritory("DF", "5300108"));
		assertDoesNotThrow(() -> filterBuilder.validateTerritory("  DF  ", "  5300108  "));
	}

	@Test
	void buildCqlFilter_WithDirectStrategy_ShouldFilterByLevel3WhenProvided() {
		assertEquals(
				"territory_level_3_id = '5300108'",
				filterBuilder.buildCqlFilter(areaTheme, "DF", "5300108")
		);
	}

	@Test
	void buildCqlFilter_WithDirectStrategy_ShouldEscapeSingleQuotesInLevel3() {
		assertEquals(
				"territory_level_3_id = 'ID-''001'",
				filterBuilder.buildCqlFilter(areaTheme, "DF", "ID-'001")
		);
	}

	@Test
	void buildCqlFilter_WithDirectStrategy_ShouldUseInClauseWhenLevel3IsOmitted() {
		TerritoryLevel3 brasilia = new TerritoryLevel3();
		brasilia.setId("5300108");
		TerritoryLevel3 gama = new TerritoryLevel3();
		gama.setId("5300109");
		when(level3Repository.findByParent_Id("DF")).thenReturn(List.of(brasilia, gama));

		assertEquals(
				"territory_level_3_id IN ('5300108', '5300109')",
				filterBuilder.buildCqlFilter(areaTheme, "DF", null)
		);
	}

	@Test
	void buildCqlFilter_WithDirectStrategy_ShouldReturnImpossibleFilterWhenNoLevel3ChildrenExist() {
		when(level3Repository.findByParent_Id("DF")).thenReturn(List.of());

		assertEquals("1=0", filterBuilder.buildCqlFilter(areaTheme, "DF", null));
	}

	@Test
	void buildCqlFilter_WithAoiLinkedStrategy_ShouldFilterByAoiIdsFromLevel3() {
		when(areaOfInterestRepository.findIdsByTerritoryLevel3Id("5300108"))
				.thenReturn(List.of("AOI-001", "AOI-002"));

		assertEquals(
				"area_of_interest_id IN ('AOI-001', 'AOI-002')",
				filterBuilder.buildCqlFilter(linkedTheme, "DF", "5300108")
		);
		verify(areaOfInterestRepository).findIdsByTerritoryLevel3Id("5300108");
	}

	@Test
	void buildCqlFilter_WithAoiLinkedStrategy_ShouldFilterByAoiIdsFromLevel2WhenLevel3IsOmitted() {
		when(areaOfInterestRepository.findIdsByTerritoryLevel2Id("DF"))
				.thenReturn(List.of("AOI-010"));

		assertEquals(
				"area_of_interest_id IN ('AOI-010')",
				filterBuilder.buildCqlFilter(linkedTheme, "DF", null)
		);
		verify(areaOfInterestRepository).findIdsByTerritoryLevel2Id("DF");
	}

	@Test
	void buildCqlFilter_WithAoiLinkedStrategy_ShouldReturnImpossibleFilterWhenNoAoiIdsExist() {
		when(areaOfInterestRepository.findIdsByTerritoryLevel2Id("DF")).thenReturn(List.of());

		assertEquals("1=0", filterBuilder.buildCqlFilter(linkedTheme, "DF", null));
	}

	@Test
	void buildCqlFilter_WithAoiLinkedStrategy_ShouldEscapeSingleQuotesInAoiIds() {
		when(areaOfInterestRepository.findIdsByTerritoryLevel3Id("5300108"))
				.thenReturn(List.of("AOI-'001"));

		assertEquals(
				"area_of_interest_id IN ('AOI-''001')",
				filterBuilder.buildCqlFilter(linkedTheme, "DF", "5300108")
		);
	}

	@Test
	void buildCqlFilter_ShouldFailWhenTerritoryFilterIsMissing() {
		DownloadThemeConfig themeWithoutFilter = new DownloadThemeConfig(
				"broken_theme",
				"Broken theme",
				"dsp:broken-layer",
				List.of("csv"),
				true,
				null
		);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.buildCqlFilter(themeWithoutFilter, "DF", "5300108")
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals(
				"Territory configuration missing for theme broken_theme",
				exception.getReason()
		);
	}

	@Test
	void buildCqlFilter_WithDirectStrategy_ShouldFailWhenLevel3FieldIsMissing() {
		DownloadThemeConfig themeWithoutField = new DownloadThemeConfig(
				"broken_direct",
				"Broken direct",
				"dsp:broken-direct",
				List.of("csv"),
				true,
				new DownloadTerritoryFilterConfig("direct", null, null)
		);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.buildCqlFilter(themeWithoutField, "DF", "5300108")
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Required territory field missing: level3Field", exception.getReason());
	}

	@Test
	void buildCqlFilter_WithAoiLinkedStrategy_ShouldFailWhenAoiLinkFieldIsMissing() {
		DownloadThemeConfig themeWithoutField = new DownloadThemeConfig(
				"broken_linked",
				"Broken linked",
				"dsp:broken-linked",
				List.of("csv"),
				true,
				new DownloadTerritoryFilterConfig("aoi_linked", null, null)
		);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.buildCqlFilter(themeWithoutField, "DF", "5300108")
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Required territory field missing: aoiLinkField", exception.getReason());
	}

	@Test
	void buildCqlFilter_ShouldFailWhenStrategyIsUnsupported() {
		DownloadThemeConfig themeWithUnknownStrategy = new DownloadThemeConfig(
				"broken_strategy",
				"Broken strategy",
				"dsp:broken-strategy",
				List.of("csv"),
				true,
				new DownloadTerritoryFilterConfig("spatial_join", "territory_level_3_id", null)
		);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> filterBuilder.buildCqlFilter(themeWithUnknownStrategy, "DF", "5300108")
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Unsupported territory strategy: spatial_join", exception.getReason());
	}
}
