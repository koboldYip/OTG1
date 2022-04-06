package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Terminal {

    static int count = 0;

    public Terminal(Element element) {
        this.id = element.getId() + "Terminal";
        this.resource = element;
    }

    private String id;
    private ConnectivityNode connectivityNode;
    private Element resource;

}
