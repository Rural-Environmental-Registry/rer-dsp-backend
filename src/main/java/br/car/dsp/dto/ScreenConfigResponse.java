package br.car.dsp.dto;

import java.util.List;

/**
 * Configuração de filtros de uma tela.
 * Home: sempre 2 níveis. Downloads: sempre 3 níveis.
 */
public record ScreenConfigResponse(
		String title,
		List<String> hierarchyKeys,
		ScreenFieldConfigResponse identifier,
		ScreenFieldConfigResponse theme,
		String level1SectionTitle,
		String level2SectionTitle,
		String filterByTitle
) {
}
