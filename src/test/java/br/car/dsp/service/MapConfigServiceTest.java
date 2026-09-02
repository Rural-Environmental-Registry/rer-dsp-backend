package br.car.dsp.service;

import br.car.dsp.config.MapConfigProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapConfigServiceTest {

	@Test
	void getBaseMaps_ShouldLoadFromClasspathJson() {
		MapConfigProperties properties = new MapConfigProperties();
		properties.setBaseMapsFile("classpath:baseMapConfig.json");

		MapConfigService service = new MapConfigService(properties, new ObjectMapper());
		JsonNode config = service.getBaseMaps();

		assertTrue(config.has("baseMap"));
		assertTrue(config.get("baseMap").isArray());
		assertEquals("esri", config.get("baseMap").get(0).get("key").asText());
	}

	@Test
	void getLayers_ShouldLoadFromClasspathJson() {
		MapConfigProperties properties = new MapConfigProperties();
		properties.setLayersFile("classpath:mapLayersConfig.json");

		MapConfigService service = new MapConfigService(properties, new ObjectMapper());
		JsonNode config = service.getLayers();

		assertTrue(config.has("hierarchy"));
		assertTrue(config.get("hierarchy").isArray());
		assertEquals("level1", config.get("hierarchy").get(0).get("key").asText());
		assertTrue(config.has("screens"));
		assertTrue(config.get("screens").has("home"));
		assertEquals("AREA_OF_INTEREST", config.get("kpis").get("primaryCode").asText());
	}

	@Test
	void getLayers_ShouldLoadFromFilesystemPath() throws Exception {
		Path temp = Files.createTempFile("map-layers", ".json");
		Files.writeString(
				temp,
				"""
				{ "groups": [ { "name": "Custom", "key": "c", "layers": [] } ] }
				"""
		);

		MapConfigProperties properties = new MapConfigProperties();
		properties.setLayersFile(temp.toAbsolutePath().toString());

		MapConfigService service = new MapConfigService(properties, new ObjectMapper());
		JsonNode config = service.getLayers();

		assertEquals("c", config.get("groups").get(0).get("key").asText());
	}

	@Test
	void getBaseMaps_ShouldFailWhenFileMissing() {
		MapConfigProperties properties = new MapConfigProperties();
		properties.setBaseMapsFile("classpath:does-not-exist-baseMap.json");

		MapConfigService service = new MapConfigService(properties, new ObjectMapper());

		assertThrows(ResponseStatusException.class, service::getBaseMaps);
	}
}
