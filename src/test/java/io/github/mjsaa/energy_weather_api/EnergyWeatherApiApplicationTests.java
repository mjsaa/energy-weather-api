package io.github.mjsaa.energy_weather_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.github.mjsaa.energy_weather_api.data.Location;
import io.github.mjsaa.energy_weather_api.data.Station;
import io.github.mjsaa.energy_weather_api.data.StationResponse;
import io.github.mjsaa.energy_weather_api.service.GeoClosestFinder;
import io.github.mjsaa.energy_weather_api.service.PostPositionService;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import utilities.smhi.JSONParse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EnergyWeatherApiApplicationTests {
	@Autowired
	PostPositionService postalPositionService;
	@Autowired
	GeoClosestFinder geoClosestFinder;
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
		double expectedLat = 55.3837;
		double expectedLon = 12.8167;

		boolean exists = stations.stream()
				.anyMatch(
						s ->
								Double.compare(expectedLat, s.latitude()) == 0 &&
										Double.compare(expectedLon, s.longitude()) == 0
				);
		// Then
		assertTrue(exists, "Expected station with given latitude and longitude to exist, but it does not");
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

	@Test
	void testGetLocation() throws IOException {
		// Given lat and lon for postal code 13443 (Gustavsberg, Sweden)
		String postCode = "13443";
		double expectedLat = 59.31477;
		double expectedLon = 18.406534;
		// When
		Location actual = postalPositionService.getLocation(postCode);
		// Assert
		assertNotNull(actual);
		assertEquals(expectedLat, actual.latitude(),  0.0001);
		assertEquals(expectedLon, actual.longitude(),  0.0001);

	}

	@Test
	void testGetClosest() throws JSONException, IOException {
		// Given
		// List of stations
		// Retrieve locations
		List<Location> locations = JSONParse.getStations("1").stream()
				.map(station -> new Location(station.latitude(), station.longitude())).toList();
		// A given location retrieved from a post code
		Location location = postalPositionService.getLocation("13443");
		Location expectedClosestSMHIStation = new Location(59.3226, 18.3725);
		// When
		Location actualClosestSMHIStation = geoClosestFinder.getClosest(locations, location);
		// Then
		assertEquals(expectedClosestSMHIStation, actualClosestSMHIStation, "This is not the expected closest station to this post number!");
	}
}
