package br.car.dsp.service;

import br.car.dsp.config.InstallationConfigProperties;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallationConfigServiceTest {

	@Test
	void getInstallationConfig_ShouldLoadEnglishLabelsFromTestFixture() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("classpath:installation-config-test.json");

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
		assertEquals("AREA_OF_INTEREST", config.kpis().primaryCode());
		assertEquals("ha", config.areaOfInterest().areaUnit());
		assertEquals("ha", config.areaOfInterest().areaUnitLabel());
		assertEquals("dd/MM/yyyy", config.formats().date());
		assertEquals("dd/MM/yyyy HH:mm:ss", config.formats().dateTime());
		assertEquals("Search details", config.screens().home().detail().sectionTitle());
		assertEquals("Area of interest data", config.screens().home().detail().areaOfInterestSectionTitle());
		assertEquals("Registration date", config.screens().home().detail().registrationDateLabel());
		assertEquals(List.of(), config.screens().home().detail().fields());
		assertFalse(config.hierarchy().isEmpty());
	}

	@Test
	void getInstallationConfig_ShouldNotExposeDetailOnDownloadsScreen() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("classpath:installation-config-test.json");

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());

		InstallationConfigResponse config = service.getInstallationConfig();

		assertNotNull(config.screens().downloads());
		assertEquals("Download public data", config.screens().downloads().title());
	}

	@Test
	void getInstallationConfig_ShouldLoadPortugueseLabelsAndAreaUnit() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("classpath:installationConfigPTBR.json");

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());

		InstallationConfigResponse config = service.getInstallationConfig();

		assertEquals("Região", config.hierarchy().getFirst().label());
		assertEquals("Estado", config.hierarchy().get(1).label());
		assertEquals("Município", config.hierarchy().get(2).label());
		assertEquals("Identificador", config.screens().home().identifier().label());
		assertEquals("Detalhes da consulta", config.screens().home().detail().sectionTitle());
		assertEquals("Dados da área de interesse", config.screens().home().detail().areaOfInterestSectionTitle());
		assertEquals("Data de registro", config.screens().home().detail().registrationDateLabel());
		assertEquals(List.of(), config.screens().home().detail().fields());
		assertEquals("ha", config.areaOfInterest().areaUnit());
		assertEquals("ha", config.areaOfInterest().areaUnitLabel());
		assertEquals("dd/MM/yyyy", config.formats().date());
	}

	@Test
	void getInstallationConfig_ShouldLoadDetailFieldsMixingColumnAndCalculated() throws Exception {
		Path temp = Files.createTempFile("installation-config-detail-fields", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": {
				    "home": {
				      "title": "Home",
				      "hierarchyKeys": ["level2"],
				      "detail": {
				        "sectionTitle": "Search details",
				        "areaOfInterestSectionTitle": "Area of interest data",
				        "registrationDateLabel": "Registration date",
				        "alterationDateLabel": "Alteration date",
				        "latitudeLabel": "Latitude",
				        "longitudeLabel": "Longitude",
				        "areaLabel": "Area",
				        "featuresDownloadLabel": "Download features",
				        "fields": [
				          { "field": "nome", "label": "Property name" },
				          { "field": "calculated.latitude", "label": "Centroid latitude" }
				        ]
				      }
				    },
				    "downloads": null
				  },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] }
				}
				"""
		);

		InstallationConfigResponse config = loadConfigFrom(temp);

		assertEquals(2, config.screens().home().detail().fields().size());
		assertEquals("nome", config.screens().home().detail().fields().getFirst().field());
		assertEquals("Property name", config.screens().home().detail().fields().getFirst().label());
		assertEquals("calculated.latitude", config.screens().home().detail().fields().get(1).field());
		assertEquals("Centroid latitude", config.screens().home().detail().fields().get(1).label());
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
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
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
		assertEquals("yyyy-MM-dd", config.formats().date());
		assertEquals("yyyy-MM-dd'T'HH:mm:ss", config.formats().dateTime());
	}

	@Test
	void getInstallationConfig_ShouldLoadManualInitialMapView() throws Exception {
		Path temp = Files.createTempFile("installation-config-map-view", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "map": {
				    "initialView": {
				      "mode": "manual",
				      "latitude": 39.5,
				      "longitude": -8.0,
				      "zoom": 7
				    }
				  }
				}
				"""
		);

		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile(temp.toAbsolutePath().toString());

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());
		InstallationConfigResponse config = service.getInstallationConfig();

		assertNotNull(config.map());
		assertEquals("manual", config.map().initialView().mode());
		assertEquals(39.5, config.map().initialView().latitude(), 1e-9);
		assertEquals(-8.0, config.map().initialView().longitude(), 1e-9);
		assertEquals(7, config.map().initialView().zoom());
	}

	@Test
	void getInstallationConfig_ShouldLoadTerritorialBboxInitialMapView() throws Exception {
		Path temp = Files.createTempFile("installation-config-map-view-territorial", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "map": {
				    "initialView": {
				      "mode": "territorial_bbox",
				      "latitude": null,
				      "longitude": null,
				      "zoom": null
				    }
				  }
				}
				"""
		);

		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile(temp.toAbsolutePath().toString());

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());
		InstallationConfigResponse config = service.getInstallationConfig();

		assertNotNull(config.map());
		assertEquals("territorial_bbox", config.map().initialView().mode());
		assertNull(config.map().initialView().latitude());
	}

	@Test
	void getInstallationConfig_ShouldLoadPlanetInitialMapView() throws Exception {
		Path temp = Files.createTempFile("installation-config-map-view-planet", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "map": {
				    "initialView": {
				      "mode": "planet",
				      "latitude": null,
				      "longitude": null,
				      "zoom": null
				    }
				  }
				}
				"""
		);

		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile(temp.toAbsolutePath().toString());

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());
		InstallationConfigResponse config = service.getInstallationConfig();

		assertNotNull(config.map());
		assertEquals("planet", config.map().initialView().mode());
	}

	@Test
	void getInstallationConfig_ShouldIgnoreInvalidManualInitialMapView() throws Exception {
		Path temp = Files.createTempFile("installation-config-invalid-map-view", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "map": {
				    "initialView": {
				      "mode": "manual",
				      "latitude": 120.0,
				      "longitude": -8.0,
				      "zoom": 7
				    }
				  }
				}
				"""
		);

		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile(temp.toAbsolutePath().toString());

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());
		InstallationConfigResponse config = service.getInstallationConfig();

		assertNull(config.map());
	}

	@Test
	void getInstallationConfig_ShouldApplyDefaultAreaUnitsWhenAreaUnitIsMissing() throws Exception {
		Path temp = Files.createTempFile("installation-config-missing-unit", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "areaOfInterest": {
				    "areaUnit": "   ",
				    "areaUnitLabel": ""
				  }
				}
				"""
		);

		InstallationConfigResponse config = loadConfigFrom(temp);

		assertEquals(AreaOfInterestMeasuresConfigResponse.DEFAULT_UNIT, config.areaOfInterest().areaUnit());
		assertEquals(AreaOfInterestMeasuresConfigResponse.DEFAULT_LABEL, config.areaOfInterest().areaUnitLabel());
	}

	@Test
	void getInstallationConfig_ShouldUseAreaUnitAsLabelWhenLabelIsMissing() throws Exception {
		Path temp = Files.createTempFile("installation-config-missing-label", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "areaOfInterest": {
				    "areaUnit": "  hectares ",
				    "areaUnitLabel": "  "
				  }
				}
				"""
		);

		InstallationConfigResponse config = loadConfigFrom(temp);

		assertEquals("hectares", config.areaOfInterest().areaUnit());
		assertEquals("hectares", config.areaOfInterest().areaUnitLabel());
	}

	@Test
	void getInstallationConfig_ShouldTrimAreaOfInterestUnits() throws Exception {
		Path temp = Files.createTempFile("installation-config-trim-units", ".json");
		Files.writeString(
				temp,
				"""
				{
				  "hierarchy": [],
				  "screens": { "home": null, "downloads": null },
				  "kpis": { "maxCards": 1, "primaryCode": "AREA_OF_INTEREST", "cards": [] },
				  "areaOfInterest": {
				    "areaUnit": "  ha ",
				    "areaUnitLabel": "  hectares "
				  }
				}
				"""
		);

		InstallationConfigResponse config = loadConfigFrom(temp);

		assertEquals("ha", config.areaOfInterest().areaUnit());
		assertEquals("hectares", config.areaOfInterest().areaUnitLabel());
	}

	@Test
	void getInstallationConfig_ShouldFailWhenClasspathConfigFileIsMissing() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("classpath:does-not-exist-installation-config.json");

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				service::getInstallationConfig
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Failed to load installation config", exception.getReason());
		assertTrue(exception.getCause().getMessage().contains("does-not-exist-installation-config.json"));
	}

	@Test
	void getInstallationConfig_ShouldFailWhenFilesystemConfigFileIsMissing() {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile("/tmp/does-not-exist-installation-config.json");

		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				service::getInstallationConfig
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("Failed to load installation config", exception.getReason());
		assertTrue(exception.getCause().getMessage().contains("does-not-exist-installation-config.json"));
	}

	private static InstallationConfigResponse loadConfigFrom(Path temp) {
		InstallationConfigProperties properties = new InstallationConfigProperties();
		properties.setFile(temp.toAbsolutePath().toString());
		InstallationConfigService service = new InstallationConfigService(properties, new ObjectMapper());
		return service.getInstallationConfig();
	}
}
