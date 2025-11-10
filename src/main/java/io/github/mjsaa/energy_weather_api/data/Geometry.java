package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Geometry (Location location) {
}
