package com.example.SpringBootSaml.RestController;

import com.example.SpringBootSaml.Model.City;
import com.example.SpringBootSaml.Service.CityService;
import com.example.SpringBootSaml.Service.ICityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CityController {

    @Autowired
    ICityService cityService;

    @GetMapping(value = "/cities")
    public ResponseEntity<List<City>> getCities() {

        return cityService.findAll();
    }

    @GetMapping(value = "/cities/{id}")
    public ResponseEntity<City> getCity(@PathVariable("id") int id) {

        return cityService.findById(id);
    }

    @PostMapping(value = "/cities/saveCity")
    public ResponseEntity<List<City>> saveCity(@RequestBody City city){
        cityService.saveCity(city);

        return cityService.findAll();
    }

}
