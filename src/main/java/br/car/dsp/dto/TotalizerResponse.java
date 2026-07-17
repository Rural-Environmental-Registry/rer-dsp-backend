package br.car.dsp.dto;

/**
 * Espelha Totalizer do Consulta Pública.
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
