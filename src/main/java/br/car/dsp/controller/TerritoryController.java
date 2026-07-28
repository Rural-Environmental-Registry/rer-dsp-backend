package br.car.dsp.controller;

import br.car.dsp.api.TerritoryApi;
import br.car.dsp.dto.TerritoryOptionResponse;
import br.car.dsp.service.TerritoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Territory", description = "Generic territorial options")
public class TerritoryController implements TerritoryApi {

	private final TerritoryService territoryService;

	public TerritoryController(TerritoryService territoryService) {
		this.territoryService = territoryService;
	}

	@Override
	@Operation(summary = "Lists options for a level (level1/level2/level3)")
	public List<TerritoryOptionResponse> getOptions(String level, String parentId) {
		return territoryService.getOptions(level, parentId);
	}
}
