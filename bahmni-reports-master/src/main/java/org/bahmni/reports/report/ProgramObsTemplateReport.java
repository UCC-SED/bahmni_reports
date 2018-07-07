package org.bahmni.reports.report;


import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ProgramObsTemplateConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ProgramObsTemplate;

public class ProgramObsTemplateReport extends Report<ProgramObsTemplateConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ProgramObsTemplate(bahmniReportsProperties);
    }
}
