package br.car.dsp.dto;

/**
 * Area unit configuration for the area of interest.
 * Free text: must reflect the unit of the migrated value .
 */
public record AreaOfInterestMeasuresConfigResponse(
		String areaUnit,
		String areaUnitLabel
) {

	public static final String DEFAULT_UNIT = "unit-not-configured";
	public static final String DEFAULT_LABEL = "unit of measurement not configured";

	public static AreaOfInterestMeasuresConfigResponse defaults() {
		return new AreaOfInterestMeasuresConfigResponse(DEFAULT_UNIT, DEFAULT_LABEL);
	}
}