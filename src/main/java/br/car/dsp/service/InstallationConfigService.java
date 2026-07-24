package br.car.dsp.service;

import br.car.dsp.config.InstallationConfigProperties;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
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

/**
 * Reads the installation configuration from an external JSON file.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstallationConfigService {

	private final InstallationConfigProperties properties;
	private final ObjectMapper objectMapper;
	private final ResourceLoader resourceLoader = new DefaultResourceLoader();

	private volatile InstallationConfigResponse cached;

	public InstallationConfigResponse getInstallationConfig() {
		InstallationConfigResponse current = cached;
		if (current != null) {
			return current;
		}
		synchronized (this) {
			if (cached == null) {
				cached = loadFromFile();
			}
			return cached;
		}
	}

	private InstallationConfigResponse loadFromFile() {
		String location = properties.getFile();
		log.info("Loading installation config from {}", location);
		try (InputStream input = openStream(location)) {
			InstallationConfigResponse loaded = objectMapper.readValue(input, InstallationConfigResponse.class);
			return normalizeAreaOfInterest(loaded);
		} catch (IOException ex) {
			log.error("Failed to load installation config from {}", location, ex);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to load installation config",
					ex
			);
		}
	}

	private InstallationConfigResponse normalizeAreaOfInterest(InstallationConfigResponse loaded) {
		AreaOfInterestMeasuresConfigResponse measures = loaded.areaOfInterest();
		String unit = measures.areaUnit();
		String label = measures.areaUnitLabel();

		// Free-form unit; blank/missing must not fall back to "ha" (hides misconfiguration).
		boolean unitMissing = unit == null || unit.isBlank();
		if (unitMissing) {
			log.warn(
					"areaOfInterest.areaUnit is missing. Using placeholder '{}'.",
					AreaOfInterestMeasuresConfigResponse.DEFAULT_UNIT
			);
			unit = AreaOfInterestMeasuresConfigResponse.DEFAULT_UNIT;
		} else {
			unit = unit.trim();
		}
		if (label == null || label.isBlank()) {
			label = unitMissing
					? AreaOfInterestMeasuresConfigResponse.DEFAULT_LABEL
					: unit;
		} else {
			label = label.trim();
		}

		AreaOfInterestMeasuresConfigResponse normalized =
				new AreaOfInterestMeasuresConfigResponse(unit, label);
		if (normalized.equals(measures)) {
			return loaded;
		}
		return new InstallationConfigResponse(
				loaded.hierarchy(),
				loaded.screens(),
				loaded.kpis(),
				normalized
		);
	}

	private InputStream openStream(String location) throws IOException {
		if (location.startsWith("classpath:") || location.startsWith("file:")) {
			Resource resource = resourceLoader.getResource(location);
			if (!resource.exists()) {
				throw new IOException("Installation config not found: " + location);
			}
			return resource.getInputStream();
		}

		Path path = Path.of(location);
		if (!Files.exists(path)) {
			throw new IOException("Installation config not found: " + path.toAbsolutePath());
		}
		return Files.newInputStream(path);
	}
}
