package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Location(@JsonProperty("lat") double latitude, @JsonProperty("lng") double longitude){
}
