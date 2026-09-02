package br.car.dsp.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlexibleLocalDateTimeTypeTest {

	@Test
	void toLocalDateTime_ShouldKeepLocalDateTime() {
		LocalDateTime value = LocalDateTime.of(2024, 6, 15, 14, 30);
		assertEquals(value, FlexibleLocalDateTimeType.toLocalDateTime(value));
	}

	@Test
	void toLocalDateTime_ShouldMapDateToStartOfDay() {
		assertEquals(
				LocalDateTime.of(2020, 1, 10, 0, 0),
				FlexibleLocalDateTimeType.toLocalDateTime(LocalDate.of(2020, 1, 10))
		);
		assertEquals(
				LocalDateTime.of(2020, 1, 10, 0, 0),
				FlexibleLocalDateTimeType.toLocalDateTime(java.sql.Date.valueOf("2020-01-10"))
		);
	}

	@Test
	void toLocalDateTime_ShouldMapOffsetAndInstant() {
		OffsetDateTime offset = OffsetDateTime.of(2024, 6, 15, 14, 0, 0, 0, ZoneOffset.ofHours(-3));
		assertEquals(LocalDateTime.of(2024, 6, 15, 14, 0), FlexibleLocalDateTimeType.toLocalDateTime(offset));
		assertEquals(
				LocalDateTime.of(2024, 6, 15, 17, 0),
				FlexibleLocalDateTimeType.toLocalDateTime(Instant.parse("2024-06-15T17:00:00Z"))
		);
	}

	@Test
	void toLocalDateTime_ShouldMapTimestampAndNull() {
		assertEquals(
				LocalDateTime.of(2024, 6, 15, 8, 45),
				FlexibleLocalDateTimeType.toLocalDateTime(Timestamp.valueOf("2024-06-15 08:45:00"))
		);
		assertNull(FlexibleLocalDateTimeType.toLocalDateTime(null));
	}

	@Test
	void toLocalDateTime_ShouldRejectUnsupportedType() {
		assertThrows(IllegalStateException.class, () -> FlexibleLocalDateTimeType.toLocalDateTime("2024-06-15"));
	}
}
