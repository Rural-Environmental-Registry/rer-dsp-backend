package br.car.dsp.service;

import br.car.dsp.config.InstallationConfigProperties;
import br.car.dsp.dto.InstallationConfigResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

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
		assertEquals("ha", config.areaOfInterest().areaUnit());
		assertEquals("ha", config.areaOfInterest().areaUnitLabel());
		assertFalse(config.hierarchy().isEmpty());
	}

	@Test
	void getInstallationConfig_ShouldLoadPortugueseLabelsAndAreaUnit() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("classpath:installationConfigPTBR.json");

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());

		InstallationConfigResponse config = service.getInstallationConfig();

		assertEquals("Continente", config.hierarchy().getFirst().label());
		assertEquals("ha", config.areaOfInterest().areaUnit());
		assertEquals("ha", config.areaOfInterest().areaUnitLabel());
	}

	@Test
	void getInstallationConfig_ShouldAcceptFreeFormAreaUnit() throws Exception {
		Path temp = Files.createTempFile("installation-config-custom-unit", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "REGISTERED_AREA", "cards": [] },
				  "areaOfInterest": {
				    "areaUnit": "football_fields",
				    "areaUnitLabel": "campos de futebol"
				  }
				}
				"""
		);

		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile(temp.toAbsolutePath().toString());

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());
		InstallationConfigResponse config = service.getInstallationConfig();

		assertEquals("football_fields", config.areaOfInterest().areaUnit());
		assertEquals("campos de futebol", config.areaOfInterest().areaUnitLabel());
	}
}
