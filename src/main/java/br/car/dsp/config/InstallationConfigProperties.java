package br.car.dsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Path to the installation JSON (labels, screens, KPIs).
 * Source of truth: rer-dsp-core/config/installation/installation-config.json
 * Accepts file:… or an absolute/relative filesystem path.
 */
@ConfigurationProperties(prefix = "dsp.installation-config")
public class InstallationConfigProperties {

	/**
	 * Example: file:/config/installation-config.json (Compose)
	 * or file:../rer-dsp-core/config/installation/installation-config.json (local)
	 */
	private String file = "file:../rer-dsp-core/config/installation/installation-config.json";

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
