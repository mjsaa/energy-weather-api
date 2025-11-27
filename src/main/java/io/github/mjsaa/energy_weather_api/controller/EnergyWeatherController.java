package io.github.mjsaa.energy_weather_api.controller;

import io.github.mjsaa.energy_weather_api.data.Response;
import io.github.mjsaa.energy_weather_api.service.CombinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/energy-weather-data")
public class EnergyWeatherController {
    @Autowired
    CombinationService combinationService;
    @GetMapping("/combined")
    public Response getCombinedData(
            @RequestParam String postCode
    ) throws IOException {
        return combinationService.combineData(postCode);
    }
}
