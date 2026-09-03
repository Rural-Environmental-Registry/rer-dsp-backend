package br.car.dsp.dto;

/**
 * Envelope of a territory boundary_box (WGS84).
 */
public record TerritoryBoundaryBoxResponse(
		double minX,
		double minY,
		double maxX,
		double maxY
) {
}
