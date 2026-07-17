package br.car.dsp.dto;

/**
 * Espelha StateDTO do Consulta Pública.
 */
public record StateResponse(
		String id,
		String name,
		String region,
		String bounderBox
) {
}
