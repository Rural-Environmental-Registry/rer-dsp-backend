package br.car.dsp.dto;

import java.util.List;

/**
 * Tema configurável para a tela de Downloads.
 */
public record DownloadThemeResponse(
		String code,
		String name,
		List<String> formats,
		boolean enabled
) {
}
