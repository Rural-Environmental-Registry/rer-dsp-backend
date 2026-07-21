package br.car.dsp.dto;

/**
 * Opção genérica de um nível territorial (id + nome exibido).
 */
public record TerritoryOptionResponse(
		String id,
		String name
) {
}
