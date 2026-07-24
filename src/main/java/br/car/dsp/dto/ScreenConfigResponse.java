package br.car.dsp.dto;

import java.util.List;

/**
 * Screen filter configuration.
 * Home: always 2 levels. Downloads: always 3 levels.
 */
public record ScreenConfigResponse(
		String title,
		List<String> hierarchyKeys,
		ScreenFieldConfigResponse identifier,
		ScreenFieldConfigResponse theme,
		String level1SectionTitle,
		String level2SectionTitle,
		String filterByTitle,
		HomeDetailSearchConfigResponse detail
) {

	public ScreenConfigResponse {
		if (detail == null) {
			detail = HomeDetailSearchConfigResponse.defaults();
		}
	}
}
