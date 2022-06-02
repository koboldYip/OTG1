package ru.mpei.cimmaintainer.converter;

import lombok.Data;
import lombok.SneakyThrows;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;

import java.io.File;
import java.io.FileOutputStream;

@Data
public class SldToOWL {

    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private String ns;
    private OWLDataFactory dataFactory;
    private OWLReasonerFactory reasonerFactory;
    private OWLReasoner reasoner;

    @SneakyThrows
    public void process(String fromPath, String toPath) {

        manager = OWLManager.createOWLOntologyManager();
        File file2 = new File(fromPath);
        ontology = manager.loadOntologyFromOntologyDocument(file2);
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        SWRLAPIRule DZT = ruleEngine.createSWRLRule("DZT", "#ThreeWindingPowerTransformerWithTapChanger(?x) ^ " +
                "#hasApparentPower(?x,?b) ^ #isApparentPowerof(?a,?b) ^ #ApparentPower(?b) ^ swrlb:greaterThan(?b,4) ^ swrlb:lessThan(?b,160) -> #DZT(?x)");

        ruleEngine.infer();
        manager.saveOntology(ontology, new FileOutputStream(toPath));
    }

}
