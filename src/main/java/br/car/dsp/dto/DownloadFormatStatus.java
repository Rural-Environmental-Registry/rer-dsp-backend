package br.car.dsp.dto;

/**
 * Disponibilidade de um formato de arquivo para um tema.
 */
public record DownloadFormatStatus(
		String format,
		String status
) {
	public static final String AVAILABLE = "available";
	public static final String UNAVAILABLE = "unavailable";
	public static final String COMING_SOON = "coming_soon";
}
