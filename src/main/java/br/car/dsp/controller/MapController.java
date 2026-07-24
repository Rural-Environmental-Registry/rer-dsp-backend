package br.car.dsp.controller;

import br.car.dsp.api.MapApi;
import br.car.dsp.service.MapConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Map", description = "Basemaps and WMS map layers")
public class MapController implements MapApi {

	private final MapConfigService mapConfigService;

	public MapController(MapConfigService mapConfigService) {
		this.mapConfigService = mapConfigService;
	}

	@Override
	@Operation(summary = "Returns basemaps (external JSON)")
	public JsonNode getBaseMaps() {
		return mapConfigService.getBaseMaps();
	}

	@Override
	@Operation(summary = "Returns WMS layer groups (external JSON)")
	public JsonNode getLayers() {
		return mapConfigService.getLayers();
	}
}
