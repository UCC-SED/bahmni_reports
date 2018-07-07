package org.bahmni.reports.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.webclients.HttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reports extends HashMap<String, Report> {

     private static final Logger logger = Logger.getLogger(Reports.class);
    public static  Report find(String reportName, String reportConfigUrl,HttpClient httpClient) throws IOException, URISyntaxException {
        logger.info("reportConfigUrl" +  reportConfigUrl);
        logger.info("reportName" +  reportName);

        String data=httpClient.get(new URI(reportConfigUrl));
        ObjectMapper mapper=new ObjectMapper();
        Reports reports=mapper.readValue(data,Reports.class);
        for (Report report : reports.values()) {
            if (reportName.equals(report.getName())) {
                logger.info("report" +  "true");
                return report;
            }
        }
        logger.info("report" +  "false");
        return null;
    }
}
