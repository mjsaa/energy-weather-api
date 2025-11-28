package io.github.mjsaa.energy_weather_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mjsaa.energy_weather_api.data.*;
import io.github.mjsaa.energy_weather_api.smhi.utils.SMHIService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.shredzone.commons.suncalc.SunTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class WeatherService {
    private static final String SMHI_TEMP_PARAMETER = "39";
    private static final String SMHI_WIND_PARAMETER = "4";
    private static final String SMHI_CLOUDINESS_PARAMETER = "16";
    @Autowired
    SMHIService smhiService;
    @Autowired
    GeoClosestFinder geoClosestFinder;
    @Autowired
    PostPositionService postPositionService;
    @Autowired
    ElectricityService electricityService;
    @Value("${smhi.opendata.api.url}")
    private String metObsAPI;


    public WeatherSeries getWeatherData(String postNumber) throws IOException {
        Map<String, String> tempMap = indexByTimestamp(getDataJson(postNumber, SMHI_TEMP_PARAMETER));
        Map<String, String> windSpeedMap = indexByTimestamp(getDataJson(postNumber, SMHI_WIND_PARAMETER));
        Map<String, String> cloudinessMap = indexByTimestamp(getDataJson(postNumber, SMHI_CLOUDINESS_PARAMETER));

        Set<String> allTimestamps = new TreeSet<>();
        allTimestamps.addAll(tempMap.keySet());
        allTimestamps.addAll(windSpeedMap.keySet());
        allTimestamps.addAll(cloudinessMap.keySet());
        JSONArray result = allTimestamps.stream()
                .map(ts -> new JSONObject()
                        .put("date", convertToIsoWithOffset(ts))
                        .put("temperature", tempMap.get(ts))
                        .put("windSpeed", windSpeedMap.get(ts))
                        .put("cloudiness", cloudinessMap.get(ts)))
                .reduce(new JSONArray(), JSONArray::put, (a, b) -> a);
        ObjectMapper mapper = new ObjectMapper();
       List<WeatherObservation> weatherObservations = mapper.readValue(result.toString(), new TypeReference<>() {});
        return new WeatherSeries(weatherObservations);
    }
    public static String convertToIsoWithOffset(String ts) {
        long millis = Long.parseLong(ts);

        Instant instant = Instant.ofEpochMilli(millis);

        // Use Europe/Stockholm (or any TZ you want)
        ZoneId zone = ZoneId.of("Europe/Stockholm");

        ZonedDateTime zdt = instant.atZone(zone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        return zdt.format(formatter);
    }
    public List<CombinedWeatherElectricityData> getCombinedData(String postCode) throws IOException {
        Map<String, String> tempMap = indexByTimestamp(getDataJson(postCode, SMHI_TEMP_PARAMETER));
        Map<String, String> windSpeedMap = indexByTimestamp(getDataJson(postCode, SMHI_WIND_PARAMETER));
        Map<String, String> cloudinessMap = indexByTimestamp(getDataJson(postCode, SMHI_CLOUDINESS_PARAMETER));

        Set<String> allTimestamps = new TreeSet<>();
        allTimestamps.addAll(tempMap.keySet());
        allTimestamps.addAll(windSpeedMap.keySet());
        allTimestamps.addAll(cloudinessMap.keySet());
        JSONArray result = allTimestamps.stream()
                .map(ts -> new JSONObject()
                        .put("date", convertToIsoWithOffset(ts))
                        .put("temperature", tempMap.get(ts))
                        .put("windSpeed", windSpeedMap.get(ts))
                        .put("cloudiness", cloudinessMap.get(ts)))
                .reduce(new JSONArray(), JSONArray::put, (a, b) -> a);
        ObjectMapper mapper = new ObjectMapper();
        List<WeatherObservation> weatherObservations = mapper.readValue(result.toString(), new TypeReference<>() {});
        List<CombinedWeatherElectricityData> data = new ArrayList<>();
        weatherObservations.forEach(observation -> {
            try {
                data.add(new CombinedWeatherElectricityData(
                        observation,
                        electricityService.getElectricityPrice(
                                observation.date(),
                                electricityService.getElectricityArea(postCode)),
                        isSunUp(OffsetDateTime.parse(observation.date()).toInstant(),
                                postPositionService.getLocation(postCode))
                        ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return data;
    }

    private Map<String, String> indexByTimestamp(JSONObject jsonObject) {
        JSONArray array = jsonObject.getJSONArray("value");
        return IntStream.range(0, array.length()).mapToObj(array::getJSONObject).collect(
                Collectors.toMap(
                        entry -> String.valueOf(entry.getLong("date")),
                        entry -> entry.getString("value")
                )
        );
    }
    public JSONObject getDataJson(String postCode, String SMHIParameter) throws IOException {
        List<Station> stations = smhiService.getStations(SMHIParameter);
        Location point = postPositionService.getLocation(postCode); // a coordination within the post code
        Station closestStation = geoClosestFinder.getClosest(point, stations); // closest station location to the point
        String url = metObsAPI
                + "/version/latest/parameter/"
                + SMHIParameter
                + "/station/"
                + closestStation.key() +
                "/period/latest-day/data.json";
        try {
            String temperatureJson = smhiService.readStringFromUrl(url);
            return new JSONObject(temperatureJson);
        } catch (FileNotFoundException exception) {
            stations.remove(closestStation);
            return getDataJson(stations, postCode, SMHIParameter);
        }
    }
    public JSONObject getDataJson(List<Station> stations, String postCode, String SMHIParameter) throws IOException {

        Location point = postPositionService.getLocation(postCode); // a coordination within the post code
        Station closestStation = geoClosestFinder.getClosest(point, stations); // closest station location to the point
        String url = metObsAPI
                + "/version/latest/parameter/"
                + SMHIParameter
                + "/station/"
                + closestStation.key() +
                "/period/latest-day/data.json";
        try {
            String temperatureJson = smhiService.readStringFromUrl(url);
            return new JSONObject(temperatureJson);
        } catch (FileNotFoundException exception) {
            stations.remove(closestStation);
            return getDataJson(stations, postCode, SMHIParameter);
        }
    }

    public boolean isSunUp(Instant instant, Location location) {
        SunTimes times = SunTimes.compute()
                .on(instant.atZone(ZoneId.of("CET")).toLocalDate())
                .latitude(location.latitude())
                .longitude(location.longitude())
                .execute();

        Instant sunrise = Objects.requireNonNull(times.getRise()).toInstant();
        Instant sunset = Objects.requireNonNull(times.getSet()).toInstant();

        return instant.isAfter(sunrise) && instant.isBefore(sunset);
    }
}