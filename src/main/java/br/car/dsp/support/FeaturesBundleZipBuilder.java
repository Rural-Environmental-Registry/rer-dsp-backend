package br.car.dsp.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

@Component
public class FeaturesBundleZipBuilder {

	public byte[] build(Map<String, byte[]> entries) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output)) {
			for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
				ZipEntry zipEntry = new ZipEntry(entry.getKey());
				zip.putNextEntry(zipEntry);
				zip.write(entry.getValue());
				zip.closeEntry();
			}
		}
		return output.toByteArray();
	}
}
