package br.car.dsp.dto;

/**
 * Mirrors Consulta Pública CityDTO.
 */
public record CityResponse(
		Integer id,
		String name,
		String bounderBox
) {
}
