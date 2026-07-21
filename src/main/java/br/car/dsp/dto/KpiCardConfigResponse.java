package br.car.dsp.dto;

/**
 * Definição estrutural de um card de KPI (rótulos/unidades/cores).
 * Valores numéricos vêm dos totalizers; labels vêm daqui.
 */
public record KpiCardConfigResponse(
		String code,
		String label,
		String unitOfMeasurement,
		String optionalLabel,
		String accentColor,
		int order,
		boolean required
) {
}
