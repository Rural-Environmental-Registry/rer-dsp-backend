package br.car.dsp.api;

import br.car.dsp.dto.TerritoryOptionResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Opções territoriais por nível genérico.
 */
@RequestMapping("/territory")
public interface TerritoryApi {

	@GetMapping(value = "/options", produces = MediaType.APPLICATION_JSON_VALUE)
	List<TerritoryOptionResponse> getOptions(
			@RequestParam("level") String level,
			@RequestParam(value = "parentId", required = false) String parentId
	);
}
