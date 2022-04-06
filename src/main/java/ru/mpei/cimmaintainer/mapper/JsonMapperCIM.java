package ru.mpei.cimmaintainer.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.mpei.cimmaintainer.dto.Device;
import ru.mpei.cimmaintainer.dto.SingleLineDiagram;
import ru.mpei.cimmaintainer.dto.VoltageLevel;

import java.io.File;
import java.util.List;

public class JsonMapperCIM {

    private ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @SneakyThrows
    public SingleLineDiagram mapJsonToSld(String filePath) {
        return objectMapper.readValue(
                new File(filePath), SingleLineDiagram.class
        );
    }

    @SneakyThrows
    public List<Device> mapJsonCatalog(String filePath) {
        return objectMapper.readValue(
                new File(filePath), new TypeReference<List<Device>>() {
                }
        );
    }

    @SneakyThrows
    public List<VoltageLevel> mapJsonVoltage(String filePath) {
        return objectMapper.readValue(
                new File(filePath), new TypeReference<List<VoltageLevel>>() {
                }
        );
    }


}
