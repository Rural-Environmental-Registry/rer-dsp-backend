package br.car.dsp.mock;

import br.car.dsp.dto.DownloadFormatStatus;
import br.car.dsp.dto.DownloadItemResponse;
import br.car.dsp.dto.DownloadThemeResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Mock theme catalog and simple file-availability rule.
 * Depois: config externa / admin; entrega via S3 ou GeoServer.
 */
public final class DownloadThemesMockData {

	private static final String LAST_UPDATE = "2026-06-01";

	private static final List<DownloadThemeResponse> THEMES = List.of(
			new DownloadThemeResponse("theme_alpha", "Tema Alpha", List.of("csv"), true),
			new DownloadThemeResponse("theme_beta", "Tema Beta", List.of("csv"), true),
			new DownloadThemeResponse("theme_gamma", "Tema Gamma", List.of("csv"), true),
			new DownloadThemeResponse("theme_delta", "Tema Delta", List.of("csv"), false)
	);

	private DownloadThemesMockData() {
	}

	public static List<DownloadThemeResponse> listEnabledThemes() {
		return THEMES.stream().filter(DownloadThemeResponse::enabled).toList();
	}

	public static Optional<DownloadThemeResponse> findEnabledByCode(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		String normalized = code.trim().toLowerCase(Locale.ROOT);
		return listEnabledThemes().stream()
				.filter(theme -> theme.code().equalsIgnoreCase(normalized))
				.findFirst();
	}

	public static List<DownloadItemResponse> search(String level2, String level3, String themeCode) {
		List<DownloadThemeResponse> themes = listEnabledThemes();
		if (themeCode != null && !themeCode.isBlank()) {
			themes = findEnabledByCode(themeCode).map(List::of).orElse(List.of());
		}

		List<DownloadItemResponse> items = new ArrayList<>();
		for (DownloadThemeResponse theme : themes) {
			List<DownloadFormatStatus> formats = theme.formats().stream()
					.map(format -> new DownloadFormatStatus(format, resolveStatus(theme.code(), format, level2, level3)))
					.toList();
			boolean anyAvailable = formats.stream()
					.anyMatch(item -> DownloadFormatStatus.AVAILABLE.equals(item.status()));
			items.add(new DownloadItemResponse(
					theme.code(),
					theme.name(),
					formats,
					anyAvailable ? LAST_UPDATE : null
			));
		}
		return items;
	}

	/**
	 * Mock rules (replaceable by real file checks):
	 * - theme_gamma: sempre coming_soon
	 * - other catalog formats: available when level 2 is present
	 */
	public static String resolveStatus(String themeCode, String format, String level2, String level3) {
		if (level2 == null || level2.isBlank()) {
			return DownloadFormatStatus.UNAVAILABLE;
		}
		if ("theme_gamma".equalsIgnoreCase(themeCode)) {
			return DownloadFormatStatus.COMING_SOON;
		}
		return DownloadFormatStatus.AVAILABLE;
	}

	public static byte[] buildMockFileContent(String level2, String level3, String theme, String format) {
		String cityPart = (level3 == null || level3.isBlank()) ? "" : level3;
		String body = """
				# DSP mock file (no S3/GeoServer)
				level2=%s
				level3=%s
				theme=%s
				format=%s
				""".formatted(level2, cityPart, theme, format);
		return body.getBytes(StandardCharsets.UTF_8);
	}

	public static String buildFileName(String level2, String level3, String theme, String format) {
		String normalizedTheme = theme == null ? "theme" : theme.toLowerCase(Locale.ROOT);
		String normalizedFormat = format == null ? "bin" : format.toLowerCase(Locale.ROOT);
		if (level3 != null && !level3.isBlank()) {
			return "%s_%s_%s.%s".formatted(level2, level3, normalizedTheme, normalizedFormat);
		}
		return "%s_%s.%s".formatted(level2, normalizedTheme, normalizedFormat);
	}
}
