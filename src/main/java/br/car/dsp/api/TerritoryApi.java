package br.car.dsp.api;

import br.car.dsp.dto.TerritoryBoundaryBoxResponse;
import br.car.dsp.dto.TerritoryOptionResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Territorial options by generic level.
 */
@RequestMapping("/territory")
public interface TerritoryApi {

	@GetMapping(value = "/options", produces = MediaType.APPLICATION_JSON_VALUE)
	List<TerritoryOptionResponse> getOptions(
			@RequestParam("level") String level,
			@RequestParam(value = "parentId", required = false) String parentId
	);

	@GetMapping(value = "/boundary-box", produces = MediaType.APPLICATION_JSON_VALUE)
	TerritoryBoundaryBoxResponse getBoundaryBox(
			@RequestParam(value = "level2Ids", required = false) List<String> level2Ids,
			@RequestParam(value = "level3Ids", required = false) List<String> level3Ids
	);
}
