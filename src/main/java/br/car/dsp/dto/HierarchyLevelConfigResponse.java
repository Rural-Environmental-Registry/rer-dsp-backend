package br.car.dsp.dto;

/**
 * Nível territorial genérico da instalação (level1 / level2 / level3).
 */
public record HierarchyLevelConfigResponse(
		String key,
		String label,
		String placeholder,
		int order
) {
}
