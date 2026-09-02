package br.car.dsp.service;

import br.car.dsp.dto.RegionResponse;
import br.car.dsp.mock.LocationMockData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoServicesService {

	public List<RegionResponse> getRegions() {
		return LocationMockData.getRegions();
	}
}
