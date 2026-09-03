package br.car.dsp.dto;

/**
 * Centroid of an area of interest, reprojected to WGS84 (EPSG:4326) at query time.
 */
public interface CentroidWgs84Projection {
	Double getLatitude();
	Double getLongitude();
}
