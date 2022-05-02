package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ConnectivityNode extends Identifier {

    private String name = "ConnectivityNode";
    private String voltageLevel;
    private List<Terminal> terminals = new LinkedList<>();
}
