package io.github.mjsaa.energy_weather_api.service;

import io.github.mjsaa.energy_weather_api.data.Location;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
@Service
public class GeoClosestFinder {
    public Location getClosest(List<Location> locations, Location location) {
        GeometryFactory gf = new GeometryFactory();
        Point ref = gf.createPoint(new Coordinate(location.latitude(), location.longitude()));
        return locations.stream().min(Comparator.comparingDouble(loc -> {
            Point point = gf.createPoint(new Coordinate(loc.latitude(), loc.longitude()));
            return ref.distance(point);
        })).orElseThrow(() -> new IllegalArgumentException("The location list is empty."));
    }
}
