package br.car.dsp.service;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.StateResponse;
import br.car.dsp.mock.LocationMockData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateService {

	public List<StateResponse> getAllUf() {
		return LocationMockData.getAllStates();
	}

	public List<CityResponse> getCitiesByUfId(String idState) {
		return LocationMockData.getCitiesByState(idState);
	}

	public List<StateResponse> getUfsByRegion(String region) {
		return LocationMockData.getStatesByRegionCode(region);
	}
}
