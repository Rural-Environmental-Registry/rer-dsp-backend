package br.car.dsp.dto;

import java.math.BigDecimal;

/**
 * Espelha DetailByIdentifier do Consulta Pública (+ centróide vindo do backend).
 */
public record DetailByIdentifierResponse(
		String codeProperty,
		String latitude,
		String longitude,
		String geographicCoordinatesOfCentroid,
		String idState,
		String nameState,
		String nameCity,
		BigDecimal fiscalModules,
		String createdAt,
		String lastRectification,
		BigDecimal haRegisteredArea,
		Integer idOrigin,
		String bounderBox
) {
}
