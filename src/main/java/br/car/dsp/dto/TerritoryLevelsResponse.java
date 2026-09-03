package br.car.dsp.dto;

/**
 * Territorial levels of detail of the area of interest (L2 and L3).
 */
public record TerritoryLevelsResponse(
		TerritoryLevelRefResponse level2,
		TerritoryLevelRefResponse level3
) {
}
