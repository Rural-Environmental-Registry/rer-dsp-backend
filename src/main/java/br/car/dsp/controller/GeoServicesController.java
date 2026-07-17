package br.car.dsp.controller;

import br.car.dsp.api.GeoServicesApi;
import br.car.dsp.dto.RegionResponse;
import br.car.dsp.service.GeoServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "GeoServices", description = "Regiões com UFs (mock — contrato Consulta Pública)")
public class GeoServicesController implements GeoServicesApi {

	private final GeoServicesService geoServicesService;

	public GeoServicesController(GeoServicesService geoServicesService) {
		this.geoServicesService = geoServicesService;
	}

	@Override
	@Operation(summary = "Lista regiões com estados")
	public List<RegionResponse> getRegions() {
		return geoServicesService.getRegions();
	}
}
