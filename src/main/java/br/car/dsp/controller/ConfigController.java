package br.car.dsp.controller;

import br.car.dsp.api.ConfigApi;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.service.InstallationConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Config", description = "Configuração da instalação (arquivo do core)")
public class ConfigController implements ConfigApi {

	private final InstallationConfigService installationConfigService;

	public ConfigController(InstallationConfigService installationConfigService) {
		this.installationConfigService = installationConfigService;
	}

	@Override
	@Operation(summary = "Retorna hierarquia, labels e filtros por tela")
	public InstallationConfigResponse getInstallationConfig() {
		return installationConfigService.getInstallationConfig();
	}
}
