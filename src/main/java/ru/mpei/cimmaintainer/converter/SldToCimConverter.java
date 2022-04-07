package ru.mpei.cimmaintainer.converter;

import lombok.Getter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import ru.mpei.cimmaintainer.catalog.Catalog;
import ru.mpei.cimmaintainer.dto.Element;
import ru.mpei.cimmaintainer.dto.Link;
import ru.mpei.cimmaintainer.dto.Port;
import ru.mpei.cimmaintainer.dto.SingleLineDiagram;
import ru.mpei.cimmaintainer.writer.RdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
        sld.getElements().forEach(this::convertElementToRdfResource);
    }

    public void connectApply(SingleLineDiagram sld) {
        List<Element> elements = sld.getElements();
        Map<String, Link> links = sld.getLinks().stream().collect(Collectors.toMap(Link::getId, Function.identity()));

        elements = elements.stream().filter(element -> !element.getType().equals("connectivity"))
                .collect(Collectors.toList());
        for (Element e :
                elements) {
            boolean flag = true;
            while (flag) {
                List<Port> portsE = e.getPorts().stream().filter(port -> !e.getConnectedPorts().contains(port))
                        .collect(Collectors.toList());
                if (portsE.isEmpty()) {
                    break;
                }
                Port portSource = portsE.get(0);
                Link linkSource = links.get(portSource.getLinks().get(0));

            }
        }

    }

    public void build(SingleLineDiagram sld) {
        List<String> levelWires = Arrays.asList("HV", "MV", "LV");
        List<Element> transformers = sld.getElements().stream()
                .filter(element -> element.getType().equals("directory"))
                .filter(e -> catalog.getDeviceDirectory().get(e.getDirectoryEntryId()).contains("PowerTransformer"))
                .collect(Collectors.toList());
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
        for (Element tr :
                transformers) {
            for (Integer volt :
                    lev) {
                Element element = new Element();
                element.setVoltageLevel(volt.toString());
                element.setId("PTE_" + tr.getProjectName() + "_" + levelWires.get(lev.indexOf(volt)));
                element.setProjectName("PowerTransformerEnd");
                modelBuilder.subject("cim:" + element.getId())
                        .add("cim:TransformerEnd.BaseVoltage", "cim:" + catalog.getVoltageLevelDirectory()
                                .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                        .add("cim:PowerTransformerEnd.PowerTransformer", "cim:" + tr.getProjectName())
                        .add("cim:TransformerEnd.endNumber", lev.indexOf(volt))
                        .add("cim:IdentifiedObject.mRID", element.getId())
                        .add("cim:PowerTransformerEnd.ratedU", catalog.getVoltageLevelDirectory()
                                .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                        .add(RDF.TYPE, "cim:" + element.getProjectName());
            }
        }

    }


    private void convertElementToRdfResource(Element element) {
        modelBuilder
                .subject("cim:" + element.getId())
                .add("cim:IdentifiedObject.mRID", element.getId())
                .add("cim:ConductingEquipment.BaseVoltage", "cim:" + catalog.getVoltageLevelDirectory()
                        .get(element.getVoltageLevel()).get("ru").replace("кВ", ""))
                .add("cim:Equipment.EquipmentContainer", "cim:" + "vl_" + catalog.getVoltageLevelDirectory()
                        .get(element.getVoltageLevel()).get("ru").replace("кВ", ""));
        if (element.getType().equals("directory")) {
            modelBuilder
                    .add(RDF.TYPE, "cim:".concat(catalog.getDeviceDirectory().get(element.getDirectoryEntryId())));
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
