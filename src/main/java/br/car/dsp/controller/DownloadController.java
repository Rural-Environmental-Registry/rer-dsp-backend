package br.car.dsp.controller;

import br.car.dsp.api.DownloadApi;
import br.car.dsp.dto.DownloadItemResponse;
import br.car.dsp.dto.DownloadSearchRequest;
import br.car.dsp.dto.DownloadThemeResponse;
import br.car.dsp.service.DownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Downloads", description = "Temas e arquivos para download (mock — sem S3/GeoServer)")
public class DownloadController implements DownloadApi {

	private final DownloadService downloadService;

	public DownloadController(DownloadService downloadService) {
		this.downloadService = downloadService;
	}

	@Override
	@Operation(summary = "Lista temas habilitados")
	public List<DownloadThemeResponse> getThemes() {
		return downloadService.getThemes();
	}

	@Override
	@Operation(summary = "Busca itens de download por hierarquia e tema")
	public List<DownloadItemResponse> search(DownloadSearchRequest request) {
		return downloadService.search(request);
	}

	@Override
	@Operation(summary = "Baixa arquivo mock do tema/formato")
	public ResponseEntity<byte[]> downloadFile(String level2, String level3, String theme, String format) {
		return downloadService.downloadFile(level2, level3, theme, format);
	}
}
