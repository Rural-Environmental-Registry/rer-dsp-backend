package br.car.dsp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Reads extra AOI columns from dsp-db. Names must match the identifier whitelist.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaOfInterestAttributeReader {

	static final Pattern COLUMN_NAME = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

	private static final String TABLE = "dsp.area_of_interest";

	private final JdbcTemplate jdbcTemplate;

	public Map<String, Object> read(String id, Collection<String> extraColumns) {
		LinkedHashMap<String, Object> values = new LinkedHashMap<>();
		List<String> validColumns = new ArrayList<>();
		for (String column : extraColumns) {
			if (column == null || column.isBlank()) {
				continue;
			}
			if (!COLUMN_NAME.matcher(column).matches()) {
				log.warn("Ignoring invalid AOI detail column name '{}'", column);
				values.put(column, null);
				continue;
			}
			validColumns.add(column);
		}
		if (id == null || id.isBlank() || validColumns.isEmpty()) {
			return values;
		}

		try {
			values.putAll(queryColumns(id, validColumns));
			return values;
		} catch (DataAccessException ex) {
			log.warn("Failed to read AOI extra columns in batch for id '{}': {}", id, ex.getMessage());
		}

		for (String column : validColumns) {
			values.put(column, queryOne(id, column));
		}
		return values;
	}

	private Map<String, Object> queryColumns(String id, List<String> columns) {
		String sql = "SELECT " + columns.stream().map(this::quoted).collect(Collectors.joining(", "))
				+ " FROM " + TABLE + " WHERE id = ?";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
		if (rows.isEmpty()) {
			return nullsFor(columns);
		}
		return mapRow(rows.getFirst(), columns);
	}

	private Object queryOne(String id, String column) {
		String sql = "SELECT " + quoted(column) + " FROM " + TABLE + " WHERE id = ?";
		try {
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
			if (rows.isEmpty()) {
				return null;
			}
			return valueFromRow(rows.getFirst(), column);
		} catch (DataAccessException ex) {
			log.warn("AOI extra column '{}' is missing for id '{}': {}", column, id, ex.getMessage());
			return null;
		}
	}

	private static Map<String, Object> mapRow(Map<String, Object> row, List<String> columns) {
		LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
		for (String column : columns) {
			mapped.put(column, valueFromRow(row, column));
		}
		return mapped;
	}

	private static Map<String, Object> nullsFor(List<String> columns) {
		LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
		for (String column : columns) {
			mapped.put(column, null);
		}
		return mapped;
	}

	private static Object valueFromRow(Map<String, Object> row, String column) {
		if (row.containsKey(column)) {
			return row.get(column);
		}
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			if (column.equalsIgnoreCase(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	private String quoted(String column) {
		return "\"" + column + "\"";
	}
}
