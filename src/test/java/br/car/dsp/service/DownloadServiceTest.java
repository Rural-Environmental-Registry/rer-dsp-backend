package br.car.dsp.service;

import br.car.dsp.config.download.DownloadTerritoryFilterConfig;
import br.car.dsp.config.download.DownloadThemeConfig;
import br.car.dsp.dto.DownloadFormatStatus;
import br.car.dsp.dto.DownloadItemResponse;
import br.car.dsp.dto.DownloadSearchRequest;
import br.car.dsp.dto.DownloadThemeResponse;
import br.car.dsp.support.DownloadFileNameBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

	@Mock
	private DownloadConfigService downloadConfigService;

	@Mock
	private DownloadTerritoryFilterBuilder territoryFilterBuilder;

	@Mock
	private GeoServerWfsClient geoServerWfsClient;

	@Mock
	private DownloadFileNameBuilder downloadFileNameBuilder;

	@InjectMocks
	private DownloadService downloadService;

	private DownloadThemeConfig areaTheme;

	@BeforeEach
	void setUp() {
		areaTheme = new DownloadThemeConfig(
				"area_of_interest",
				"Aeroportos",
				"dsp:area-of-interest",
				List.of("csv"),
				true,
				new DownloadTerritoryFilterConfig("direct", "territory_level_3_id", null)
		);
	}

	@Test
	void getThemes_ShouldReturnEnabledThemesFromConfig() {
		when(downloadConfigService.getEnabledThemes()).thenReturn(List.of(areaTheme));

		List<DownloadThemeResponse> themes = downloadService.getThemes();

		assertEquals(1, themes.size());
		assertEquals("area_of_interest", themes.getFirst().code());
		assertEquals("Aeroportos", themes.getFirst().name());
	}

	@Test
	void search_ShouldRequireLevel2() {
		DownloadSearchRequest request = new DownloadSearchRequest();

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> downloadService.search(request)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	void search_ShouldReturnAvailableFormatsWhenWfsHasFeatures() {
		DownloadSearchRequest request = new DownloadSearchRequest();
		request.setLevel2("DF");

		when(downloadConfigService.getEnabledThemes()).thenReturn(List.of(areaTheme));
		when(downloadConfigService.resolveWfsBaseUrl()).thenReturn("http://localhost:22668/geoserver/dsp/wfs");
		when(territoryFilterBuilder.buildCqlFilter(areaTheme, "DF", null))
				.thenReturn("territory_level_3_id IN ('5300108')");
		when(geoServerWfsClient.countFeatures(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(2L);
		when(geoServerWfsClient.fetchLatestAttributeValue(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(java.util.Optional.of("2024-06-15T10:30:00Z"));

		List<DownloadItemResponse> items = downloadService.search(request);

		assertEquals(1, items.size());
		assertEquals("area_of_interest", items.getFirst().themeCode());
		assertEquals("2024-06-15T10:30:00Z", items.getFirst().lastUpdate());
		assertTrue(items.getFirst().formats().stream().anyMatch(format ->
				"csv".equals(format.format()) && DownloadFormatStatus.AVAILABLE.equals(format.status())));
		verify(territoryFilterBuilder).validateTerritory("DF", null);
	}

	@Test
	void search_ShouldSkipLastUpdateFetchWhenNoFeaturesMatched() {
		DownloadSearchRequest request = new DownloadSearchRequest();
		request.setLevel2("DF");

		when(downloadConfigService.getEnabledThemes()).thenReturn(List.of(areaTheme));
		when(downloadConfigService.resolveWfsBaseUrl()).thenReturn("http://localhost:22668/geoserver/dsp/wfs");
		when(territoryFilterBuilder.buildCqlFilter(areaTheme, "DF", null))
				.thenReturn("territory_level_3_id IN ('5300108')");
		when(geoServerWfsClient.countFeatures(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(0L);

		List<DownloadItemResponse> items = downloadService.search(request);

		assertEquals(1, items.size());
		assertNull(items.getFirst().lastUpdate());
		verify(geoServerWfsClient, never()).fetchLatestAttributeValue(anyString(), anyString(), anyString());
	}

	@Test
	void search_ShouldKeepAvailableWhenLastUpdateFetchFails() {
		DownloadSearchRequest request = new DownloadSearchRequest();
		request.setLevel2("DF");

		when(downloadConfigService.getEnabledThemes()).thenReturn(List.of(areaTheme));
		when(downloadConfigService.resolveWfsBaseUrl()).thenReturn("http://localhost:22668/geoserver/dsp/wfs");
		when(territoryFilterBuilder.buildCqlFilter(areaTheme, "DF", null))
				.thenReturn("territory_level_3_id IN ('5300108')");
		when(geoServerWfsClient.countFeatures(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(1L);
		when(geoServerWfsClient.fetchLatestAttributeValue(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(java.util.Optional.empty());

		List<DownloadItemResponse> items = downloadService.search(request);

		assertEquals(1, items.size());
		assertNull(items.getFirst().lastUpdate());
		assertTrue(items.getFirst().formats().stream().anyMatch(format ->
				DownloadFormatStatus.AVAILABLE.equals(format.status())));
	}

	@Test
	void search_ShouldFilterByTheme() {
		DownloadSearchRequest request = new DownloadSearchRequest();
		request.setLevel2("DF");
		request.setTheme("area_of_interest");

		when(downloadConfigService.findEnabledTheme("area_of_interest")).thenReturn(java.util.Optional.of(areaTheme));
		when(downloadConfigService.resolveWfsBaseUrl()).thenReturn("http://localhost:22668/geoserver/dsp/wfs");
		when(territoryFilterBuilder.buildCqlFilter(areaTheme, "DF", null))
				.thenReturn("territory_level_3_id IN ('5300108')");
		when(geoServerWfsClient.countFeatures(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(0L);

		List<DownloadItemResponse> items = downloadService.search(request);

		assertEquals(1, items.size());
		assertTrue(items.getFirst().formats().stream().allMatch(format ->
				DownloadFormatStatus.UNAVAILABLE.equals(format.status())));
	}

	@Test
	void downloadFile_ShouldReturnCsvBytesWhenAvailable() {
		when(downloadConfigService.findEnabledTheme("area_of_interest")).thenReturn(java.util.Optional.of(areaTheme));
		when(downloadConfigService.resolveWfsBaseUrl()).thenReturn("http://localhost:22668/geoserver/dsp/wfs");
		when(territoryFilterBuilder.buildCqlFilter(areaTheme, "DF", null))
				.thenReturn("territory_level_3_id IN ('5300108')");
		when(geoServerWfsClient.countFeatures(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(1L);
		when(geoServerWfsClient.downloadCsv(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn("id,name\n1,test\n".getBytes());
		when(downloadFileNameBuilder.build("DF", null, "Aeroportos", "csv"))
				.thenReturn("aeroportos_df-distrito-federal.csv");

		ResponseEntity<byte[]> response = downloadService.downloadFile("DF", null, "area_of_interest", "csv");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().length > 0);
		assertTrue(response.getHeaders().getFirst("Content-Disposition")
				.contains("aeroportos_df-distrito-federal.csv"));
	}

	@Test
	void downloadFile_ShouldReturnNotFoundWhenUnavailable() {
		when(downloadConfigService.findEnabledTheme("area_of_interest")).thenReturn(java.util.Optional.of(areaTheme));
		when(downloadConfigService.resolveWfsBaseUrl()).thenReturn("http://localhost:22668/geoserver/dsp/wfs");
		when(territoryFilterBuilder.buildCqlFilter(areaTheme, "DF", null))
				.thenReturn("territory_level_3_id IN ('')");
		when(geoServerWfsClient.countFeatures(anyString(), eq("dsp:area-of-interest"), anyString()))
				.thenReturn(0L);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> downloadService.downloadFile("DF", null, "area_of_interest", "csv")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}
}
