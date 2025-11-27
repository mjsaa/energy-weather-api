package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public record ElectricityPrice(
        @JsonProperty("SEK_per_kWh") String priceInSEK,
        @JsonProperty("time_start") String time) {}
