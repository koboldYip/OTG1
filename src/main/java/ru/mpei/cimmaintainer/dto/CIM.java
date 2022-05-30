package ru.mpei.cimmaintainer.dto;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class CIM {

    private List<Terminal> terminals = new LinkedList<>();
    private List<Identifier> elements = new LinkedList<>();

}
