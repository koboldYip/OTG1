package ru.mpei.cimmaintainer;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;
import ru.mpei.cimmaintainer.binder.ElementsBinder;
import ru.mpei.cimmaintainer.converter.SldToCimConverter;
import ru.mpei.cimmaintainer.dto.SingleLineDiagram;
import ru.mpei.cimmaintainer.mapper.JsonMapperCIM;

import java.io.FileInputStream;
import java.io.IOException;

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
        String cimUri = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
        try {
            Model results = Rio.parse(new FileInputStream("src/test/resources/asd1"),
                    "http://iec.ch/TC57/2013/CIM-schema-cim16#",
                    RDFFormat.RDFXML);
            Repository repository = new SailRepository(new MemoryStore());
            RepositoryConnection connection = repository.getConnection();
            connection.add(results);

            String queryString = "PREFIX cim: <" + cimUri + "> " +
                    "SELECT ?tId ?cnId ?ceId " +
                    "WHERE { " +
                    "    ?t a cim:Terminal ; " +
                    "       cim:IdentifiedObject.mRID ?tId ; " +
                    "       cim:Terminal.ConductingEquipment ?ce ; " +
                    "       cim:Terminal.ConnectivityNode ?cn . " +
                    "    ?cn cim:IdentifiedObject.mRID ?cnId . " +
                    "    ?cn cim:IdentifiedObject.mRID ?ceId . " +
                    "}";
            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = query.evaluate()) {
                for (BindingSet solution :
                        result) {
                    String tId = solution.getValue("tId").stringValue();
                    String cnId = solution.getValue("cnId").stringValue();
                    String ceId = solution.getValue("ceId").stringValue();
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
