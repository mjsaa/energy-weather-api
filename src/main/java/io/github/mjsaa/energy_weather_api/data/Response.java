package io.github.mjsaa.energy_weather_api.data;

import java.util.List;

public record Response (String electricityArea, List<CombinedWeatherElectricityData> data) {
}
