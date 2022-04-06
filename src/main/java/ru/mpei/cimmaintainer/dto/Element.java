package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Element extends Identifier {

    private String directoryEntryId;
    private String voltageLevel;
    private String operationName;
    private String type;
    private String projectName;
    private List<Map<String, String>> fields;
    private List<Port> ports;

}
