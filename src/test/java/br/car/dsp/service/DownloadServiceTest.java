package br.car.dsp.service;

import br.car.dsp.dto.DownloadFormatStatus;
import br.car.dsp.dto.DownloadItemResponse;
import br.car.dsp.dto.DownloadSearchRequest;
import br.car.dsp.dto.DownloadThemeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DownloadServiceTest {

	private final DownloadService downloadService = new DownloadService();

	@Test
	void getThemes_ShouldReturnOnlyEnabledThemes() {
		List<DownloadThemeResponse> themes = downloadService.getThemes();

		assertFalse(themes.isEmpty());
		assertTrue(themes.stream().allMatch(DownloadThemeResponse::enabled));
		assertTrue(themes.stream().noneMatch(theme -> "theme_delta".equals(theme.code())));
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
	void search_ShouldReturnFormatsWithStatuses() {
		DownloadSearchRequest request = new DownloadSearchRequest();
		request.setLevel2("DF");

		List<DownloadItemResponse> items = downloadService.search(request);

		assertFalse(items.isEmpty());
		DownloadItemResponse alpha = items.stream()
				.filter(item -> "theme_alpha".equals(item.themeCode()))
				.findFirst()
				.orElseThrow();
		assertEquals("Tema Alpha", alpha.themeName());
		assertTrue(alpha.formats().stream().anyMatch(format ->
				"csv".equals(format.format()) && DownloadFormatStatus.AVAILABLE.equals(format.status())));
		assertTrue(alpha.formats().stream().noneMatch(format -> "gpkg".equalsIgnoreCase(format.format())));
		assertNotNull(alpha.lastUpdate());
	}

	@Test
	void search_ShouldFilterByTheme() {
		DownloadSearchRequest request = new DownloadSearchRequest();
		request.setLevel2("DF");
		request.setTheme("theme_gamma");

		List<DownloadItemResponse> items = downloadService.search(request);

		assertEquals(1, items.size());
		assertEquals("theme_gamma", items.getFirst().themeCode());
		assertTrue(items.getFirst().formats().stream().allMatch(format ->
				DownloadFormatStatus.COMING_SOON.equals(format.status())));
	}

	@Test
	void downloadFile_ShouldReturnMockBytesWhenAvailable() {
		ResponseEntity<byte[]> response = downloadService.downloadFile("DF", null, "theme_alpha", "csv");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().length > 0);
		assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("DF_theme_alpha.csv"));
	}

	@Test
	void downloadFile_ShouldReturnNotFoundWhenUnavailable() {
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> downloadService.downloadFile("DF", null, "theme_gamma", "csv")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	void getThemes_ShouldNotExposeGpkgFormat() {
		List<DownloadThemeResponse> themes = downloadService.getThemes();

		assertTrue(themes.stream().noneMatch(theme ->
				theme.formats().stream().anyMatch(format -> "gpkg".equalsIgnoreCase(format))));
	}
}
