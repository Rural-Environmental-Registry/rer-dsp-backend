package br.car.dsp.dto;

import java.util.List;

/**
 * Mirrors Consulta Pública Region + states (geoServices/getRegions).
 */
public record RegionResponse(
		Long id,
		String name,
		String code,
		List<StateResponse> states
) {
}
