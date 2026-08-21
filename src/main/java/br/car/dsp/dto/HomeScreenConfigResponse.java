package br.car.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Home screen filter configuration, including the AOI detail panel labels and fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HomeScreenConfigResponse(
		String title,
		List<String> hierarchyKeys,
		ScreenFieldConfigResponse identifier,
		ScreenFieldConfigResponse theme,
		String level1SectionTitle,
		String level2SectionTitle,
		String filterByTitle,
		HomeDetailSearchConfigResponse detail
) {

	public HomeScreenConfigResponse {
		if (detail == null) {
			detail = HomeDetailSearchConfigResponse.defaults();
		}
	}
}
