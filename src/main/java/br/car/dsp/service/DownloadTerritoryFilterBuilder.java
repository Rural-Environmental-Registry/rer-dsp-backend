package br.car.dsp.service;

import br.car.dsp.config.download.DownloadTerritoryFilterConfig;
import br.car.dsp.config.download.DownloadThemeConfig;
import br.car.dsp.repository.AreaOfInterestRepository;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class DownloadTerritoryFilterBuilder {

	private final TerritoryLevel2Repository level2Repository;
	private final TerritoryLevel3Repository level3Repository;
	private final AreaOfInterestRepository areaOfInterestRepository;

	public void validateTerritory(String level2, String level3) {
		String normalizedLevel2 = requireNonBlank(level2, "Level 2 is required to search downloads");
		if (!level2Repository.existsById(normalizedLevel2)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Territory level 2 not found");
		}

		if (level3 == null || level3.isBlank()) {
			return;
		}

		String normalizedLevel3 = level3.trim();
		if (!level3Repository.existsById(normalizedLevel3)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Territory level 3 not found");
		}

		boolean belongsToLevel2 = level3Repository.findByParent_Id(normalizedLevel2).stream()
				.anyMatch(unit -> unit.getId().equals(normalizedLevel3));
		if (!belongsToLevel2) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Level 3 does not belong to the selected level 2"
			);
		}
	}

	public String buildAoiScopedCqlFilter(DownloadThemeConfig theme, String aoiId) {
		DownloadTerritoryFilterConfig filter = requireFilter(theme);
		String normalizedAoiId = requireNonBlank(aoiId, "Area of interest id is required");

		return switch (filter.strategy().toLowerCase(Locale.ROOT)) {
			case "direct" -> "id = '" + escapeCql(normalizedAoiId) + "'";
			case "aoi_linked" -> {
				String field = requireField(filter.aoiLinkField(), "aoiLinkField");
				yield field + " = '" + escapeCql(normalizedAoiId) + "'";
			}
			default -> throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Unsupported territory strategy: " + filter.strategy()
			);
		};
	}

	public String buildCqlFilter(DownloadThemeConfig theme, String level2, String level3) {
		DownloadTerritoryFilterConfig filter = requireFilter(theme);

		return switch (filter.strategy().toLowerCase(Locale.ROOT)) {
			case "direct" -> buildDirectFilter(filter, level2, level3);
			case "aoi_linked" -> buildAoiLinkedFilter(filter, level2, level3);
			default -> throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Unsupported territory strategy: " + filter.strategy()
			);
		};
	}

	private String buildDirectFilter(
			DownloadTerritoryFilterConfig filter,
			String level2,
			String level3
	) {
		String field = requireField(filter.level3Field(), "level3Field");
		if (level3 != null && !level3.isBlank()) {
			return field + " = '" + escapeCql(level3.trim()) + "'";
		}

		List<String> level3Ids = level3Repository.findByParent_Id(level2.trim()).stream()
				.map(unit -> unit.getId())
				.filter(Objects::nonNull)
				.toList();
		if (level3Ids.isEmpty()) {
			return "1=0";
		}
		return field + " IN (" + joinQuoted(level3Ids) + ")";
	}

	private String buildAoiLinkedFilter(
			DownloadTerritoryFilterConfig filter,
			String level2,
			String level3
	) {
		String field = requireField(filter.aoiLinkField(), "aoiLinkField");
		List<String> aoiIds;
		if (level3 != null && !level3.isBlank()) {
			aoiIds = areaOfInterestRepository.findIdsByTerritoryLevel3Id(level3.trim());
		} else {
			aoiIds = areaOfInterestRepository.findIdsByTerritoryLevel2Id(level2.trim());
		}
		if (aoiIds.isEmpty()) {
			return "1=0";
		}
		return field + " IN (" + joinQuoted(aoiIds) + ")";
	}

	private static DownloadTerritoryFilterConfig requireFilter(DownloadThemeConfig theme) {
		DownloadTerritoryFilterConfig filter = theme.territoryFilter();
		if (filter == null || filter.strategy() == null) {
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Territory configuration missing for theme " + theme.code()
			);
		}
		return filter;
	}

	private static String requireField(String value, String label) {
		if (value == null || value.isBlank()) {
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Required territory field missing: " + label
			);
		}
		return value.trim();
	}

	private static String requireNonBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
		}
		return value.trim();
	}

	private static String joinQuoted(List<String> values) {
		List<String> quoted = new ArrayList<>();
		for (String value : values) {
			quoted.add("'" + escapeCql(value) + "'");
		}
		return String.join(", ", quoted);
	}

	static String escapeCql(String value) {
		return value.replace("'", "''");
	}
}
