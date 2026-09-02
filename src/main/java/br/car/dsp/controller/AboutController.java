package br.car.dsp.controller;

import br.car.dsp.api.AboutApi;
import br.car.dsp.dto.AboutConfigResponse;
import br.car.dsp.service.AboutConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "About", description = "About page configuration (core file)")
public class AboutController implements AboutApi {

	private final AboutConfigService aboutConfigService;

	public AboutController(AboutConfigService aboutConfigService) {
		this.aboutConfigService = aboutConfigService;
	}

	@Override
	@Operation(summary = "Returns the About page banner title and tabbed content")
	public AboutConfigResponse getAboutConfig() {
		return aboutConfigService.getAboutConfig();
	}
}
