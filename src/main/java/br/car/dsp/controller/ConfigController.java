package br.car.dsp.controller;

import br.car.dsp.api.ConfigApi;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.service.InstallationConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Config", description = "Installation configuration (core file)")
public class ConfigController implements ConfigApi {

	private final InstallationConfigService installationConfigService;

	public ConfigController(InstallationConfigService installationConfigService) {
		this.installationConfigService = installationConfigService;
	}

	@Override
	@Operation(summary = "Returns hierarchy, labels, and per-screen filters")
	public InstallationConfigResponse getInstallationConfig() {
		return installationConfigService.getInstallationConfig();
	}
}
