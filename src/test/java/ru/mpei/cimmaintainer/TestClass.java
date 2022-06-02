package ru.mpei.cimmaintainer;

import lombok.SneakyThrows;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import ru.mpei.cimmaintainer.binder.ElementsBinder;
import ru.mpei.cimmaintainer.converter.SldToCimConverter;
import ru.mpei.cimmaintainer.converter.SldToOWL;
import ru.mpei.cimmaintainer.dto.SingleLineDiagram;
import ru.mpei.cimmaintainer.mapper.JsonMapperCIM;
import ru.mpei.cimmaintainer.writer.JsonWriter;

public class TestClass {


    @Test
    public void test() {
        JsonMapperCIM jsonMapperCIM = new JsonMapperCIM();
        SingleLineDiagram sld = jsonMapperCIM.mapJsonToSld("src/test/resources/Viezdnoe.json");

        ElementsBinder.bind(sld);

        SldToCimConverter converter = new SldToCimConverter();
        converter.build(sld);
        converter.convert(sld);
        String cimModel = converter.getResult(RDFFormat.RDFXML);
        System.out.println();
    }

    @Test
    public void test2() {

        JsonWriter jsonWriter = new JsonWriter();
        jsonWriter.parseCimRdfToJson("src/test/resources/asd");
        System.out.println();

    }

    @SneakyThrows
    @Test
    public void test3() {

        SldToOWL sld = new SldToOWL();

        sld.process("src/test/resources/substation.owl","src/test/resources/фчс");



        System.out.println("Load ontology: ");


    }
}
