package br.car.dsp.api;

import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contratos compatíveis com o Consulta Pública (/totalizer/...).
 * Mantém o typo getDeatilsByIdentifier do sistema original.
 */
@RequestMapping("/totalizer")
public interface TotalizerApi {

	@PostMapping(
			value = "/getTotalizerByStateOrCity",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	List<TotalizerResponse> getTotalizerByStateOrCity(@RequestBody TotalizerFilterRequest filter);

	@GetMapping(value = "/getDeatilsByIdentifier/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE)
	DetailByIdentifierResponse getDetailsByIdentifier(@PathVariable("identifier") String identifier);
}
