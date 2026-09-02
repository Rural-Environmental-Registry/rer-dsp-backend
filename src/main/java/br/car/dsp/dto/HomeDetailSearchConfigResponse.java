package br.car.dsp.dto;

import java.util.List;

public record HomeDetailSearchConfigResponse(
		String sectionTitle,
		String areaOfInterestSectionTitle,
		String registrationDateLabel,
		String alterationDateLabel,
		String latitudeLabel,
		String longitudeLabel,
		String areaLabel,
		String featuresDownloadLabel,
		List<DetailFieldConfigResponse> fields
) {

	public HomeDetailSearchConfigResponse {
		fields = fields == null ? List.of() : List.copyOf(fields);
	}

	public static HomeDetailSearchConfigResponse defaults() {
		return new HomeDetailSearchConfigResponse(
				"Search details",
				"Record data",
				"Registration date",
				"Alteration date",
				"Latitude",
				"Longitude",
				"Area",
				"Download features",
				List.of()
		);
	}
}
