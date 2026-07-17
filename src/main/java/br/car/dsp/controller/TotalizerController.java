package br.car.dsp.controller;

import br.car.dsp.api.TotalizerApi;
import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import br.car.dsp.service.TotalizerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Totalizer", description = "KPIs e detalhe por identificador (mock — contrato Consulta Pública)")
public class TotalizerController implements TotalizerApi {

	private final TotalizerService totalizerService;

	public TotalizerController(TotalizerService totalizerService) {
		this.totalizerService = totalizerService;
	}

	@Override
	@Operation(summary = "Totalizadores por UF e/ou municípios")
	public List<TotalizerResponse> getTotalizerByStateOrCity(TotalizerFilterRequest filter) {
		return totalizerService.getTotalizers(filter);
	}

	@Override
	@Operation(summary = "Detalhe por identificador (CAR)")
	public DetailByIdentifierResponse getDetailsByIdentifier(String identifier) {
		return totalizerService.getDetailByIdentifier(identifier);
	}
}
