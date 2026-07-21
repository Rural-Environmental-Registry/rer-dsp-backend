package br.car.dsp.controller;

import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.mock.InstallationConfigMockData;
import br.car.dsp.service.InstallationConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

	@Mock
	private InstallationConfigService installationConfigService;

	@InjectMocks
	private ConfigController configController;

	@Test
	void getInstallationConfig_ShouldDelegateToService() {
		InstallationConfigResponse expected = InstallationConfigMockData.get();
		when(installationConfigService.getInstallationConfig()).thenReturn(expected);

		InstallationConfigResponse result = configController.getInstallationConfig();

		assertEquals(expected, result);
		assertEquals(List.of("level2", "level3"), result.screens().home().hierarchyKeys());
		assertEquals(List.of("level1", "level2", "level3"), result.screens().downloads().hierarchyKeys());
		assertEquals(5, result.kpis().maxCards());
		assertEquals(InstallationConfigMockData.PRIMARY_KPI_CODE, result.kpis().primaryCode());
		assertEquals(InstallationConfigMockData.PRIMARY_KPI_CODE, result.kpis().cards().get(0).code());
		verify(installationConfigService).getInstallationConfig();
	}
}
