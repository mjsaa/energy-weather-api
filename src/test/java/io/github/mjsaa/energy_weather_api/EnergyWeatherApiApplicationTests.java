package io.github.mjsaa.energy_weather_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.github.mjsaa.energy_weather_api.data.*;
import io.github.mjsaa.energy_weather_api.service.*;
import jakarta.validation.ConstraintViolationException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.mjsaa.energy_weather_api.smhi.utils.SMHIService;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EnergyWeatherApiApplicationTests {
	private static final double GEO_TOLERANCE = 1e-4;

	@Autowired
	ElectricityService electricityService;
	@Autowired
	PostPositionService postalPositionService;
	@Autowired
	GeoClosestFinder geoClosestFinder;
	@Autowired
	WeatherService weatherService;
	@Autowired
    SMHIService SMHIService;

	@Autowired
	CombinationService combinationService;

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
	void testGetClosest() throws IOException {
		// Given
		// List of stations
		// Retrieve locations
		try (InputStream inputStream = getClass().getResourceAsStream(
				"/stations.json"
		)) {
			ObjectMapper mapper = new ObjectMapper();
			List<Station> stations = mapper.readValue(inputStream, StationResponse.class).stations();
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
		String postCode = "13443";
		// When
		WeatherSeries weatherSeries = weatherService.getWeatherData(postCode);
		// Then
		assertNotNull(weatherSeries);
		assertTrue(weatherSeries.observations().getFirst().temperature() > -50
				&& weatherSeries.observations().getFirst().temperature() < 50); // Assume that it never reaches ±50
	}

	@Test
	void testGetCombinedData() throws IOException {
		Response response = combinationService.combineData("13443");
		String area = response.electricityArea();
		assertEquals(area, "3");
	}

	@Test
	void testGetElectricityAreaCode() throws IOException {
		String RegularPostCode = "13443";
		String wrongPostCode = "wrong";
		String tooShortCode = "1";
		String tooLongCode = "9999999";
		String electricityArea = electricityService.getElectricityArea(RegularPostCode);
		assertEquals(electricityArea, "3");
		assertThrows(ConstraintViolationException.class,
				() -> electricityService.getElectricityArea(wrongPostCode));
		assertThrows(ConstraintViolationException.class,
				() -> electricityService.getElectricityArea(tooLongCode));
		assertThrows(ConstraintViolationException.class,
				() -> electricityService.getElectricityArea(tooShortCode));
	}

	// this will start failing in November 2030
	@Test
	void testGetElectricityPrice() throws IOException {
		// Given
		String timestamp = "2025-11-27T00:00:00+01:00";
		String areaCode = "3";
		String expected = "0.60104";
		// When
		String actual = electricityService.getElectricityPrice(timestamp, areaCode);
		// Then
		assertEquals(expected, actual);
	}

	@Test
	void testIsSunUp() {
		Location location = new Location(59.314778, 18.406528); // Gustavsberg
		String nightTimestamp = "1764198000000"; // 2025-11-25 00:00
		String dayTimestamp = "1764241200000"; // 2025-11-25 12:00
		Instant day = Instant.ofEpochMilli(Long.parseLong(dayTimestamp));
		Instant night = Instant.ofEpochMilli(Long.parseLong(nightTimestamp));
        assertTrue(weatherService.isSunUp(day, location));
        assertFalse(weatherService.isSunUp(night, location));
	}
}
