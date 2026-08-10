package br.car.dsp.service;

import br.car.dsp.config.DownloadConfigProperties;
import br.car.dsp.config.download.DownloadThemeConfig;
import br.car.dsp.config.download.DownloadThemesDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadConfigService {

	private final DownloadConfigProperties properties;
	private final ObjectMapper objectMapper;
	private final ResourceLoader resourceLoader = new DefaultResourceLoader();

	private volatile DownloadThemesDocument cachedDocument;

	public String resolveWfsBaseUrl() {
		String fromEnv = properties.getGeoserverWfsBaseUrl();
		if (fromEnv != null && !fromEnv.isBlank()) {
			return fromEnv.trim().replaceAll("/$", "");
		}
		DownloadThemesDocument document = getDocument();
		if (document.wfsBaseUrl() != null && !document.wfsBaseUrl().isBlank()) {
			return document.wfsBaseUrl().trim().replaceAll("/$", "");
		}
		throw new ResponseStatusException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"WFS base URL is not configured"
		);
	}

	public List<DownloadThemeConfig> getEnabledThemes() {
		return getDocument().themes().stream()
				.filter(DownloadThemeConfig::enabled)
				.toList();
	}

	public Optional<DownloadThemeConfig> findEnabledTheme(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		String normalized = code.trim().toLowerCase(Locale.ROOT);
		return getEnabledThemes().stream()
				.filter(theme -> theme.code().equalsIgnoreCase(normalized))
				.findFirst();
	}

	private DownloadThemesDocument getDocument() {
		DownloadThemesDocument current = cachedDocument;
		if (current != null) {
			return current;
		}
		synchronized (this) {
			if (cachedDocument == null) {
				cachedDocument = loadDocument(properties.getThemesFile());
			}
			return cachedDocument;
		}
	}

	private DownloadThemesDocument loadDocument(String location) {
		log.info("Loading download themes from {}", location);
		try (InputStream input = openStream(location)) {
			DownloadThemesDocument document = objectMapper.readValue(input, DownloadThemesDocument.class);
			if (document.themes() == null || document.themes().isEmpty()) {
				throw new IOException("Download themes config has no themes");
			}
			return document;
		} catch (IOException ex) {
			log.error("Failed to load download themes from {}", location, ex);
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to load download themes config",
					ex
			);
		}
	}

	private InputStream openStream(String location) throws IOException {
		if (location.startsWith("classpath:") || location.startsWith("file:")) {
			Resource resource = resourceLoader.getResource(location);
			if (!resource.exists()) {
				throw new IOException("Download themes config not found: " + location);
			}
			return resource.getInputStream();
		}

		Path path = Path.of(location);
		if (!Files.exists(path)) {
			throw new IOException("Download themes config not found: " + path.toAbsolutePath());
		}
		return Files.newInputStream(path);
	}
}
