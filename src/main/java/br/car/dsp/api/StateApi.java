package br.car.dsp.api;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.StateResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contracts compatible with Consulta Pública (/state/...).
 */
@RequestMapping("/state")
public interface StateApi {

	@GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
	List<StateResponse> getAll();

	@GetMapping(value = "/getCitiesByUf/{idState}", produces = MediaType.APPLICATION_JSON_VALUE)
	List<CityResponse> getCitiesByUf(@PathVariable("idState") String idState);

	@GetMapping(value = "/getUfsByRegion/{region}", produces = MediaType.APPLICATION_JSON_VALUE)
	List<StateResponse> getUfsByRegion(@PathVariable("region") String region);
}
