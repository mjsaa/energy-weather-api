package io.github.mjsaa.energy_weather_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.mjsaa.energy_weather_api.data.GoogleResponse;
import io.github.mjsaa.energy_weather_api.data.Location;
import org.springframework.stereotype.Service;
import utilities.smhi.JSONParse;

import java.io.IOException;

@Service
public class PostPositionService {
    public Location getLocation(String postCode) throws IOException {
        // call API
        // Do not use in production environment. Use for example AWS Secret Manager instead.
        Dotenv dotenv = Dotenv.load();
        String googleApiKey = dotenv.get("GOOGLE_API_KEY");
        String googleApiUrl = dotenv.get("GOOGLE_API_URL")
                + "?address=" + postCode + "%20Sweden"
                + "&key=" + googleApiKey;
        var json = JSONParse.readStringFromUrl(googleApiUrl);
        ObjectMapper mapper = new ObjectMapper();
        GoogleResponse response = mapper.readValue(json, GoogleResponse.class);
        return response.results().getFirst().geometry().location();
    }
}
