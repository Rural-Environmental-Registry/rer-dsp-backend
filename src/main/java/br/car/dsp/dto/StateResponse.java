package br.car.dsp.dto;

/**
 * Mirrors Consulta Pública StateDTO.
 */
public record StateResponse(
		String id,
		String name,
		String region,
		String bounderBox
) {
}
