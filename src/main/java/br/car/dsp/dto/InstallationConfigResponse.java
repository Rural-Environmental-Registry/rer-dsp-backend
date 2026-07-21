package br.car.dsp.dto;

import java.util.List;

/**
 * Contrato de configuração da instalação DSP.
 * Hoje mock; depois virá do banco/core.
 */
public record InstallationConfigResponse(
		List<HierarchyLevelConfigResponse> hierarchy,
		ScreensConfigResponse screens,
		HomeKpisConfigResponse kpis
) {
}
