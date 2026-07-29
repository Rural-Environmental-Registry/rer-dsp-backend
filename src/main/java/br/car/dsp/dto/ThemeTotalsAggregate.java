package br.car.dsp.dto;

import java.math.BigDecimal;

/**
 * Sum of theme measure columns on {@code dsp.area_of_interest} (THEME_1…THEME_4).
 */
public interface ThemeTotalsAggregate {
	BigDecimal getTheme1();
	BigDecimal getTheme2();
	BigDecimal getTheme3();
	BigDecimal getTheme4();
}
