package br.car.dsp.dto;

import java.util.List;

/**
 * Espelha Region + states do Consulta Pública (geoServices/getRegions).
 */
public record RegionResponse(
		Long id,
		String name,
		String code,
		List<StateResponse> states
) {
}
