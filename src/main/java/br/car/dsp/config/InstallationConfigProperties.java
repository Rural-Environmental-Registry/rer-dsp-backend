package br.car.dsp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Caminho do JSON de instalação (labels, telas, KPIs).
 * Fonte de verdade: rer-dsp-core/config/installation/installation-config.json
 * Aceita file:… ou caminho absoluto/relativo no filesystem.
 */
@ConfigurationProperties(prefix = "dsp.installation-config")
public class InstallationConfigProperties {

	/**
	 * Ex.: file:/config/installation-config.json (Compose)
	 * ou file:../rer-dsp-core/config/installation/installation-config.json (local)
	 */
	private String file = "file:../rer-dsp-core/config/installation/installation-config.json";

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
