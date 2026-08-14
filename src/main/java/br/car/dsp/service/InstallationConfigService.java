package br.car.dsp.service;

import br.car.dsp.config.InstallationConfigProperties;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.FormatsConfigResponse;
import br.car.dsp.dto.InitialMapViewConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.dto.MapUiConfigResponse;
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
			return normalize(loaded);
		} catch (IOException ex) {
			log.error("Failed to load installation config from {}", location, ex);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to load installation config",
					ex
			);
		}
	}

	private InstallationConfigResponse normalize(InstallationConfigResponse loaded) {
		AreaOfInterestMeasuresConfigResponse measures = normalizeAreaOfInterest(loaded.areaOfInterest());
		FormatsConfigResponse formats = normalizeFormats(loaded.formats());
		MapUiConfigResponse map = normalizeMap(loaded.map());

		if (measures.equals(loaded.areaOfInterest())
				&& formats.equals(loaded.formats())
				&& map == loaded.map()) {
			return loaded;
		}
		return new InstallationConfigResponse(
				loaded.hierarchy(),
				loaded.screens(),
				loaded.kpis(),
				measures,
				formats,
				map
		);
	}

	private MapUiConfigResponse normalizeMap(MapUiConfigResponse map) {
		if (map == null || map.initialView() == null) {
			return null;
		}

		InitialMapViewConfigResponse initialView = map.initialView();
		String mode = initialView.mode();
		if (mode == null || mode.isBlank()) {
			log.warn("map.initialView.mode is missing. Ignoring map configuration.");
			return null;
		}

		mode = mode.trim();
		return switch (mode) {
			case "territorial_bbox", "planet" -> new MapUiConfigResponse(
					new InitialMapViewConfigResponse(mode, null, null, null)
			);
			case "manual" -> normalizeManualInitialView(initialView);
			default -> {
				log.warn("map.initialView.mode '{}' is invalid. Ignoring map configuration.", mode);
				yield null;
			}
		};
	}

	private MapUiConfigResponse normalizeManualInitialView(InitialMapViewConfigResponse initialView) {
		Double latitude = initialView.latitude();
		Double longitude = initialView.longitude();
		Integer zoom = initialView.zoom();

		if (latitude == null || longitude == null || zoom == null) {
			log.warn(
					"map.initialView manual mode requires latitude, longitude, and zoom. "
							+ "Ignoring map configuration."
			);
			return null;
		}

		if (!isValidLatitude(latitude) || !isValidLongitude(longitude) || !isValidZoom(zoom)) {
			log.warn(
					"map.initialView manual coordinates are invalid (latitude={}, longitude={}, zoom={}). "
							+ "Ignoring map configuration.",
					latitude,
					longitude,
					zoom
			);
			return null;
		}

		return new MapUiConfigResponse(
				new InitialMapViewConfigResponse("manual", latitude, longitude, zoom)
		);
	}

	private static boolean isValidLatitude(double latitude) {
		return Double.isFinite(latitude) && latitude >= -90 && latitude <= 90;
	}

	private static boolean isValidLongitude(double longitude) {
		return Double.isFinite(longitude) && longitude >= -180 && longitude <= 180;
	}

	private static boolean isValidZoom(int zoom) {
		return zoom >= 0 && zoom <= 16;
	}

	private AreaOfInterestMeasuresConfigResponse normalizeAreaOfInterest(
			AreaOfInterestMeasuresConfigResponse measures
	) {
		String unit = measures.areaUnit();
		String label = measures.areaUnitLabel();

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

		return new AreaOfInterestMeasuresConfigResponse(unit, label);
	}

	private FormatsConfigResponse normalizeFormats(FormatsConfigResponse formats) {
		String date = formats.date();
		String dateTime = formats.dateTime();
		if (date == null || date.isBlank()) {
			date = FormatsConfigResponse.DEFAULT_DATE;
		} else {
			date = date.trim();
		}
		if (dateTime == null || dateTime.isBlank()) {
			dateTime = FormatsConfigResponse.DEFAULT_DATE_TIME;
		} else {
			dateTime = dateTime.trim();
		}
		return new FormatsConfigResponse(date, dateTime);
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
