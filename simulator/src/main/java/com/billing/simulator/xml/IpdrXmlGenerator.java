package com.billing.simulator.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.billing.simulator.config.SimulatorConfig;
import com.billing.simulator.model.GeneratedIpdrSession;

public class IpdrXmlGenerator {

    private final SimulatorConfig config;
    private final DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;

    public IpdrXmlGenerator(SimulatorConfig config) {
        this.config = config;
    }

    public File writeIpdrXml(GeneratedIpdrSession session) throws IOException {
        Path outDir = Paths.get(config.getOutputDirectory());
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }

        String fileName = String.format("IPDR_%s_%s.xml",
                session.getSessionEnd().format(fileFormatter),
                session.getServiceIdentifier());
        File xmlFile = outDir.resolve(fileName).toFile();

        String docId = UUID.randomUUID().toString().toUpperCase();
        String creationTime = session.getSessionEnd().format(isoFormatter);

        long packets = Math.max(1, session.getOctetsPassed() / 1460);

        String xmlContent = String.format("""
<?xml version="1.0" encoding="UTF-8"?>
<IPDRDoc xmlns="http://www.ipdr.org/namespaces/ipdr"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="DOCSIS-3.1-B.0.xsd"
         docId="%s"
         creationTime="%s"
         IPDRRecorderInfo="standalone-simulator-v1"
         version="3.1">
    <IPDR xsi:type="DOCSIS-Type">
        <IPDRcreationTime>%s</IPDRcreationTime>
        <CMTSHostName>%s</CMTSHostName>
        <CMTSipAddress>%s</CMTSipAddress>
        <CMTSsysUpTime>61878200</CMTSsysUpTime>
        <CMTScatvIfName>Cable5/0</CMTScatvIfName>
        <CMTScatvIfIndex>9</CMTScatvIfIndex>
        <CMTSupIfName>Ca5/0-upstream3</CMTSupIfName>
        <CMTSupIfType>129</CMTSupIfType>
        <CMTSdownIfName>Ca5/0-downstream</CMTSdownIfName>
        <CMmacAddress>%s</CMmacAddress>
        <CMipAddress>%s</CMipAddress>
        <CMdocsisMode>10</CMdocsisMode>
        <CMCPEipAddress></CMCPEipAddress>
        <RecType>1</RecType>
        <serviceIdentifier>%s</serviceIdentifier>
        <serviceClassName>BroadbandStandard</serviceClassName>
        <serviceDirection>%d</serviceDirection>
        <serviceOctetsPassed>%d</serviceOctetsPassed>
        <servicePktsPassed>%d</servicePktsPassed>
        <serviceSlaDropPkts>0</serviceSlaDropPkts>
        <serviceSlaDelayPkts>874</serviceSlaDelayPkts>
        <serviceTimeCreated>1850600</serviceTimeCreated>
        <serviceTimeActive>600274</serviceTimeActive>
    </IPDR>
</IPDRDoc>
""",
                docId,
                creationTime,
                creationTime,
                session.getHostname(),
                session.getCmtsIp(),
                session.getCmMac(),
                session.getClientIp(),
                session.getServiceIdentifier(),
                session.getDirection(),
                session.getOctetsPassed(),
                packets
        );

        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xmlContent.trim() + System.lineSeparator());
        }

        return xmlFile;
    }
}
