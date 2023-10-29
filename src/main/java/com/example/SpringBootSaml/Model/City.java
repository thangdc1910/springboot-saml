package com.example.SpringBootSaml.Model;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class City {
    private Long id;
    private String name;
    private Integer population;
}
