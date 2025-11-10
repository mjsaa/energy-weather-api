package io.github.mjsaa.energy_weather_api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.github.mjsaa.energy_weather_api.data.Station;
import io.github.mjsaa.energy_weather_api.data.StationResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import utilities.smhi.JSONParse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EnergyWeatherApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testStationListLength() throws JSONException, IOException {
		// Given

		// When
		// Call SMHI API to retrieve station list
		List<Station> stations = JSONParse.getStations("1");
		int expectedNumberOfStations = 991;
		// Then
		// Assert that it has expected number of stations
		assertEquals(
				expectedNumberOfStations,
				stations.size(),
				"List should contain exactly "
						+ expectedNumberOfStations
						+ " items."
		);
	}

	@Test
	void testPositionOfARandomStation() throws JSONException, IOException {
		// Given

		// When
		List<Station> stations = JSONParse.getStations("1");
		Double expectedLat = 55.3837;
		Double expectedLon = 12.8167;

		boolean exists = stations.stream()
				.anyMatch(
						s ->
								expectedLat.toString().equals(s.latitude()) &&
										expectedLon.toString().equals(s.longitude())
				);
		// Then
		assertTrue(exists, "Expected station with given latitude and longitude to exist");
	}

	@Test
	void testIllegalArgumentExceptionIfMissedLonAndLat() throws IOException {
		// Given
        try (InputStream inputStream = getClass().getResourceAsStream(
                "/testIllegalArgumentExceptionIfMissedLonAndLat.json"
        )) {
            ObjectMapper mapper = new ObjectMapper();

            Throwable throwable = assertThrows(
					ValueInstantiationException.class,
					() -> mapper.readValue(inputStream, StationResponse.class
					)
            );
			Throwable cause = throwable.getCause();
			String msg = cause.getMessage();

			assertEquals(IllegalArgumentException.class, cause.getClass());
			assertEquals(msg, "Latitude and longitude cannot be null for station");
        }
    }

}
