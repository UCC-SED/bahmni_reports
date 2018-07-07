/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bahmni.reports.report;

/**
 *
 * @author ucc-ian
 */
import org.bahmni.reports.template.*;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.AggregationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;


public class CohortReport extends Report<AggregationReportConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new CohortReportTemplate(bahmniReportsProperties);
    }
}
