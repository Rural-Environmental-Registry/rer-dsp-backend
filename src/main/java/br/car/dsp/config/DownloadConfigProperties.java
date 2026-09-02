package br.car.dsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dsp.download")
public class DownloadConfigProperties {

	private String themesFile = "classpath:downloadThemesConfig.json";

	private String geoserverWfsBaseUrl;

	public String getThemesFile() {
		return themesFile;
	}

	public void setThemesFile(String themesFile) {
		this.themesFile = themesFile;
	}

	public String getGeoserverWfsBaseUrl() {
		return geoserverWfsBaseUrl;
	}

	public void setGeoserverWfsBaseUrl(String geoserverWfsBaseUrl) {
		this.geoserverWfsBaseUrl = geoserverWfsBaseUrl;
	}
}
