package br.car.dsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paths to the About page index JSON and the directory holding its markdown tab content.
 * Source of truth: rer-dsp-core/config/about/
 * Accepts file:… or an absolute/relative filesystem path.
 */
@ConfigurationProperties(prefix = "dsp.about-config")
public class AboutConfigProperties {

	/**
	 * Example: file:/config/about-config.json (Compose)
	 * or file:../rer-dsp-core/config/about/about-config.json (local)
	 */
	private String configFile = "file:../rer-dsp-core/config/about/about-config.json";

	/**
	 * Directory containing the markdown files referenced by each tab's "file".
	 * Example: file:/config/about/ (Compose)
	 * or file:../rer-dsp-core/config/about/ (local)
	 */
	private String contentDir = "file:../rer-dsp-core/config/about/";

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getContentDir() {
		return contentDir;
	}

	public void setContentDir(String contentDir) {
		this.contentDir = contentDir;
	}
}
