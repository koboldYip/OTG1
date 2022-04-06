package ru.mpei.cimmaintainer.catalog;

import lombok.Getter;
import lombok.Setter;
import ru.mpei.cimmaintainer.dto.Device;
import ru.mpei.cimmaintainer.dto.VoltageLevel;
import ru.mpei.cimmaintainer.mapper.JsonMapperCIM;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class Catalog {

    JsonMapperCIM jsonMapperCIM = new JsonMapperCIM();
    List<Device> devices = jsonMapperCIM.mapJsonCatalog("src/main/resources/DeviceDirectory.json");
    List<VoltageLevel> voltageLevel = jsonMapperCIM.mapJsonVoltage("src/main/resources/VoltageLevelDirectory.json");

    Map<String, String> deviceDirectory = devices.stream()
            .collect(Collectors.toMap(Device::getId, Device::getDeviceType));

    Map<String, Map<String, String>> voltageLevelDirectory = voltageLevel.stream()
            .collect(Collectors.toMap(VoltageLevel::getDirectoryId, VoltageLevel::getValue));

}
