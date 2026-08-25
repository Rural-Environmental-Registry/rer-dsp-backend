package br.car.dsp.dto;

/**
 * One tab of the About page. {@code content} is the markdown text read from
 * the file referenced by the tab in the index JSON, not the file path.
 */
public record AboutTabResponse(
		String id,
		String label,
		String content
) {
}
