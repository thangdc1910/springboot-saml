package com.example.SpringBootSaml.Controller;

import com.example.SpringBootSaml.Model.City;
import com.example.SpringBootSaml.Service.ICityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    ICityService iCityService;

    @RequestMapping("/")
    public String home(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal, Model model) {
        model.addAttribute("name", principal.getName());
        model.addAttribute("emailAddress", principal.getFirstAttribute("email"));
        model.addAttribute("userAttributes", principal.getAttributes());

        return "home";
    }

    @RequestMapping("/cityList")
    public String cityList(Model model) {
        List<City> cityList = iCityService.findAll().getBody();
        model.addAttribute("cityList", cityList);

        return "cityList";
    }

    @RequestMapping("/addCity")
    public String showAddCityPage(Model  model) {

        City cityForm = new City();
        model.addAttribute("cityForm", cityForm);
        return "addCity";
    }

    @PostMapping(value = {"/addCity"})
    public String savePerson(Model model, //
                             @ModelAttribute("cityForm") City cityForm) {

        iCityService.saveCity(cityForm);

        return "redirect:/cityList";
    }

    @RequestMapping("/test/test1")
    public String getTest1(){
        return "test1";
    }

    @RequestMapping("/test/test2")
    public String getTest2(){
        return "test2";
    }

    @RequestMapping("/test/test1/test2")
    public String getTest3(){
        return "test2";
    }
}