package br.car.dsp.dto;

import java.util.List;

/**
 * Linha da tabela de Downloads (tema + formatos + última atualização).
 */
public record DownloadItemResponse(
		String themeCode,
		String themeName,
		List<DownloadFormatStatus> formats,
		String lastUpdate
) {
}
