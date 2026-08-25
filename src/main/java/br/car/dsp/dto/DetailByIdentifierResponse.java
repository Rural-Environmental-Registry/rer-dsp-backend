package br.car.dsp.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DetailByIdentifierResponse(
		String id,
		String latitude,
		String longitude,
		TerritoryLevelsResponse territory,
		String registrationDate,
		String alterationDate,
		BigDecimal area,
		List<String> otherIds,
		Map<String, Object> attributes
) {

	public DetailByIdentifierResponse {
		otherIds = otherIds == null ? List.of() : List.copyOf(otherIds);
		attributes = attributes == null
				? Map.of()
				: Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
	}
}
