package org.bahmni.reports.report;

import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.model.ProgramConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.template.BaseReportTemplate;
import org.bahmni.reports.template.ProgramStateCountTemplate;

public class ProgramStateCountReport extends Report<ProgramConfig> {
    @Override
    public BaseReportTemplate getTemplate(BahmniReportsProperties bahmniReportsProperties) {
        return new ProgramStateCountTemplate();
    }
}
