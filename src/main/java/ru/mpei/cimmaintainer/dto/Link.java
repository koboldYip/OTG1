package ru.mpei.cimmaintainer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Link extends Identifier {
    private String sourceId;
    private String id;
    private String targetId;
    private String sourcePortId;
    private String targetPortId;

    private Port sourcePort;
    private Port targetPort;
    private Element source;
    private Element target;
}
