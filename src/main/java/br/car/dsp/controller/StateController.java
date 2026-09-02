package br.car.dsp.controller;

import br.car.dsp.api.StateApi;
import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.StateResponse;
import br.car.dsp.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "State", description = "UFs and municipalities (mock — Consulta Pública contract)")
public class StateController implements StateApi {

	private final StateService stateService;

	public StateController(StateService stateService) {
		this.stateService = stateService;
	}

	@Override
	@Operation(summary = "Lists all UFs")
	public List<StateResponse> getAll() {
		return stateService.getAllUf();
	}

	@Override
	@Operation(summary = "Lists municipalities by UF")
	public List<CityResponse> getCitiesByUf(String idState) {
		return stateService.getCitiesByUfId(idState);
	}

	@Override
	@Operation(summary = "Lists UFs by region code")
	public List<StateResponse> getUfsByRegion(String region) {
		return stateService.getUfsByRegion(region);
	}
}
