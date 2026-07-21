package br.car.dsp.dto;

import java.util.List;

/**
 * Configuração do painel de KPIs da Home.
 * Máximo de 5 cards; o primeiro deve ser o primaryCode (imóveis cadastrados).
 */
public record HomeKpisConfigResponse(
		int maxCards,
		String primaryCode,
		List<KpiCardConfigResponse> cards
) {
}
