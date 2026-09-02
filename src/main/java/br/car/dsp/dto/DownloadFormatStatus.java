package br.car.dsp.dto;

/**
 * Availability of a file format for a theme.
 */
public record DownloadFormatStatus(
		String format,
		String status
) {
	public static final String AVAILABLE = "available";
	public static final String UNAVAILABLE = "unavailable";
}
