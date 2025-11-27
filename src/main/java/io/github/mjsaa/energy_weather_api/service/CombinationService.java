package io.github.mjsaa.energy_weather_api.service;

import io.github.mjsaa.energy_weather_api.data.CombinedWeatherElectricityData;
import io.github.mjsaa.energy_weather_api.data.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CombinationService {
    @Autowired
    WeatherService weatherService;
    @Autowired
    ElectricityService electricityService;
    public Response combineData(String postCode) throws IOException {
        String electricityArea = electricityService.getElectricityArea(postCode);
        List<CombinedWeatherElectricityData> data = weatherService.getCombinedData(postCode);
        return new Response(electricityArea, data);
    }
}
