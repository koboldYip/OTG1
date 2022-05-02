package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Terminal extends Identifier {

    private String name = "Terminal";
    private Element element;
    private Port port;
    private ConnectivityNode connectivityNode;

}
