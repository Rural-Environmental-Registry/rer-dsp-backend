package br.car.dsp.api;

import br.car.dsp.dto.AboutConfigResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * About page configuration (banner title and tabbed content).
 */
@RequestMapping("/config")
public interface AboutApi {

	@GetMapping(value = "/about", produces = MediaType.APPLICATION_JSON_VALUE)
	AboutConfigResponse getAboutConfig();
}
