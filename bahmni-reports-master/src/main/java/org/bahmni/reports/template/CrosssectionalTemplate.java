package org.bahmni.reports.template;

import java.io.FileInputStream;
import java.io.InputStream;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.expression.AddExpression;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.*;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericLabOrderDaoImpl;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.dao.impl.GenericProgramDaoImpl;
import org.bahmni.reports.dao.impl.GenericVisitDaoImpl;
import org.bahmni.reports.model.AggregationReportConfig;
import org.bahmni.reports.model.GenericReportsConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.log4j.Logger;
import static org.bahmni.reports.model.Constants.*;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import org.bahmni.reports.util.SqlUtil;
import org.stringtemplate.v4.ST;

@UsingDatasource("openmrs")
public class CrosssectionalTemplate extends BaseReportTemplate<AggregationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;
    private static final Logger logger = Logger.getLogger(CrosssectionalTemplate.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public CrosssectionalTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
            Report<AggregationReportConfig> aggregateReport,
            String startDate, String endDate, List<AutoCloseable> resources,
            PageType pageType) throws Exception {
        logger.info("sqlPath " + aggregateReport.getConfig().getSqlPath());
        InputStream is = new FileInputStream(aggregateReport.getConfig().getSqlPath());

        try {

            JasperReportBuilder jasperReportBuilder = report()
                    .setTemplate(Templates.reportTemplate)
                    .setTemplateDesign(is)
                    .setParameter("start_date", startDate)
                    .setParameter("end_date", endDate)
                    .setParameter("CTCID", "123REF12")
                    .setParameter("HFRID", "321323")
                    .setParameter("TypeOfService", "CTC")
                    .setParameter("reportingPeriod", sdf.parse(endDate))
                    .setParameter("dateCompletion", sdf.parse(endDate))
                    .setParameter("faciltyName", "Mirembe Hospital")
                    .setParameter("council", "Mirembe")
                    .setParameter("personReporting", "Ian Manyama")
                    .setParameter("region", "Dodoma")
                    .setParameter("designation", "Reporter")
                    .setDataSource("select * from users limit 1", connection);
            return new BahmniReportBuilder(jasperReportBuilder);
        } catch (DRException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSqlString(Report<AggregationReportConfig> reportConfig, String startDate, String endDate) {
        String sql = getFileContent(reportConfig.getConfig().getSqlPath(), true);
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        logger.info("SQL " + sqlTemplate.render());
        return sqlTemplate.render();
    }

    private void addRowGroup(AggregationReportConfig config, List<String> rowGroups, CrosstabBuilder crosstab) {
        for (String row : rowGroups) {
            if (applyAgeGroup(row, config)) {
                createAgeGroupRows(row, config, crosstab);
            } else {
                CrosstabRowGroupBuilder rowGroup = ctab.rowGroup(row, Object.class)
                        .setShowTotal(config.getShowTotalRow());
                crosstab.rowGroups(rowGroup);
            }
        }
    }

    private void addColumnGroup(AggregationReportConfig config, List<String> columnGroups, CrosstabBuilder crosstab) {
        if (CollectionUtils.isEmpty(columnGroups)) {
            crosstab.columnGroups(ctab.columnGroup(new AddExpression())
                    .setShowTotal(config.getShowTotalColumn()).setHeaderStyle(stl.style(Templates.columnStyle)));
            return;
        }
        for (String column : columnGroups) {
            if (applyAgeGroup(column, config)) {
                createAgeGroupColumns(column, config, crosstab);
            } else {
                CrosstabColumnGroupBuilder columnGroup = ctab.columnGroup(column, Object.class)
                        .setShowTotal(config.getShowTotalColumn());
                crosstab.columnGroups(columnGroup);
            }
        }
    }

    private void createAgeGroupColumns(String column, AggregationReportConfig config, CrosstabBuilder crosstab) {
        CrosstabColumnGroupBuilder<Integer> sortOrderGroup = ctab.columnGroup("Age Group Order", Integer.class)
                .setShowTotal(config.getShowTotalColumn())
                .setHeaderStyle(stl.style().setFontSize(0))
                .setOrderType(OrderType.ASCENDING);
        CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup(column, String.class)
                .setShowTotal(false);
        crosstab.columnGroups(sortOrderGroup, columnGroup);
    }

    private void createAgeGroupRows(String row, AggregationReportConfig config, CrosstabBuilder crosstab) {
        CrosstabRowGroupBuilder<Integer> sortOrderGroup = ctab.rowGroup("Age Group Order", Integer.class)
                .setShowTotal(config.getShowTotalRow())
                .setHeaderWidth(0)
                .setOrderType(OrderType.ASCENDING);
        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup(row, String.class)
                .setShowTotal(false);
        crosstab.rowGroups(sortOrderGroup, rowGroup);
    }

    private boolean applyAgeGroup(String row, AggregationReportConfig config) {
        if (config.getReport().getConfig() == null) {
            return false;
        }
        GenericReportsConfig genericReportsConfig = (GenericReportsConfig) config.getReport().getConfig();
        return row.equals(genericReportsConfig.getAgeGroupName());
    }

    private GenericDao getReportToAggregate(Report<AggregationReportConfig> aggregateReport) {
        Report report = aggregateReport.getConfig().getReport();
        report.setHttpClient(aggregateReport.getHttpClient());
        switch (aggregateReport.getConfig().getReport().getType()) {
            case OBSERVAIONS:
                return new GenericObservationDaoImpl(report, bahmniReportsProperties);
            case VISITS:
                return new GenericVisitDaoImpl(report);
            case PROGRAMS:
                return new GenericProgramDaoImpl(report);
            case LABORDERS:
                return new GenericLabOrderDaoImpl(report, bahmniReportsProperties);
            default:
                return null;

        }
    }

}
