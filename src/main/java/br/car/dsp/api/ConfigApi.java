package br.car.dsp.api;

import br.car.dsp.dto.InstallationConfigResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Configuração da instalação (hierarquia, labels e telas).
 */
@RequestMapping("/config")
public interface ConfigApi {

	@GetMapping(value = "/installation", produces = MediaType.APPLICATION_JSON_VALUE)
	InstallationConfigResponse getInstallationConfig();
}
