package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record Station(
        String key,
        String title,
        String summary,
        String name,
        String owner,
        String id,
        String height,
        String latitude,
        String longitude,
        boolean active
) {
    public Station {
        if (latitude == null
         || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude cannot be null for station");
        }
    }
}
