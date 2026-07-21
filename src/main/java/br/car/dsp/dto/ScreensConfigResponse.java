package br.car.dsp.dto;

/**
 * Telas que consomem a hierarquia territorial.
 */
public record ScreensConfigResponse(
		ScreenConfigResponse home,
		ScreenConfigResponse downloads
) {
}
