package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherObservation(
        @JsonProperty("date") String date, // temperature, windspeed, cloudiness common date
        @JsonProperty("temperature") double temperature, // temperture -> value -> value
        @JsonProperty("windSpeed") double windSpeed, // windspeed -> value -> value
        @JsonProperty("cloudiness") String cloudiness // cloudiness -> value -> value
) {}