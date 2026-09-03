package br.car.dsp.dto;

import java.util.List;

/**
 * Configurable theme for the Downloads screen.
 */
public record DownloadThemeResponse(
		String code,
		String name,
		List<String> formats,
		boolean enabled
) {
}
