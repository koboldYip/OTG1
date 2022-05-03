package ru.mpei.cimmaintainer.converter;

import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import ru.mpei.cimmaintainer.catalog.Catalog;
import ru.mpei.cimmaintainer.dto.*;
import ru.mpei.cimmaintainer.writer.RdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SldToCimConverter {

    private final String cimNamespace = "http://iec.ch/TC57/2013/CIM-schema-cim16#";

    Catalog catalog = new Catalog();

    @Getter
    private ModelBuilder modelBuilder = new ModelBuilder();

    public SldToCimConverter() {
        modelBuilder
                .setNamespace(RDF.PREFIX, RDF.NAMESPACE)
                .setNamespace("cim", cimNamespace);
    }

    public void convert(SingleLineDiagram sld) {
        List<List<Element>> groupsOfConnectivityElements = groupConnectivityElementsByGraphAnalyzing(sld);
        defineCimClassForDeviceType(sld);
        groupsOfConnectivityElements.forEach(this::createTerminalsAndNodes);
        sld.getElements().forEach(this::convertElementToRdfResource);

    }

    private void createTerminalsAndNodes(List<Element> elements) {
        ConnectivityNode connectivityNode = new ConnectivityNode();
        connectivityNode.setId(UUID.randomUUID().toString());
        modelBuilder.subject("cim:" + connectivityNode.getId())
                .add("cim:IdentifiedObject.mRID", connectivityNode.getId())
                .add(RDF.TYPE, "cim:" + connectivityNode.getName())
                .add("cim:IdentifiedObject.name", connectivityNode.getName());
        if (elements.stream().anyMatch(element -> element.getType().equals("connectivity"))) {
            elements.forEach(element -> element.getPorts().forEach(port -> {
                if (port.getLinks().isEmpty()) return;
                if (port.getLink().getSource().getType().equals("connectivity") &&
                        port.getLink().getTarget().getType().equals("connectivity")) return;
                Link link = port.getLink();
                Element directory = link.getSource().getType().equals("connectivity") ?
                        link.getTarget() : link.getSource();
                if (catalog.getDeviceDirectory().get(directory.getDirectoryEntryId()) == null ||
                        !catalog.getDeviceDirectory().get(directory.getDirectoryEntryId())
                                .contains("PowerTransformer")) {
                    modelBuilder.subject("cim:" + connectivityNode.getId())
                            .add("cim:ConnectivityNode.BaseVoltage", catalog.getVoltageLevelDirectory()
                                    .get(elements.get(0).getVoltageLevel()).get("ru")
                                    .replace("кВ", ""));
                    Terminal terminal = new Terminal();
                    terminal.setId(UUID.randomUUID().toString());
                    terminal.setElement(directory);
                    terminal.setConnectivityNode(connectivityNode);
                    connectivityNode.getTerminals().add(terminal);
                    modelBuilder.subject("cim:" + terminal.getId())
                            .add("cim:IdentifiedObject.mRID", terminal.getId())
                            .add(RDF.TYPE, "cim:" + terminal.getName())
                            .add("cim:IdentifiedObject.name", terminal.getName())
                            .add("cim:Terminal.ConductingEquipment", "cim:" + directory.getId())
                            .add("cim:Terminal.ConnectivityNode", "cim:" + connectivityNode.getId());
                } else {
                    Element connectivity = !link.getSource().getType().equals("connectivity") ?
                            link.getTarget() : link.getSource();
                    List<Element> elementList = Arrays.asList(connectivity, directory);
                    transformerEndCreate(elementList, connectivityNode);
                }
            }));
        } else if (elements.stream().filter(element ->
                        element.getType().equals("directory"))
                .noneMatch(element ->
                        catalog.getDeviceDirectory().get(element.getDirectoryEntryId()).contains("PowerTransformer"))) {
            modelBuilder.subject("cim:" + connectivityNode.getId())
                    .add("cim:ConnectivityNode.BaseVoltage", catalog.getVoltageLevelDirectory()
                            .get(elements.get(0).getVoltageLevel()).get("ru").replace("кВ", ""));
            elements.forEach(element -> {
                Terminal terminal = new Terminal();
                terminal.setId(UUID.randomUUID().toString());
                terminal.setElement(element);
                terminal.setConnectivityNode(connectivityNode);
                connectivityNode.getTerminals().add(terminal);
                modelBuilder.subject("cim:" + terminal.getId())
                        .add("cim:IdentifiedObject.mRID", terminal.getId())
                        .add(RDF.TYPE, "cim:" + terminal.getName())
                        .add("cim:IdentifiedObject.name", terminal.getName())
                        .add("cim:Terminal.ConductingEquipment", "cim:" + element.getId())
                        .add("cim:Terminal.ConnectivityNode", "cim:" + connectivityNode.getId());
            });
        } else {
            transformerEndCreate(elements, connectivityNode);
        }
    }

    private void transformerEndCreate(List<Element> elements, ConnectivityNode connectivityNode) {
        ConnectivityNode connectivityNodeTREnd = new ConnectivityNode();
        connectivityNodeTREnd.setId(UUID.randomUUID().toString());

        Element powerTR;
        Element powerTREndDirectory;

        if (elements.get(0).getType().equals("connectivity")) {
            powerTR = elements.get(1);
            powerTREndDirectory = elements.get(0);
        } else {
            powerTR = catalog.getDeviceDirectory()
                    .get(elements.get(0).getDirectoryEntryId())
                    .contains("PowerTransformer") ? elements.get(0) : elements.get(1);
            powerTREndDirectory = !catalog.getDeviceDirectory()
                    .get(elements.get(0).getDirectoryEntryId())
                    .contains("PowerTransformer") ? elements.get(0) : elements.get(1);
        }

        modelBuilder.subject("cim:" + connectivityNode.getId())
                .add("cim:ConnectivityNode.BaseVoltage", catalog.getVoltageLevelDirectory()
                        .get(powerTREndDirectory.getVoltageLevel()).get("ru").replace("кВ", ""));

        modelBuilder.subject("cim:" + connectivityNodeTREnd.getId())
                .add("cim:IdentifiedObject.mRID", connectivityNodeTREnd.getId())
                .add(RDF.TYPE, "cim:" + connectivityNodeTREnd.getName())
                .add("cim:IdentifiedObject.name", connectivityNodeTREnd.getName())
                .add("cim:ConnectivityNode.BaseVoltage", catalog.getVoltageLevelDirectory()
                        .get(powerTREndDirectory.getVoltageLevel()).get("ru").replace("кВ", ""));

        Terminal terminalTR = new Terminal();
        terminalTR.setId(UUID.randomUUID().toString());
        terminalTR.setElement(powerTR);
        terminalTR.setConnectivityNode(connectivityNodeTREnd);
        connectivityNodeTREnd.getTerminals().add(terminalTR);

        modelBuilder.subject("cim:" + terminalTR.getId())
                .add("cim:IdentifiedObject.mRID", terminalTR.getId())
                .add(RDF.TYPE, "cim:" + terminalTR.getName())
                .add("cim:IdentifiedObject.name", terminalTR.getName())
                .add("cim:Terminal.ConductingEquipment", "cim:" + powerTR.getId())
                .add("cim:Terminal.ConnectivityNode", "cim:" + connectivityNodeTREnd.getId());


        Element element = new Element();
        element.setVoltageLevel(catalog.getVoltageLevelDirectory()
                .get(powerTREndDirectory.getVoltageLevel()).get("ru"));
        element.setId("PTE_" + powerTR.getProjectName() + "_" + element.getVoltageLevel());
        element.setProjectName("PowerTransformerEnd");

        modelBuilder.subject("cim:" + element.getId())
                .add("cim:TransformerEnd.BaseVoltage", "cim:" + element.getVoltageLevel()
                        .replace("кВ", ""))
                .add("cim:PowerTransformerEnd.PowerTransformer", "cim:" + powerTR.getProjectName())
                .add("cim:IdentifiedObject.mRID", element.getId())
                .add("cim:PowerTransformerEnd.ratedU", element.getVoltageLevel()
                        .replace("кВ", ""))
                .add(RDF.TYPE, "cim:" + element.getProjectName());

        Terminal terminalTREnd1 = new Terminal();
        terminalTREnd1.setId(UUID.randomUUID().toString());
        terminalTREnd1.setElement(element);
        terminalTREnd1.setConnectivityNode(connectivityNodeTREnd);
        connectivityNodeTREnd.getTerminals().add(terminalTREnd1);

        modelBuilder.subject("cim:" + terminalTREnd1.getId())
                .add("cim:IdentifiedObject.mRID", terminalTREnd1.getId())
                .add(RDF.TYPE, "cim:" + terminalTREnd1.getName())
                .add("cim:IdentifiedObject.name", terminalTREnd1.getName())
                .add("cim:Terminal.ConductingEquipment", "cim:" + element.getId())
                .add("cim:Terminal.ConnectivityNode", "cim:" + connectivityNodeTREnd.getId());


        Terminal terminalTREnd2 = new Terminal();
        terminalTREnd2.setId(UUID.randomUUID().toString());
        terminalTREnd2.setElement(powerTR);
        terminalTREnd2.setConnectivityNode(connectivityNode);
        connectivityNode.getTerminals().add(terminalTREnd2);

        modelBuilder.subject("cim:" + terminalTREnd2.getId())
                .add("cim:IdentifiedObject.mRID", terminalTREnd2.getId())
                .add(RDF.TYPE, "cim:" + terminalTREnd2.getName())
                .add("cim:IdentifiedObject.name", terminalTREnd2.getName())
                .add("cim:Terminal.ConductingEquipment", "cim:" + element.getId())
                .add("cim:Terminal.ConnectivityNode", "cim:" + connectivityNode.getId());

        if (!elements.get(0).getType().equals("connectivity")) {
            Terminal terminalDirectory = new Terminal();
            terminalDirectory.setId(UUID.randomUUID().toString());
            terminalDirectory.setElement(powerTREndDirectory);
            terminalDirectory.setConnectivityNode(connectivityNode);
            connectivityNode.getTerminals().add(terminalDirectory);

            modelBuilder.subject("cim:" + terminalDirectory.getId())
                    .add("cim:IdentifiedObject.mRID", terminalDirectory.getId())
                    .add(RDF.TYPE, "cim:" + terminalDirectory.getName())
                    .add("cim:IdentifiedObject.name", terminalDirectory.getName())
                    .add("cim:Terminal.ConductingEquipment", "cim:" + powerTREndDirectory.getId())
                    .add("cim:Terminal.ConnectivityNode", "cim:" + connectivityNode.getId());
        }
    }


    private List<List<Element>> groupConnectivityElementsByGraphAnalyzing(SingleLineDiagram sld) {
        Set<String> visitedElementIds = new HashSet<>();
        List<List<Element>> groupsOfConnectivityElements = new LinkedList<>();
        sld.getElements().stream()
                .filter(element -> element.getType().equals("connectivity"))
                .filter(element -> !visitedElementIds.contains(element.getId()))
                .forEach(element -> {
                    Deque<Element> elements = new LinkedList<>() {{
                        add(element);
                    }};
                    List<Element> groupOfConnectivityElements = new LinkedList<>();
                    walkThroughSingleLineDiagram(elements, visitedElementIds, groupOfConnectivityElements);
                    groupsOfConnectivityElements.add(groupOfConnectivityElements);
                });
        sld.getElements().stream()
                .filter(element -> element.getType().equals("bus"))
                .filter(element -> !visitedElementIds.contains(element.getId()))
                .forEach(element -> {
                    List<Element> groupOfConnectivityElements = new LinkedList<>();
                    walkThroughSingleLineDiagram(
                            element,
                            visitedElementIds,
                            groupOfConnectivityElements);
                    groupsOfConnectivityElements.add(groupOfConnectivityElements);
                });
        sld.getElements().stream()
                .filter(element -> element.getType().equals("directory"))
                .filter(element -> !visitedElementIds.contains(element.getId()))
                .forEach(element ->
                        walkThroughSingleLineDiagramElements(
                                element,
                                visitedElementIds,
                                groupsOfConnectivityElements));
        return groupsOfConnectivityElements;
    }

    private void walkThroughSingleLineDiagramElements(
            Element element,
            Set<String> visitedElementIds,
            List<List<Element>> groupsOfConnectivityElements) {
        visitedElementIds.add(element.getId());
        element.getPorts().forEach(port -> {
            Link link = port.getLink();
            if (link == null) return;
            Element sibling = link.getSourcePortId().equals(port.getId()) ? link.getTarget() : link.getSource();
            if ("directory".equals(sibling.getType()) && !visitedElementIds.contains(sibling.getId())) {
                List<Element> groupOfConnectivityElements = new LinkedList<>();
                groupOfConnectivityElements.add(element);
                groupOfConnectivityElements.add(sibling);
                groupsOfConnectivityElements.add(groupOfConnectivityElements);
            }
        });
    }

    private void walkThroughSingleLineDiagram(
            Element element,
            Set<String> visitedElementIds,
            List<Element> groupOfConnectivityElements) {
        visitedElementIds.add(element.getId());
        groupOfConnectivityElements.add(element);
        element.getPorts().forEach(port -> {
            Link link = port.getLink();
            if (link == null) return;
            Element sibling = link.getSourcePortId().equals(port.getId()) ? link.getTarget() : link.getSource();
            if (!visitedElementIds.contains(sibling.getId())) {
                groupOfConnectivityElements.add(sibling);
            }
        });
    }

    private void walkThroughSingleLineDiagram(
            Deque<Element> elements,
            Set<String> visitedElementIds,
            List<Element> groupOfConnectivityElements) {
        Element connectivity = elements.pop();
        visitedElementIds.add(connectivity.getId());
        groupOfConnectivityElements.add(connectivity);
        connectivity.getPorts().forEach(port -> {
            Link link = port.getLink();
            if (link == null) return;
            Element sibling = link.getSourcePortId().equals(port.getId()) ? link.getTarget() : link.getSource();

            if ("connectivity".equals(sibling.getType()) && !visitedElementIds.contains(sibling.getId())) {
                elements.add(sibling);
                walkThroughSingleLineDiagram(elements, visitedElementIds, groupOfConnectivityElements);
            }
        });
    }


    public void build(SingleLineDiagram sld) {

        List<String> levels = sld.getElements().stream()
                .map(Element::getVoltageLevel)
                .distinct()
                .collect(Collectors.toList());
        for (String level :
                levels) {
            Element element = new Element();
            element.setId(level);
            element.setVoltageLevel(level);
            element.setOperationName("BaseVoltage");
            element.setProjectName("BaseVoltage");
            modelBuilder.subject("cim:" + catalog.getVoltageLevelDirectory()
                            .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                    .add("cim:IdentifiedObject.mRID", catalog.getVoltageLevelDirectory()
                            .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                    .add(RDF.TYPE, "cim:" + element.getProjectName())
                    .add("cim:IdentifiedObject.name", catalog.getVoltageLevelDirectory()
                            .get(element.getVoltageLevel()).get("ru"))
                    .add("cim:BaseVoltage.nominalVoltage", catalog.getVoltageLevelDirectory()
                            .get(element.getVoltageLevel()).get("ru").replace("кВ", ""));
        }
        List<Integer> lev = levels.stream().map(Integer::parseInt)
                .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (Integer Voltage :
                lev) {
            Element element = new Element();
            element.setVoltageLevel(Voltage.toString());
            element.setId("vl_" + catalog.getVoltageLevelDirectory().get(element.getVoltageLevel())
                    .get("ru").replace("кВ", ""));
            element.setOperationName("VoltageLevel");
            element.setProjectName("VoltageLevel");
            modelBuilder.subject("cim:" + element.getId())
                    .add("cim:IdentifiedObject.mRID", element.getId())
                    .add("cim:VoltageLevel.BaseVoltage", "cim:" + catalog.getVoltageLevelDirectory()
                            .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                    .add(RDF.TYPE, "cim:" + element.getProjectName());
        }
    }

    private void defineCimClassForDeviceType(SingleLineDiagram sld) {
        sld.getElements().stream()
                .filter(element -> !element.getType().equals("connectivity"))
                .forEach(this::replaceDeviceType);
    }

    private void replaceDeviceType(Element element) {
        if (catalog.getDeviceDirectory().get(element.getDirectoryEntryId()) != null) {
            element.setCIMType(catalog.getDeviceDirectory().get(element.getDirectoryEntryId()));
            switch (element.getCIMType()) {
                case "OverheadTransmissionLine":
                case "CableTransmissionLine":
                    element.setCIMType("ACLineSegment");
                    break;
                case "WaveTrap":
                    element.setCIMType("WaveTrap");
                    break;
                case "GroundDisconnector":
                    element.setCIMType("GroundDisconnector");
                    break;
                case "Disconnector":
                    element.setCIMType("Disconnector");
                    break;
                case "SurgeArrester":
                    element.setCIMType("SurgeArrester");
                    break;
                case "RfFilter":
                case "CouplingCapacitor":
                case "Breaker":
                case "ThreePhaseCurrentTransformer":
                case "IndoorCircuitBreaker":
                case "ModularSwitchboardWithFuse":
                case "SinglePhaseCurrentTransformer":
                case "ModularSwitchboard":
                case "FourWindingVoltageTransformer":
                    element.setCIMType("Breaker");
                    break;
                case "ThreeWindingPowerTransformerWithTapChanger":
                    element.setCIMType("PowerTransformer");
                    break;
                default:
                    throw new RuntimeException("Unexpected device type: " + element.getCIMType());
            }
        }
    }


    private void convertElementToRdfResource(Element element) {
        if (element.getType().contains("connectivity")) return;
        modelBuilder
                .subject("cim:" + element.getId())
                .add("cim:IdentifiedObject.mRID", element.getId())
                .add("cim:ConductingEquipment.BaseVoltage", "cim:" + catalog.getVoltageLevelDirectory()
                        .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                .add("cim:Equipment.EquipmentContainer", "cim:" + "vl_" + catalog.getVoltageLevelDirectory()
                        .get(element.getVoltageLevel()).get("ru").replace("кВ", ""));
        if (element.getType().equals("directory")) {
            modelBuilder
                    .add(RDF.TYPE, "cim:".concat(element.getCIMType()));
            if (catalog.getDeviceDirectory().get(element.getDirectoryEntryId()).contains("PowerTransformer")) {
                modelBuilder.add("cim:ApparentPower", element
                        .getFields()
                        .stream()
                        .filter(e -> e.containsKey("name"))
                        .filter(e -> e.get("name").equals("ApparentPower"))
                        .map(e -> e.get("value"))
                        .collect(Collectors.joining()));
            }
        } else {
            modelBuilder
                    .add(RDF.TYPE, "cim:".concat(element.getType()));
        }

        if (element.getProjectName() != null)
            modelBuilder.add("cim:IdentifiedObject.name", element.getProjectName());
    }


    public String getResult(RDFFormat rdfFormat) {
        Model model = modelBuilder.build();

        if (rdfFormat.equals(RDFFormat.RDFXML)) {
            RdfWriter rdfWriter = new RdfWriter();
            return rdfWriter.writeXml(model);
        } else {
            OutputStream out = null;
            String cim;
            try {
                File tempFile = File.createTempFile("file", ".txt");
                out = new FileOutputStream(tempFile);
                Rio.write(model, out, cimNamespace, rdfFormat);
                cim = Files.readString(Path.of(tempFile.getPath()));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return cim;
        }
    }

}
