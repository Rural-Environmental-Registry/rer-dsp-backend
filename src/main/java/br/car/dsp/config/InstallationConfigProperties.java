package br.car.dsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Installation configuration file path (labels, screens, KPIs).
 * Accepts classpath:… or absolute/relative file system paths (similar to Consulta Pública's layerConfig).
 */
@ConfigurationProperties(prefix = "dsp.installation-config")
public class InstallationConfigProperties {

	/**
	 * Example: classpath:installationConfig.json or /etc/dsp/installationConfig.json
	 */
	private String file = "classpath:installationConfig.json";

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
