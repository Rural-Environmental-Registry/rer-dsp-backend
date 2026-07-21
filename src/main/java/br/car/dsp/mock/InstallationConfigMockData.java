package br.car.dsp.mock;

import br.car.dsp.dto.HierarchyLevelConfigResponse;
import br.car.dsp.dto.HomeKpisConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.dto.KpiCardConfigResponse;
import br.car.dsp.dto.ScreenConfigResponse;
import br.car.dsp.dto.ScreenFieldConfigResponse;
import br.car.dsp.dto.ScreensConfigResponse;

import java.util.List;

/**
 * Configuração mock da instalação.
 * Labels genéricas por enquanto; no futuro vêm do banco por adotante.
 */
public final class InstallationConfigMockData {

	public static final String PRIMARY_KPI_CODE = "REGISTERED_AREA";

	private InstallationConfigMockData() {
	}

	public static InstallationConfigResponse get() {
		return new InstallationConfigResponse(
				List.of(
						new HierarchyLevelConfigResponse("level1", "Level 1", "Select level 1", 1),
						new HierarchyLevelConfigResponse("level2", "Level 2", "Select level 2", 2),
						new HierarchyLevelConfigResponse("level3", "Level 3", "Select level 3", 3)
				),
				new ScreensConfigResponse(
						new ScreenConfigResponse(
								"Browse registered data",
								List.of("level2", "level3"),
								new ScreenFieldConfigResponse(
										"identifier",
										"Identifier",
										"Enter the identifier"
								),
								null,
								null,
								null,
								null
						),
						new ScreenConfigResponse(
								"Download public data",
								List.of("level1", "level2", "level3"),
								null,
								new ScreenFieldConfigResponse(
										"theme",
										"Theme",
										"All themes"
								),
								"Select the level 1 you want to access for Downloads",
								"Options for the selected level 1",
								"Filter by:"
						)
				),
				new HomeKpisConfigResponse(
						5,
						PRIMARY_KPI_CODE,
						List.of(
								new KpiCardConfigResponse(
										PRIMARY_KPI_CODE,
										"Registered properties",
										"un.",
										"ha",
										"#CED6E5",
										1,
										true
								),
								new KpiCardConfigResponse(
										"LEGAL_RESERVE",
										"Legal reserve",
										"ha",
										null,
										"#C1D2F2",
										2,
										false
								),
								new KpiCardConfigResponse(
										"PERMANENT_PRESERVATION_AREA",
										"Permanent preservation area",
										"ha",
										null,
										"#98B7EC",
										3,
										false
								),
								new KpiCardConfigResponse(
										"NATIVE_VEGETATION",
										"Native vegetation",
										"ha",
										null,
										"#97CCE3",
										4,
										false
								),
								new KpiCardConfigResponse(
										"CONSOLIDATED_AREA",
										"Consolidated area",
										"ha",
										null,
										"#B6C3D9",
										5,
										false
								)
						)
				)
		);
	}
}
