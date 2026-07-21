package br.car.dsp.service;

import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.mock.InstallationConfigMockData;
import org.springframework.stereotype.Service;

@Service
public class InstallationConfigService {

	public InstallationConfigResponse getInstallationConfig() {
		return InstallationConfigMockData.get();
	}
}
