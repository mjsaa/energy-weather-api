package io.github.mjsaa.energy_weather_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.github.mjsaa.energy_weather_api.data.ElectricityArea;
import io.github.mjsaa.energy_weather_api.smhi.utils.SMHIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;


import java.io.IOException;
@Validated
@Service
public class ElectricityService {
    @Value("${el.zone.from.post.code}")
    private String elprisetUrl;
    public String getElectricityArea(@Pattern(regexp = "\\d{5}", message = "Postal code must be exactly 5 digits")
                                     String postCode) throws IOException {
        var jsonString = SMHIService.readStringFromUrl(elprisetUrl + postCode);

        ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
        ElectricityArea electricityArea = mapper.readValue(jsonString, ElectricityArea.class);
        return electricityArea.zone().orElse("Post code " + postCode + " could not retrieve a zone code");
    }
}
