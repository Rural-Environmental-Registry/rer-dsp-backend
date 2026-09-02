package br.car.dsp.config.download;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DownloadTerritoryFilterConfig(
        String strategy,
        String level3Field,
        String aoiLinkField
) {
}
