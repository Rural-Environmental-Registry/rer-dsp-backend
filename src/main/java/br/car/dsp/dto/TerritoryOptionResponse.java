package br.car.dsp.dto;

/**
 * Generic option for a territorial level (id + display name).
 */
public record TerritoryOptionResponse(
		String id,
		String name
) {
}
