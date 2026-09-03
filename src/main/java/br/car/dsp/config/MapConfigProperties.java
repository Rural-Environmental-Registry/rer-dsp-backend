package br.car.dsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dsp.map")
public class MapConfigProperties {

	private String baseMapsFile = "classpath:baseMapConfig.json";

	private String layersFile = "classpath:mapLayersConfig.json";

	public String getBaseMapsFile() {
		return baseMapsFile;
	}

	public void setBaseMapsFile(String baseMapsFile) {
		this.baseMapsFile = baseMapsFile;
	}

	public String getLayersFile() {
		return layersFile;
	}

	public void setLayersFile(String layersFile) {
		this.layersFile = layersFile;
	}
}
