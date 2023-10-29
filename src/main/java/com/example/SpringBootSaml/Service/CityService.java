package com.example.SpringBootSaml.Service;

import com.example.SpringBootSaml.Model.City;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CityService implements ICityService {

    private static List<City> cities = new ArrayList<>();

    static {
        cities.add(new City(1L, "Bratislava", 432000));
        cities.add(new City(2L, "Budapest", 1759000));
        cities.add(new City(3L, "Prague", 1280000));
        cities.add(new City(4L, "Warsaw", 1748000));
        cities.add(new City(5L, "Los Angeles", 3971000));
        cities.add(new City(6L, "New York", 8550000));
        cities.add(new City(7L, "Edinburgh", 464000));
        cities.add(new City(8L, "Berlin", 3671000));
    }

    @Override
    public ResponseEntity<List<City>> findAll() {
        return ResponseEntity.ok(cities);
    }

    @Override
    public ResponseEntity<City> findById(int id) {
        Optional<City> cityOptional = cities.stream().filter(c -> c.getId() == id).findFirst();

        if (cityOptional.isPresent()) {
            return ResponseEntity.ok(cityOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public void saveCity(City city) {
        cities.add(city);
    }

    @Override
    public void test() {
        //do nothing
    }
}
