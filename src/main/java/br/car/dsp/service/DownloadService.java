package br.car.dsp.service;

import br.car.dsp.dto.DownloadFormatStatus;
import br.car.dsp.dto.DownloadItemResponse;
import br.car.dsp.dto.DownloadSearchRequest;
import br.car.dsp.dto.DownloadThemeResponse;
import br.car.dsp.mock.DownloadThemesMockData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DownloadService {

	public List<DownloadThemeResponse> getThemes() {
		return DownloadThemesMockData.listEnabledThemes();
	}

	public List<DownloadItemResponse> search(DownloadSearchRequest request) {
		if (request == null || request.getLevel2() == null || request.getLevel2().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nível 2 é obrigatório para buscar downloads");
		}
		return DownloadThemesMockData.search(
				request.getLevel2().trim(),
				blankToNull(request.getLevel3()),
				blankToNull(request.getTheme())
		);
	}

	public ResponseEntity<byte[]> downloadFile(String level2, String level3, String theme, String format) {
		if (level2 == null || level2.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nível 2 é obrigatório");
		}
		if (theme == null || theme.isBlank() || format == null || format.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tema e formato são obrigatórios");
		}

		DownloadThemeResponse themeConfig = DownloadThemesMockData.findEnabledByCode(theme)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tema não encontrado"));

		String normalizedFormat = format.trim().toLowerCase();
		if (!themeConfig.formats().contains(normalizedFormat)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato não suportado para o tema");
		}

		String status = DownloadThemesMockData.resolveStatus(
				themeConfig.code(),
				normalizedFormat,
				level2.trim(),
				blankToNull(level3)
		);

		if (!DownloadFormatStatus.AVAILABLE.equals(status)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo indisponível para download");
		}

		byte[] content = DownloadThemesMockData.buildMockFileContent(
				level2.trim(),
				blankToNull(level3),
				themeConfig.code(),
				normalizedFormat
		);
		String fileName = DownloadThemesMockData.buildFileName(
				level2.trim(),
				blankToNull(level3),
				themeConfig.code(),
				normalizedFormat
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		headers.setContentLength(content.length);

		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
