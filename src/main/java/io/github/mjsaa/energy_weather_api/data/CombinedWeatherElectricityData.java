package io.github.mjsaa.energy_weather_api.data;

public record CombinedWeatherElectricityData(WeatherObservation observation,
                                             String electricityPrice,
                                             boolean isSunUp) {
}
