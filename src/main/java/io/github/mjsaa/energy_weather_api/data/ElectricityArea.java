package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
public record ElectricityArea(@NonNull @JsonProperty("status") String status,
                              @NonNull@JsonProperty("msg") String msg,
                              @JsonProperty("zone") Optional<String> zone) {
}
