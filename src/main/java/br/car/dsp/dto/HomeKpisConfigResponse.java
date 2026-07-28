package br.car.dsp.dto;

import java.util.List;

/**
 * Home KPI panel configuration.
 * Up to 5 cards; the first must be the primaryCode (registered properties).
 */
public record HomeKpisConfigResponse(
		int maxCards,
		String primaryCode,
		List<KpiCardConfigResponse> cards
) {
}
