package utilities.smhi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mjsaa.energy_weather_api.data.Station;
import io.github.mjsaa.energy_weather_api.data.StationResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 * Example class for the SMHI metobs API. Uses org.json for JSON parsing.
 *
 */

public class JSONParse {

    // Url for the metobs API
    @Value("${smhi.opendata.api.url}")
    static String metObsAPI = "https://opendata-download-metobs.smhi.se/api";


    /**
     * Print all available parameters.
     *
     * @return The key for the last parameter.
     * @throws IOException
     * @throws JSONException
     */
    private String getParameters() throws IOException, JSONException {

        JSONObject parameterObject = readJsonFromUrl(metObsAPI + "/version/latest.json");
        JSONArray parametersArray = parameterObject.getJSONArray("resource");

        String parameterKey = null;
        for (int i = 0; i < parametersArray.length(); i++) {

            JSONObject parameter = parametersArray.getJSONObject(i);
            parameterKey = parameter.getString("key");
            String parameterName = parameter.getString("title");
            System.out.println(parameterKey + ": " + parameterName);
        }

        return parameterKey;
    }


    /**
     * Print all available stations for the given parameter. Return the id for the last station.
     *
     * @param parameterKey The key for the wanted parameter
     * @return The id for the last station
     * @throws IOException
     * @throws JSONException
     */
    public static String getStationNames(String parameterKey) throws IOException, JSONException {

        JSONObject stationsObject = readJsonFromUrl(metObsAPI + "/version/latest/parameter/" + parameterKey + ".json");
        JSONArray stationsArray = stationsObject.getJSONArray("station");
        String stationId = null;
        for (int i = 0; i < stationsArray.length(); i++) {
            String stationName = stationsArray.getJSONObject(i).getString("name");
            stationId = stationsArray.getJSONObject(i).getString("key");
            System.out.println(stationId + ": " + stationName);
        }
        return stationId;
    }

    public static List<Station> getStations(String parameterKey) throws IOException, JSONException {

        String stationsJsonString = readStringFromUrl(
                metObsAPI + "/version/latest/parameter/" + parameterKey + ".json"
        );
        ObjectMapper mapper = new ObjectMapper();

        StationResponse stationResponse = mapper.readValue(stationsJsonString, new TypeReference<StationResponse>() {});
        return stationResponse.stations();
    }

    /**
     * Print all available periods for the given parameter and station. Return the key for the last period.
     *
     * @param parameterKey The key for the wanted parameter
     * @param stationKey The key for the wanted station
     * @return The name for the last period
     * @throws IOException
     * @throws JSONException
     */
    private String getPeriodNames(String parameterKey, String stationKey) throws IOException, JSONException {

        JSONObject periodsObject = readJsonFromUrl(metObsAPI + "/version/latest/parameter/" + parameterKey + "/station/" + stationKey + ".json");
        JSONArray periodsArray = periodsObject.getJSONArray("period");

        String periodName = null;
        for (int i = 0; i < periodsArray.length(); i++) {
            periodName = periodsArray.getJSONObject(i).getString("key");
            System.out.println(periodName);
        }

        return periodName;
    }


    /**
     * Get the data for the given parameter, station and period.
     *
     * @param parameterKey The key for the wanted parameter
     * @param stationKey The key for the wanted station
     * @param periodName The name for the wanted period
     * @return The data
     * @throws IOException
     * @throws JSONException
     */
    private String getData(String parameterKey, String stationKey, String periodName) throws IOException {
        return readStringFromUrl(
                metObsAPI
                        + "/version/latest/parameter/"
                        + parameterKey
                        + "/station/"
                        + stationKey
                        + "/period/"
                        + periodName
                        + "/data.csv");
    }


    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        String text = readStringFromUrl(url);
        return new JSONObject(text);
    }


    private static String readStringFromUrl(String url) throws IOException {

        InputStream inputStream = new URL(url).openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            StringBuilder stringBuilder = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1) {
                stringBuilder.append((char) cp);
            }
            return stringBuilder.toString();
        } finally {
            inputStream.close();
        }
    }
}
