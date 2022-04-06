package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Terminal {

    public Terminal() {
        this.connectivityNode = new ConnectivityNode();
    }

    private ConnectivityNode connectivityNode;

}
