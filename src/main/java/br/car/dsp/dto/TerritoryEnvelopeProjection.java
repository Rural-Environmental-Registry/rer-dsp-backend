package br.car.dsp.dto;

/**
 * Envelope (bounding box) of one or more territory geometries, reprojected to
 * WGS84 (EPSG:4326) at query time. All fields are null when no matching
 * geometry was found.
 */
public interface TerritoryEnvelopeProjection {
	Double getMinX();
	Double getMinY();
	Double getMaxX();
	Double getMaxY();
}
