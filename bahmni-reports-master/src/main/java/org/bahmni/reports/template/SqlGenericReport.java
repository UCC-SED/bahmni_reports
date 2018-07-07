/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bahmni.reports.template;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.ctab;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.StretchType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.SqlReportConfig;
import org.bahmni.reports.report.BahmniReportBuilder;
import org.bahmni.reports.util.CommonComponents;
import static org.bahmni.reports.util.FileReaderUtil.getFileContent;
import org.stringtemplate.v4.ST;

/**
 *
 * @author ucc-ian
 */
public class SqlGenericReport extends BaseReportTemplate<SqlReportConfig> {

    @Override
    public BahmniReportBuilder build(Connection connection, JasperReportBuilder jasperReport, Report<SqlReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        String sqlString = getSqlString(report, startDate, endDate);
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlString);

            CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("indicator", String.class).setHeaderStretchWithOverflow(Boolean.TRUE)
                    .setShowTotal(Boolean.FALSE);

            CrosstabColumnGroupBuilder<String> columnGroup = ctab.columnGroup("Gender", String.class).setHeaderStretchWithOverflow(Boolean.TRUE);

            CrosstabBuilder crosstab = ctab.crosstab()
                    .headerCell(cmp.text("Indicators").setStyle(Templates.boldCenteredStyle))
                    .rowGroups(rowGroup)
                    .setStretchType(StretchType.RELATIVE_TO_TALLEST_OBJECT)
                    .columnGroups(columnGroup)
                    .setStretchType(StretchType.RELATIVE_TO_TALLEST_OBJECT)
                    .measures(ctab.measure("Total", "Total", Integer.class, Calculation.SUM),
                            ctab.measure("<1 year", "<1 year", Integer.class, Calculation.SUM),
                            ctab.measure("1-4 year", "1-4 year", Integer.class, Calculation.SUM),
                            ctab.measure("5-14 year", "5-14 year", Integer.class, Calculation.SUM),
                            ctab.measure(">=15 year", ">=15 year", Integer.class, Calculation.SUM)
                    );

            jasperReport
                    .setPageFormat(PageType.A4, PageOrientation.LANDSCAPE)
                    .setTemplate(Templates.reportTemplate)
                    .title(Templates.createTitleComponent("CustomPercentageCrosstab"))
                    .summary(crosstab)
                    .pageFooter(Templates.footerComponent)
                    //  .setDataSource(createDataSource())
                    .setDataSource(resultSet)
                    .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
            // .show();

            resources.add(statement);
        } catch (SQLException ex) {
            Logger.getLogger(SqlGenericReport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new BahmniReportBuilder(jasperReport);
    }

    public BahmniReportBuilder build_bk(Connection connection, JasperReportBuilder jasperReport, Report<SqlReportConfig> report, String startDate, String endDate, List<AutoCloseable> resources, PageType pageType) {
        CommonComponents.addTo(jasperReport, report, pageType);

        String sqlString = getSqlString(report, startDate, endDate);
        ResultSet resultSet = null;
        Statement statement = null;
        ResultSetMetaData metaData;
        int columnCount;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlString);
            metaData = resultSet.getMetaData();
            columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                jasperReport.addColumn(col.column(metaData.getColumnLabel(i), metaData.getColumnLabel(i), mapSqlDataTypeToJasperDataType(metaData.getColumnType(i))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        jasperReport.setDataSource(resultSet);
        jasperReport.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);
        resources.add(statement);
        return new BahmniReportBuilder(jasperReport);
    }

    private String getSqlString(Report<SqlReportConfig> reportConfig, String startDate, String endDate) {
        String sql = getFileContent(reportConfig.getConfig().getSqlPath(), true);
        ST sqlTemplate = new ST(sql, '#', '#');
        sqlTemplate.add("startDate", startDate);
        sqlTemplate.add("endDate", endDate);
        return sqlTemplate.render();
    }

    private DRIDataType mapSqlDataTypeToJasperDataType(int sqlType) {

        switch (sqlType) {
            case Types.BIT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return DataTypes.integerType();

            case Types.BIGINT:
                return DataTypes.longType();

            case Types.FLOAT:
                return DataTypes.floatType();
            case Types.DOUBLE:
                return DataTypes.doubleType();
            case Types.DATE:
                return DataTypes.dateType();
            default:
                return DataTypes.stringType();
        }
    }

}
