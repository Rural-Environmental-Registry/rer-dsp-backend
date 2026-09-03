package br.car.dsp.service;

import br.car.dsp.config.MapConfigProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapConfigService {

	private final MapConfigProperties properties;
	private final ObjectMapper objectMapper;
	private final ResourceLoader resourceLoader = new DefaultResourceLoader();

	private volatile JsonNode cachedBaseMaps;
	private volatile JsonNode cachedLayers;

	public JsonNode getBaseMaps() {
		JsonNode current = cachedBaseMaps;
		if (current != null) {
			return current;
		}
		synchronized (this) {
			if (cachedBaseMaps == null) {
				cachedBaseMaps = loadJson(properties.getBaseMapsFile(), "base maps");
			}
			return cachedBaseMaps;
		}
	}

	public JsonNode getLayers() {
		JsonNode current = cachedLayers;
		if (current != null) {
			return current;
		}
		synchronized (this) {
			if (cachedLayers == null) {
				cachedLayers = loadJson(properties.getLayersFile(), "map layers");
			}
			return cachedLayers;
		}
	}

	private JsonNode loadJson(String location, String label) {
		log.info("Loading {} from {}", label, location);
		try (InputStream input = openStream(location)) {
			return objectMapper.readTree(input);
		} catch (IOException ex) {
			log.error("Failed to load {} from {}", label, location, ex);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to load " + label,
					ex
			);
		}
	}

	private InputStream openStream(String location) throws IOException {
		if (location.startsWith("classpath:") || location.startsWith("file:")) {
			Resource resource = resourceLoader.getResource(location);
			if (!resource.exists()) {
				throw new IOException("Map config not found: " + location);
			}
			return resource.getInputStream();
		}

		Path path = Path.of(location);
		if (!Files.exists(path)) {
			throw new IOException("Map config not found: " + path.toAbsolutePath());
		}
		return Files.newInputStream(path);
	}
}
