package br.car.dsp.dto;

/**
 * Campo auxiliar de tela (identificador, tema, etc.).
 */
public record ScreenFieldConfigResponse(
		String key,
		String label,
		String placeholder
) {
}
