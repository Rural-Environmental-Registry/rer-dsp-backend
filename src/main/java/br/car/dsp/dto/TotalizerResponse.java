package br.car.dsp.dto;

/**
 * Mirrors Consulta Pública Totalizer.
 */
public record TotalizerResponse(
		String name,
		String code,
		Double value,
		String subItemName,
		Long subItemValue,
		String unitOfMeasurement
) {
}
