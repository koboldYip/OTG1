package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class VoltageLevel {

    private String directoryId;
    private Map<String, String> value;

}
