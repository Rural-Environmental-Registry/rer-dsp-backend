package br.car.dsp.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Reads a temporal source column as {@link LocalDateTime},
 * whether {@code date}, {@code timestamp}, or {@code timestamptz}.
 */
public class FlexibleLocalDateTimeType implements UserType<LocalDateTime> {

	@Override
	public int getSqlType() {
		return Types.TIMESTAMP;
	}

	@Override
	public Class<LocalDateTime> returnedClass() {
		return LocalDateTime.class;
	}

	@Override
	public boolean equals(LocalDateTime left, LocalDateTime right) {
		return Objects.equals(left, right);
	}

	@Override
	public int hashCode(LocalDateTime value) {
		return Objects.hashCode(value);
	}

	@Override
	public LocalDateTime nullSafeGet(
			ResultSet resultSet,
			int position,
			SharedSessionContractImplementor session,
			Object owner
	) throws SQLException {
		return toLocalDateTime(resultSet.getObject(position));
	}

	@Override
	public void nullSafeSet(
			PreparedStatement statement,
			LocalDateTime value,
			int index,
			SharedSessionContractImplementor session
	) throws SQLException {
		if (value == null) {
			statement.setObject(index, null);
			return;
		}
		statement.setObject(index, Timestamp.valueOf(value));
	}

	@Override
	public LocalDateTime deepCopy(LocalDateTime value) {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(LocalDateTime value) {
		return value;
	}

	@Override
	public LocalDateTime assemble(Serializable cached, Object owner) {
		return (LocalDateTime) cached;
	}

	static LocalDateTime toLocalDateTime(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof LocalDateTime localDateTime) {
			return localDateTime;
		}
		if (value instanceof LocalDate localDate) {
			return localDate.atStartOfDay();
		}
		if (value instanceof OffsetDateTime offsetDateTime) {
			return offsetDateTime.toLocalDateTime();
		}
		if (value instanceof Instant instant) {
			return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		}
		if (value instanceof Timestamp timestamp) {
			return timestamp.toLocalDateTime();
		}
		if (value instanceof java.sql.Date sqlDate) {
			return sqlDate.toLocalDate().atStartOfDay();
		}
		throw new IllegalStateException(
				"Unsupported temporal JDBC value: " + value.getClass().getName()
		);
	}
}
