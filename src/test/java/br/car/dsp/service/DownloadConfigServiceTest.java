package br.car.dsp.service;

import br.car.dsp.config.DownloadConfigProperties;
import br.car.dsp.config.download.DownloadThemeConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadConfigServiceTest {

	@Test
	void getEnabledThemes_ShouldReturnOnlyEnabledThemesFromClasspathFixture() {
		DownloadConfigService service = serviceWithTestFixture();

		List<DownloadThemeConfig> themes = service.getEnabledThemes();

		assertEquals(2, themes.size());
		assertTrue(themes.stream().allMatch(DownloadThemeConfig::enabled));
		assertEquals("area_of_interest", themes.get(0).code());
		assertEquals("Area of interest", themes.get(0).name());
		assertEquals("dsp:area-of-interest", themes.get(0).typeName());
		assertEquals("generic_layer", themes.get(1).code());
		assertTrue(themes.stream().noneMatch(theme -> "disabled_theme".equals(theme.code())));
	}

	@Test
	void findEnabledTheme_ShouldFindThemeIgnoringCaseAndWhitespace() {
		DownloadConfigService service = serviceWithTestFixture();

		Optional<DownloadThemeConfig> byExactCode = service.findEnabledTheme("area_of_interest");
		Optional<DownloadThemeConfig> byUpperCase = service.findEnabledTheme("AREA_OF_INTEREST");
		Optional<DownloadThemeConfig> withWhitespace = service.findEnabledTheme("  generic_layer  ");

		assertTrue(byExactCode.isPresent());
		assertEquals("area_of_interest", byExactCode.get().code());
		assertTrue(byUpperCase.isPresent());
		assertEquals("area_of_interest", byUpperCase.get().code());
		assertTrue(withWhitespace.isPresent());
		assertEquals("generic_layer", withWhitespace.get().code());
	}

	@Test
	void findEnabledTheme_ShouldReturnEmptyForBlankUnknownOrDisabledCodes() {
		DownloadConfigService service = serviceWithTestFixture();

		assertTrue(service.findEnabledTheme(null).isEmpty());
		assertTrue(service.findEnabledTheme("").isEmpty());
		assertTrue(service.findEnabledTheme("   ").isEmpty());
		assertTrue(service.findEnabledTheme("unknown_theme").isEmpty());
		assertTrue(service.findEnabledTheme("disabled_theme").isEmpty());
	}

	@Test
	void resolveWfsBaseUrl_ShouldPreferConfiguredPropertyOverJson() {
		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile("classpath:downloadThemesConfig-test.json");
		properties.setGeoserverWfsBaseUrl("  http://from-env.example/geoserver/dsp/wfs/  ");

		DownloadConfigService service = new DownloadConfigService(properties, new ObjectMapper());

		assertEquals("http://from-env.example/geoserver/dsp/wfs", service.resolveWfsBaseUrl());
	}

	@Test
	void resolveWfsBaseUrl_ShouldFallbackToJsonWhenPropertyIsBlank() {
		DownloadConfigService service = serviceWithTestFixture();

		assertEquals("http://localhost:22669/geoserver/dsp/wfs", service.resolveWfsBaseUrl());
	}

	@Test
	void resolveWfsBaseUrl_ShouldRemoveTrailingSlashFromJsonUrl() throws Exception {
		Path temp = Files.createTempFile("download-themes-wfs", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "wfsBaseUrl": "http://localhost:22669/geoserver/dsp/wfs/",
				  "themes": [
				    {
				      "code": "area_of_interest",
				      "name": "Area of interest",
				      "typeName": "dsp:area-of-interest",
				      "formats": ["csv"],
				      "enabled": true,
				      "territoryFilter": {
				        "strategy": "direct",
				        "level3Field": "territory_level_3_id"
				      }
				    }
				  ]
				}
				"""
		);

		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile(temp.toAbsolutePath().toString());

		DownloadConfigService service = new DownloadConfigService(properties, new ObjectMapper());

		assertEquals("http://localhost:22669/geoserver/dsp/wfs", service.resolveWfsBaseUrl());
	}

	@Test
	void resolveWfsBaseUrl_ShouldFailWhenNeitherPropertyNorJsonProvideUrl() {
		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile("classpath:downloadThemesConfig-without-wfs-url.json");

		DownloadConfigService service = new DownloadConfigService(properties, new ObjectMapper());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				service::resolveWfsBaseUrl
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("WFS base URL is not configured", exception.getReason());
	}

	@Test
	void getEnabledThemes_ShouldFailWhenClasspathThemesFileIsMissing() {
		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile("classpath:does-not-exist-download-themes.json");

		DownloadConfigService service = new DownloadConfigService(properties, new ObjectMapper());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				service::getEnabledThemes
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Failed to load download themes config", exception.getReason());
		assertTrue(exception.getCause().getMessage().contains("does-not-exist-download-themes.json"));
	}

	@Test
	void getEnabledThemes_ShouldFailWhenFilesystemThemesFileIsMissing() {
		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile("/tmp/does-not-exist-download-themes.json");

		DownloadConfigService service = new DownloadConfigService(properties, new ObjectMapper());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				service::getEnabledThemes
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Failed to load download themes config", exception.getReason());
		assertTrue(exception.getCause().getMessage().contains("does-not-exist-download-themes.json"));
	}

	@Test
	void getEnabledThemes_ShouldFailWhenThemesListIsEmpty() {
		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile("classpath:downloadThemesConfig-empty-themes.json");

		DownloadConfigService service = new DownloadConfigService(properties, new ObjectMapper());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				service::getEnabledThemes
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Failed to load download themes config", exception.getReason());
		assertEquals("Download themes config has no themes", exception.getCause().getMessage());
	}

	private static DownloadConfigService serviceWithTestFixture() {
		DownloadConfigProperties properties = new DownloadConfigProperties();
		properties.setThemesFile("classpath:downloadThemesConfig-test.json");
		return new DownloadConfigService(properties, new ObjectMapper());
	}
}
