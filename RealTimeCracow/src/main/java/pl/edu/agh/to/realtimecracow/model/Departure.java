package pl.edu.agh.to.realtimecracow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Departure {
    private String stopName;
    private String lineNumber;
    private String departureTime;
}
