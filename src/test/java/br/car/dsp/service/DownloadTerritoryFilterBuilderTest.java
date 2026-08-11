package br.car.dsp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.car.dsp.config.download.DownloadTerritoryFilterConfig;
import br.car.dsp.config.download.DownloadThemeConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DownloadTerritoryFilterBuilderTest {

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
}
