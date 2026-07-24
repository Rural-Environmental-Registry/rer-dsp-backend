package br.car.dsp.dto;

public record HomeDetailSearchConfigResponse(
		String sectionTitle,
		String propertySectionTitle,
		String registrationDateLabel,
		String alterationDateLabel,
		String latitudeLabel,
		String longitudeLabel,
		String areaLabel,
		String featuresDownloadLabel
) {

	public static HomeDetailSearchConfigResponse defaults() {
		return new HomeDetailSearchConfigResponse(
				"Search details",
				"Record data",
				"Registration date",
				"Alteration date",
				"Latitude",
				"Longitude",
				"Area",
				"Download features"
		);
	}
}
