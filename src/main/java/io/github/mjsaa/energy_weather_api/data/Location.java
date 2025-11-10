package io.github.mjsaa.energy_weather_api.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Location(@JsonProperty("lat") double latitude, @JsonProperty("lng") double longitude){
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Location location = (Location) object;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0;
    }
    @Override
    public int hashCode() {
        return java.util.Objects.hash(latitude, longitude);
    }
}
