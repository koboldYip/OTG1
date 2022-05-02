package ru.mpei.cimmaintainer;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import ru.mpei.cimmaintainer.binder.ElementsBinder;
import ru.mpei.cimmaintainer.converter.SldToCimConverter;
import ru.mpei.cimmaintainer.dto.SingleLineDiagram;
import ru.mpei.cimmaintainer.mapper.JsonMapperCIM;

public class TestClass {

    @Test
    public void test1() {
        JsonMapperCIM jsonMapperCIM = new JsonMapperCIM();
        SingleLineDiagram sld = jsonMapperCIM.mapJsonToSld("src/test/resources/Viezdnoe.json");

        ElementsBinder.bind(sld);

        SldToCimConverter converter = new SldToCimConverter();
        converter.build(sld);
        converter.convert(sld);
        String cimModel = converter.getResult(RDFFormat.RDFXML);
        System.out.println();
    }
}
