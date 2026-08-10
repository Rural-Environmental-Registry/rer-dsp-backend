package br.car.dsp.config.download;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DownloadThemeConfig(
		String code,
		String name,
		String typeName,
		List<String> formats,
		boolean enabled,
		DownloadTerritoryFilterConfig territoryFilter
) {
}
