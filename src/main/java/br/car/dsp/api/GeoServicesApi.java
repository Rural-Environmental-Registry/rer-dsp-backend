package br.car.dsp.api;

import br.car.dsp.dto.RegionResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contratos compatíveis com o Consulta Pública (/geoServices/...).
 */
@RequestMapping("/geoServices")
public interface GeoServicesApi {

	@GetMapping(value = "/getRegions", produces = MediaType.APPLICATION_JSON_VALUE)
	List<RegionResponse> getRegions();
}
