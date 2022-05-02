package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Port extends Identifier {

    private String name;
    private List<String> links = new LinkedList<>();
    private List<Map<String, String>> fields;

    private Link link;
    private Element element;

}
