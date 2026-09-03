package br.car.dsp.support;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeaturesBundleZipBuilderTest {

	private final FeaturesBundleZipBuilder builder = new FeaturesBundleZipBuilder();

	@Test
	void build_ShouldCreateZipWithAllEntriesAndContents() throws IOException {
		Map<String, byte[]> entries = new LinkedHashMap<>();
		entries.put("area-of-interest_demo-001.csv", "id\nDEMO-001\n".getBytes());
		entries.put("generic-layer_demo-001.csv", "area_of_interest_id\nDEMO-001\n".getBytes());

		byte[] zipBytes = builder.build(entries);
		Map<String, byte[]> extracted = unzip(zipBytes);

		assertTrue(isZipArchive(zipBytes));
		assertEquals(2, extracted.size());
		assertArrayEquals("id\nDEMO-001\n".getBytes(), extracted.get("area-of-interest_demo-001.csv"));
		assertArrayEquals(
				"area_of_interest_id\nDEMO-001\n".getBytes(),
				extracted.get("generic-layer_demo-001.csv")
		);
	}

	@Test
	void build_ShouldPreserveEntryOrderFromInputMap() throws IOException {
		Map<String, byte[]> entries = new LinkedHashMap<>();
		entries.put("first.csv", "1".getBytes());
		entries.put("second.csv", "2".getBytes());
		entries.put("third.csv", "3".getBytes());

		Map<String, byte[]> extracted = unzip(builder.build(entries));

		assertEquals(
				java.util.List.of("first.csv", "second.csv", "third.csv"),
				java.util.List.copyOf(extracted.keySet())
		);
	}

	@Test
	void build_ShouldCreateEmptyZipWhenNoEntries() throws IOException {
		byte[] zipBytes = builder.build(Map.of());

		assertTrue(isZipArchive(zipBytes));
		assertTrue(unzip(zipBytes).isEmpty());
	}

	private static boolean isZipArchive(byte[] bytes) {
		return bytes.length >= 2 && bytes[0] == 'P' && bytes[1] == 'K';
	}

	private static Map<String, byte[]> unzip(byte[] zipBytes) throws IOException {
		Map<String, byte[]> result = new LinkedHashMap<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				result.put(entry.getName(), zip.readAllBytes());
			}
		}
		return result;
	}
}
