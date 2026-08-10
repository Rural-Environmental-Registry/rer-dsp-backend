package br.car.dsp.support;

import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.TerritoryLevel2Repository;
import br.car.dsp.repository.TerritoryLevel3Repository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds readable file names for territorial downloads.
 * Pattern: {@code {theme}_{level2}_{level3}.{format}} or {@code {theme}_{level2}.{format}}.
 */
@Component
@RequiredArgsConstructor
public class DownloadFileNameBuilder {

	private static final int MAX_SEGMENT_LENGTH = 48;

	private final TerritoryLevel2Repository level2Repository;
	private final TerritoryLevel3Repository level3Repository;

	public String build(String level2Id, String level3Id, String themeName, String format) {
		List<String> segments = new ArrayList<>();
		segments.add(toFileSegment(themeName, "theme"));
		segments.add(resolveLevel2Segment(level2Id));
		if (level3Id != null && !level3Id.isBlank()) {
			segments.add(resolveLevel3Segment(level3Id));
		}
		return String.join("_", segments) + "." + normalizeExtension(format);
	}

	private String resolveLevel2Segment(String level2Id) {
		return level2Repository.findById(level2Id)
				.map(TerritoryLevel2::getName)
				.map(name -> toFileSegment(name, level2Id))
				.orElseGet(() -> toFileSegment(level2Id, "territory"));
	}

	private String resolveLevel3Segment(String level3Id) {
		return level3Repository.findById(level3Id)
				.map(TerritoryLevel3::getName)
				.map(name -> toFileSegment(name, level3Id))
				.orElseGet(() -> toFileSegment(level3Id, "territory"));
	}

	static String toFileSegment(String raw, String fallback) {
		if (raw == null || raw.isBlank()) {
			return toFileSegment(fallback, "na");
		}

		String withoutAccents = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "");
		String slug = withoutAccents
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");

		if (slug.isBlank()) {
			return toFileSegment(fallback, "na");
		}
		if (slug.length() > MAX_SEGMENT_LENGTH) {
			return slug.substring(0, MAX_SEGMENT_LENGTH).replaceAll("-+$", "");
		}
		return slug;
	}

	static String normalizeExtension(String format) {
		if (format == null || format.isBlank()) {
			return "bin";
		}
		String normalized = format.trim().toLowerCase(Locale.ROOT).replaceAll("^\\.", "");
		return normalized.isBlank() ? "bin" : normalized;
	}
}
