package ru.mpei.cimmaintainer.dto;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Port extends Identifier {

    private String name;
    private List<String> links;
    private ArrayNode fields;

}
