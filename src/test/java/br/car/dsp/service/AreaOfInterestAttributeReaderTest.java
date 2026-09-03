package br.car.dsp.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaOfInterestAttributeReaderTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private AreaOfInterestAttributeReader reader;

	@Test
	void read_ShouldRejectInvalidColumnNamesWithoutQuerying() {
		Map<String, Object> result = reader.read("DF-123", List.of("nome;drop", "calculated.latitude"));

		assertNull(result.get("nome;drop"));
		assertNull(result.get("calculated.latitude"));
		verifyNoInteractions(jdbcTemplate);
	}

	@Test
	void read_ShouldQueryQuotedWhitelistColumns() {
		when(jdbcTemplate.queryForList(anyString(), eq("DF-123")))
				.thenReturn(List.of(Map.of("nome", "Sample property")));

		Map<String, Object> result = reader.read("DF-123", List.of("nome"));

		assertEquals("Sample property", result.get("nome"));
		ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
		verify(jdbcTemplate).queryForList(sql.capture(), eq("DF-123"));
		assertTrue(sql.getValue().contains("\"nome\""));
		assertFalse(sql.getValue().contains(";"));
	}

	@Test
	void read_ShouldReturnNullForMissingColumnWithoutFailing() {
		BadSqlGrammarException missingColumn = new BadSqlGrammarException(
				"select",
				"SELECT \"nome\", \"missing\" FROM dsp.area_of_interest WHERE id = ?",
				new SQLException("column missing does not exist")
		);
		when(jdbcTemplate.queryForList(anyString(), eq("DF-123")))
				.thenThrow(missingColumn)
				.thenReturn(List.of(Map.of("nome", "Sample property")))
				.thenThrow(missingColumn);

		Map<String, Object> result = reader.read("DF-123", List.of("nome", "missing"));

		assertEquals("Sample property", result.get("nome"));
		assertNull(result.get("missing"));
		verify(jdbcTemplate, times(3)).queryForList(anyString(), eq("DF-123"));
	}
}
