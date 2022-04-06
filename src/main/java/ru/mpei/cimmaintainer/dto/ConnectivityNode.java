package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectivityNode {

    static int count = 0;

    public ConnectivityNode() {
        this.VL = count + "Connectivity";
        count++;
    }

    private String VL;

}
