package br.car.dsp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeoServerWfsClientTest {

	private static final String WFS_BASE_URL = "http://localhost:22669/geoserver/dsp/wfs";
	private static final String TYPE_NAME = "dsp:area-of-interest";
	private static final String CQL_FILTER = "territory_level_3_id IN ('5300108')";

	private MockRestServiceServer mockServer;
	private GeoServerWfsClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		client = new GeoServerWfsClient(new ObjectMapper(), builder.build());
	}

	@Test
	void buildLatestAttributeValueUrl_ShouldIncludeSortByCountAndPropertyName() {
		String url = GeoServerWfsClient.buildLatestAttributeValueUrl(WFS_BASE_URL, TYPE_NAME, CQL_FILTER);

		assertTrue(url.contains("sortBy="));
		assertTrue(url.contains("alteration_date"));
		assertTrue(url.contains("count=1"));
		assertTrue(url.contains("propertyName="));
		assertTrue(url.contains("outputFormat="));
		assertFalse(url.contains("resultType=hits"));
	}

	@Test
	void fetchLatestAttributeValue_ShouldParseGeoJsonAttribute() {
		String geoJson = """
				{
				  "type": "FeatureCollection",
				  "features": [
				    {
				      "type": "Feature",
				      "properties": {
				        "alteration_date": "2024-06-15T10:30:00Z"
				      }
				    }
				  ]
				}
				""";

		mockServer.expect(requestTo(containsString("sortBy=")))
				.andExpect(requestTo(containsString("count=1")))
				.andExpect(requestTo(containsString("propertyName=")))
				.andRespond(withSuccess(geoJson, org.springframework.http.MediaType.APPLICATION_JSON));

		Optional<String> result = client.fetchLatestAttributeValue(WFS_BASE_URL, TYPE_NAME, CQL_FILTER);

		assertTrue(result.isPresent());
		assertEquals("2024-06-15T10:30:00Z", result.get());
		mockServer.verify();
	}

	@Test
	void fetchLatestAttributeValue_ShouldReturnEmptyWhenNoFeatures() {
		String geoJson = """
				{
				  "type": "FeatureCollection",
				  "features": []
				}
				""";

		mockServer.expect(requestTo(containsString("area-of-interest")))
				.andRespond(withSuccess(geoJson, org.springframework.http.MediaType.APPLICATION_JSON));

		Optional<String> result = client.fetchLatestAttributeValue(WFS_BASE_URL, TYPE_NAME, CQL_FILTER);

		assertTrue(result.isEmpty());
		mockServer.verify();
	}

	@Test
	void fetchLatestAttributeValue_ShouldReturnEmptyForImpossibleFilter() {
		Optional<String> result = client.fetchLatestAttributeValue(WFS_BASE_URL, TYPE_NAME, "1=0");

		assertTrue(result.isEmpty());
		mockServer.verify();
	}
}
