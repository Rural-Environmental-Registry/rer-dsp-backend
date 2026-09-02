package br.car.dsp.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/map")
public interface MapApi {

	@GetMapping(value = "/getBaseMaps", produces = MediaType.APPLICATION_JSON_VALUE)
	JsonNode getBaseMaps();

	@GetMapping(value = "/getLayers", produces = MediaType.APPLICATION_JSON_VALUE)
	JsonNode getLayers();
}
