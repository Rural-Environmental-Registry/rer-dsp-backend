package br.car.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Screen filter configuration shared by Home and Downloads.
 * Home adds {@link HomeScreenConfigResponse#detail()}; Downloads has no detail panel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
