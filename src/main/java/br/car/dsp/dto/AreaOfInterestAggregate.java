package br.car.dsp.dto;

import java.math.BigDecimal;

/**
 * Aggregates for the area-of-interest KPI (count + area sum).
 */
public interface AreaOfInterestAggregate {
	Long getCount();
	BigDecimal getTotalArea();
}
