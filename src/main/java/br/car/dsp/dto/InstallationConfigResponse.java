package br.car.dsp.dto;

import java.util.List;

public record InstallationConfigResponse(
		List<HierarchyLevelConfigResponse> hierarchy,
		ScreensConfigResponse screens,
		HomeKpisConfigResponse kpis,
		AreaOfInterestMeasuresConfigResponse areaOfInterest
) {

	public InstallationConfigResponse {
		if (areaOfInterest == null) {
			areaOfInterest = AreaOfInterestMeasuresConfigResponse.defaults();
		}
	}
}
