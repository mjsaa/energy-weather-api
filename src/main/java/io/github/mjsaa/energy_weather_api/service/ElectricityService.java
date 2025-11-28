package io.github.mjsaa.energy_weather_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.github.mjsaa.energy_weather_api.data.ElectricityArea;
import io.github.mjsaa.energy_weather_api.data.ElectricityPrice;
import io.github.mjsaa.energy_weather_api.smhi.utils.SMHIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;


import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@Service
public class ElectricityService {
    @Value("${el.zone.from.post.code}")
    private String elZoneUrl;
    @Value("${el.price}")
    private String elPrice;

    @Autowired
    SMHIService smhiService;

    public String getElectricityArea(@Pattern(regexp = "\\d{5}", message = "Postal code must be exactly 5 digits")
                                     String postCode) throws IOException {
        var jsonString = smhiService.readStringFromUrl(elZoneUrl + postCode);

        ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
        ElectricityArea electricityArea = mapper.readValue(jsonString, ElectricityArea.class);
        return electricityArea.zone().orElse("Post code " + postCode + " could not retrieve a zone code");
    }


    public String getElectricityPrice(String timestamp, String areaCode) throws IOException {
        String url = buildUrl(timestamp, areaCode);
        String response = smhiService.readStringFromUrl(url);
        ObjectMapper mapper = new ObjectMapper();
        List<ElectricityPrice> prices = mapper.readValue(response, new TypeReference<>() {
        });
        Map<String, String> timePriceMap = new HashMap<>();
        prices.forEach(p -> timePriceMap.put(p.time(), p.priceInSEK()));
//        String isoTime = unixToISOTime(Long.parseLong(timestamp));
//        return timePriceMap.get(isoTime);
        return timePriceMap.get(timestamp);
    }
    public String unixToISOTime(long unixMillis) {
        return Instant.ofEpochMilli(unixMillis)
                .atZone(ZoneId.of("Europe/Stockholm"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    private String buildUrl(String timestamp, String areaCode) {
        return elPrice +
                OffsetDateTime.parse(timestamp).getYear() +
                "/" +
                OffsetDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("MM-dd")) +
                "_SE" +
                areaCode +
                ".json";
    }

    public int getYearFromUnix(long unixMillis) {
        return Instant.ofEpochMilli(unixMillis)
                .atZone(ZoneId.systemDefault())
                .getYear();
    }
    public String getMonthDayFromUnix(long unixMillis) {
        return Instant.ofEpochMilli(unixMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MM-dd"));
    }


}
