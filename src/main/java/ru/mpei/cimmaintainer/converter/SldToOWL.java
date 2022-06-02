package ru.mpei.cimmaintainer.converter;

import lombok.Data;
import lombok.SneakyThrows;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
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

        ns = ontology.getOntologyID().getOntologyIRI().get().toString() + "#";
        dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        OWLClass DZT1 = dataFactory.getOWLClass(IRI.create(ns + "DZT"));

        OWLNamedIndividual owlNamedIndividual = dataFactory.getOWLNamedIndividual(IRI.create(ns + "DZT1"));
        OWLAxiom classDeclaration = dataFactory.getOWLClassAssertionAxiom(DZT1, owlNamedIndividual);
        AddAxiom addAxiom = new AddAxiom(ontology, classDeclaration);
        manager.applyChange(addAxiom);

        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);

        SWRLAPIRule DZT = ruleEngine.createSWRLRule("DZT", "#ThreeWindingPowerTransformerWithTapChanger(?x) ^ " +
                "#hasApparentPower(?x,?b) ^ swrlb:greaterThanOrEqual(?b,4.0) -> #hasProtection(?x, #DZT)");

        ruleEngine.infer();
        manager.saveOntology(ontology, new FileOutputStream(toPath));
    }

}
