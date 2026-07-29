package br.car.dsp.dto;

import java.math.BigDecimal;
import java.util.List;

public record DetailByIdentifierResponse(
		String id,
		String latitude,
		String longitude,
		TerritoryLevelsResponse territory,
		String registrationDate,
		String alterationDate,
		BigDecimal area,
		List<String> otherIds
) {
}
