package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ConnectivityNode {

    private List<String> resources = new ArrayList<>();
    private List<String> devices = new ArrayList<>();

}
