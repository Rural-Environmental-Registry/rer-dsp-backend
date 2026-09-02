package br.car.dsp.dto;

/**
 * Installation date/time display defaults.
 * Transfer API always uses yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.
 */
public record FormatsConfigResponse(
		String date,
		String dateTime
) {

	public static final String DEFAULT_DATE = "yyyy-MM-dd";
	public static final String DEFAULT_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";

	public static FormatsConfigResponse defaults() {
		return new FormatsConfigResponse(DEFAULT_DATE, DEFAULT_DATE_TIME);
	}
}
