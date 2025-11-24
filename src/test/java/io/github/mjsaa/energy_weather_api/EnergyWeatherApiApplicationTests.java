package io.github.mjsaa.energy_weather_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.github.mjsaa.energy_weather_api.data.*;
import io.github.mjsaa.energy_weather_api.service.GeoClosestFinder;
import io.github.mjsaa.energy_weather_api.service.PostPositionService;
import io.github.mjsaa.energy_weather_api.service.WeatherService;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.mjsaa.energy_weather_api.smhi.utils.SMHIService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EnergyWeatherApiApplicationTests {
	private static final double GEO_TOLERANCE = 1e-4;

	@Autowired
	PostPositionService postalPositionService;
	@Autowired
	GeoClosestFinder geoClosestFinder;
	@Autowired
	WeatherService weatherService;
	@Autowired
    SMHIService SMHIService;

	@Test
	void testStationListLength() throws JSONException, IOException {
		// Given

		// When
		// Call SMHI API to retrieve station list

		List<Station> stations = SMHIService.getStations("1");
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
		List<Station> stations = SMHIService.getStations("1");
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
		assertEquals(expectedLat, actual.latitude(),  GEO_TOLERANCE);
		assertEquals(expectedLon, actual.longitude(),  GEO_TOLERANCE);

	}

	@Test
	void testGetClosest() throws JSONException, IOException {
		// Given
		// List of stations
		// Retrieve locations
		try (InputStream inputStream = getClass().getResourceAsStream(
				"/stations.json"
		)) {
			ObjectMapper mapper = new ObjectMapper();
			List<Station> stations = mapper.readValue(inputStream, StationResponse.class).stations();
			List<Location> locations = stations.stream()
					.map(station -> new Location(station.latitude(), station.longitude())).toList();
			// A given location
			Location location = new Location(59.31477,18.4065336);
			Location expectedClosestSMHIStation = new Location(59.3226, 18.3725);

			// When
			Station actualClosestSMHIStation = geoClosestFinder.getClosest(location, stations);

			// Then
			assertEquals(
					expectedClosestSMHIStation.latitude(),
					actualClosestSMHIStation.latitude(),
					GEO_TOLERANCE);
			assertEquals(
					expectedClosestSMHIStation.longitude(),
					actualClosestSMHIStation.longitude(),
					GEO_TOLERANCE
			);
		}
	}

	@Test
	void testGetWeatherData() throws IOException {
		// Given
		String postNumber = "13443";
		// When
		WeatherSeries weatherSeries = weatherService.getWeatherData(postNumber);
		// Then
		assertNotNull(weatherSeries);
		assertTrue(weatherSeries.observations().getFirst().temperature() > -50
				&& weatherSeries.observations().getFirst().temperature() < 50); // Assume that it never reaches ±50
	}
}
