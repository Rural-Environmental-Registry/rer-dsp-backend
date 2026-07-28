package br.car.dsp.dto;

/**
 * Generic territorial level for the installation (level1 / level2 / level3).
 */
public record HierarchyLevelConfigResponse(
		String key,
		String label,
		String placeholder,
		int order
) {
}
