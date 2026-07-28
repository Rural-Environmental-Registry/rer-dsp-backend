package br.car.dsp.dto;

/**
 * Screens that consume the territorial hierarchy.
 */
public record ScreensConfigResponse(
		ScreenConfigResponse home,
		ScreenConfigResponse downloads
) {
}
