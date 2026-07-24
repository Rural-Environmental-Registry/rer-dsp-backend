package br.car.dsp.dto;

import java.math.BigDecimal;

public record DetailByIdentifierResponse(
		String id,
		String latitude,
		String longitude,
		TerritoryLevelsResponse territory,
		String registrationDate,
		String alterationDate,
		BigDecimal area
) {
}
