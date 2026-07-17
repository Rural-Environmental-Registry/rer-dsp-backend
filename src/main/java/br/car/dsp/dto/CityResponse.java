package br.car.dsp.dto;

/**
 * Espelha CityDTO do Consulta Pública.
 */
public record CityResponse(
		Integer id,
		String name,
		String bounderBox
) {
}
