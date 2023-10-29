package com.example.SpringBootSaml.Service;

import com.example.SpringBootSaml.Model.City;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ICityService {
    ResponseEntity<List<City>> findAll();
    ResponseEntity<City> findById(int id);
    void saveCity(City city);

    void test();
}
