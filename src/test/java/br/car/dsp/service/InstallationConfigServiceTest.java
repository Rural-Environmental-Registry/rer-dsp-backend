package br.car.dsp.service;

import br.car.dsp.config.InstallationConfigProperties;
import br.car.dsp.dto.InstallationConfigResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InstallationConfigServiceTest {

	@Test
	void getInstallationConfig_ShouldLoadEnglishLabelsFromClasspathJson() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("classpath:installationConfig.json");

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());

		InstallationConfigResponse config = service.getInstallationConfig();

		assertNotNull(config);
		assertEquals(3, config.hierarchy().size());
		assertEquals("Level 1", config.hierarchy().getFirst().label());
		assertEquals("Level 2", config.hierarchy().get(1).label());
		assertEquals("Level 3", config.hierarchy().get(2).label());
		assertEquals("Select level 1", config.hierarchy().getFirst().placeholder());
		assertEquals("Browse registered data", config.screens().home().title());
		assertEquals("Identifier", config.screens().home().identifier().label());
		assertEquals("Theme", config.screens().downloads().theme().label());
		assertEquals("Registered properties", config.kpis().cards().getFirst().label());
		assertEquals("REGISTERED_AREA", config.kpis().primaryCode());
		assertFalse(config.hierarchy().isEmpty());
	}
}
