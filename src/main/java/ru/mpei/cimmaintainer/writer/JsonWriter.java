package ru.mpei.cimmaintainer.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import ru.mpei.cimmaintainer.dto.CIM;
import ru.mpei.cimmaintainer.dto.ConnectivityNode;
import ru.mpei.cimmaintainer.dto.Element;
import ru.mpei.cimmaintainer.dto.Terminal;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Paths;

@Getter
@Setter
public class JsonWriter {

    private Model results;
    private String cimUri = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    private RepositoryConnection connection = new SailRepository(new MemoryStore()).getConnection();
    private String queryString;
    private TupleQuery query;
    private CIM all = new CIM();
    private ObjectMapper objectMapper = new ObjectMapper();


    @SneakyThrows
    public void parseCimRdfToJson(String filePath) {
        results = Rio.parse(new FileInputStream(filePath),
                cimUri,
                RDFFormat.RDFXML);
        connection.add(results);
        this.parse();
        System.out.println();
    }

    @SneakyThrows
    private JsonWriter parse() {
        queryString = "PREFIX cim: <" + cimUri + "> " +
                "SELECT ?tId ?cnId ?ceId " +
                "WHERE { " +
                "    ?t a cim:Terminal  ; " +
                "       cim:IdentifiedObject.mRID ?tId ; " +
                "       cim:Terminal.ConductingEquipment ?ce ; " +
                "       cim:Terminal.ConnectivityNode ?cn . " +
                "    ?cn cim:IdentifiedObject.mRID ?cnId . " +
                "    ?ce cim:IdentifiedObject.mRID ?ceId . " +
                "}";

        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/resources/json", false));
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution :
                    result) {
                Terminal terminal = new Terminal();
                ConnectivityNode connectivityNode = new ConnectivityNode();
                Element element = new Element();
                String tId = solution.getValue("tId").stringValue();
                String cnId = solution.getValue("cnId").stringValue();
                String ceId = solution.getValue("ceId").stringValue();
                terminal.setId(tId);
                connectivityNode.setId(cnId);
                element.setId(ceId);
                terminal.setConnectivityNode(connectivityNode);
                terminal.setElement(element);
                all.getElements().add(connectivityNode);
                all.getElements().add(element);
                all.getTerminals().add(terminal);
            }
        }
        objectMapper.writeValue(Paths.get("src/test/resources/json").toFile(), all);
        return this;
    }

}
