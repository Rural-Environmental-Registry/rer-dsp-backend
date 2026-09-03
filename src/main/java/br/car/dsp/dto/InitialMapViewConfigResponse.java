package br.car.dsp.dto;

public record InitialMapViewConfigResponse(
		String mode,
		Double latitude,
		Double longitude,
		Integer zoom
) {
}
