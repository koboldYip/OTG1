package ru.mpei.cimmaintainer;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import ru.mpei.cimmaintainer.converter.SldToCimConverter;
import ru.mpei.cimmaintainer.dto.SingleLineDiagram;
import ru.mpei.cimmaintainer.mapper.JsonMapperCIM;

public class TestClass {

    @Test
    public void test() {
        JsonMapperCIM jsonMapperCIM = new JsonMapperCIM();
        SingleLineDiagram sld = jsonMapperCIM.mapJsonToSld("src/test/resources/Viezdnoe.json");

        SldToCimConverter converter = new SldToCimConverter();
        converter.build(sld);
        converter.convert(sld);
        String cimModel = converter.getResult(RDFFormat.RDFXML);
        System.out.println();
    }

}
