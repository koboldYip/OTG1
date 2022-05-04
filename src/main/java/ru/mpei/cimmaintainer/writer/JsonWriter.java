package ru.mpei.cimmaintainer.writer;

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
import ru.mpei.cimmaintainer.dto.ConnectivityNode;
import ru.mpei.cimmaintainer.dto.Element;
import ru.mpei.cimmaintainer.dto.Terminal;

import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class JsonWriter {

    private Model results;
    private String cimUri = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    private RepositoryConnection connection = new SailRepository(new MemoryStore()).getConnection();
    private String queryString;
    private TupleQuery query;
    private List<Terminal> terminals;
    private List<ConnectivityNode> connectivityNodes;
    private List<Element> elements;

    @SneakyThrows
    public void parseCimRdfToJson(String filePath) {
        results = Rio.parse(new FileInputStream(filePath),
                cimUri,
                RDFFormat.RDFXML);
        terminals = new LinkedList<>();
        connectivityNodes = new LinkedList<>();
        elements = new LinkedList<>();

        connection.add(results);
//        this.parseConnectivityNodes()
//                .parseBus()
//                .parseTerminals()
//                this.parseTerminals();
        this.parse();
        System.out.println();
    }

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
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution :
                    result) {
                Terminal terminal = new Terminal();
                ConnectivityNode connectivityNode = new ConnectivityNode();
                Element element = new Element();
//                String t = solution.getValue("t").stringValue();
                String tId = solution.getValue("tId").stringValue();
                String cnId = solution.getValue("cnId").stringValue();
                String ceId = solution.getValue("ceId").stringValue();
                terminal.setId(tId);
                connectivityNode.setId(cnId);
                element.setId(ceId);
                terminal.setConnectivityNode(connectivityNode);
                terminal.setElement(element);
                terminals.add(terminal);
                connectivityNodes.add(connectivityNode);
                elements.add(element);
                System.out.println();
            }
        }
        return this;
    }

    private JsonWriter parseTerminals() {
        queryString = "PREFIX cim: <" + cimUri + "> " +
                "SELECT ?tId ?cnId ?ceId " +
                "WHERE { " +
                "    ?t a cim:Terminal ; " +
                "       cim:IdentifiedObject.mRID ?tId ; " +
                "       cim:Terminal.ConductingEquipment ?ce ; " +
                "       cim:Terminal.ConnectivityNode ?cn . " +
                "    ?cn cim:IdentifiedObject.mRID ?cnId . " +
                "    ?ce cim:IdentifiedObject.mRID ?ceId . " +
                "}";

        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution :
                    result) {
                String tId = solution.getValue("tId").stringValue();
                String cnId = solution.getValue("cnId").stringValue();
                String ceId = solution.getValue("ceId").stringValue();
                System.out.println("tId = " + tId);
                System.out.println("cnId = " + cnId);
                System.out.println("ceId = " + ceId);
                System.out.println();
            }
        }
        return this;
    }

    private JsonWriter parseBus() {
        queryString = "PREFIX cim: <" + cimUri + "> " +
                "SELECT ?tId ?cnId ?ceId " +
                "WHERE { " +
                "    ?t a cim:bus ; " +
                "       cim:IdentifiedObject.mRID ?tId ; " +
                "       cim:ConductingEquipment.BaseVoltage ?ce ; " +
                "       cim:Equipment.EquipmentContainer ?cn . " +
                "    ?cn cim:IdentifiedObject.mRID ?cnId . " +
                "    ?ce cim:IdentifiedObject.mRID ?ceId . " +
                "}";

        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution :
                    result) {
                String tId = solution.getValue("tId").stringValue();
                String cnId = solution.getValue("cnId").stringValue();
                String ceId = solution.getValue("ceId").stringValue();
                System.out.println("tId = " + tId);
                System.out.println("cnId = " + cnId);
                System.out.println("ceId = " + ceId);
            }
        }
        return this;
    }

    private JsonWriter parseConnectivityNodes() {
        queryString = "PREFIX cim: <" + cimUri + "> " +
                "SELECT ?tId ?ceId " +
                "WHERE { " +
                "    ?t a cim:ConnectivityNode ; " +
                "       cim:IdentifiedObject.mRID ?tId ; " +
                "       cim:ConnectivityNode.BaseVoltage ?ceId . " +
                "}";

        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution :
                    result) {
                String tId = solution.getValue("tId").stringValue();
                String ceId = solution.getValue("ceId").stringValue();
                System.out.println("tId = " + tId);
                System.out.println("ceId = " + ceId);
            }
        }
        return this;
    }

    private JsonWriter parseBrakers() {
        String queryString = "PREFIX cim: <" + cimUri + "> " +
                "SELECT ?tId ?cnId ?ceId " +
                "WHERE { " +
                "    ?t a cim:Breaker ; " +
                "       cim:IdentifiedObject.mRID ?tId ; " +
                "       cim:ConductingEquipment.BaseVoltage ?ce ; " +
                "       cim:Equipment.EquipmentContainer ?cn . " +
                "    ?cn cim:IdentifiedObject.mRID ?cnId . " +
                "    ?ce cim:IdentifiedObject.mRID ?ceId . " +
                "}";

        query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        try (TupleQueryResult result = query.evaluate()) {
            for (BindingSet solution :
                    result) {
                String tId = solution.getValue("tId").stringValue();
                String cnId = solution.getValue("cnId").stringValue();
                String ceId = solution.getValue("ceId").stringValue();
                System.out.println("tId = " + tId);
                System.out.println("cnId = " + cnId);
                System.out.println("ceId = " + ceId);
                System.out.println();
            }
        }
        return this;
    }

}
