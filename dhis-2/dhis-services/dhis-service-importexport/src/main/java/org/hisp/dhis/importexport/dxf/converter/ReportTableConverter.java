package org.hisp.dhis.importexport.dxf.converter;

/*
 * Copyright (c) 2004-2010, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroupSet;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.importexport.importer.ReportTableImporter;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.RelativePeriods;
import org.hisp.dhis.reporttable.ReportParams;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.reporttable.ReportTableService;

/**
 * @author Lars Helge Overland
 */
public class ReportTableConverter
    extends ReportTableImporter implements XMLConverter
{
    public static final String COLLECTION_NAME = "reportTables";
    public static final String ELEMENT_NAME = "reportTable";

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_TABLE_NAME = "tableName";
    private static final String FIELD_EXISTING_TABLE_NAME = "existingTableName";
    private static final String FIELD_MODE = "mode";
    private static final String FIELD_REGRESSION = "regression";
    
    private static final String FIELD_DATA_ELEMENTS = "dataElements";
    private static final String FIELD_INDICATORS = "indicators";
    private static final String FIELD_DATASETS = "dataSets";
    private static final String FIELD_PERIODS = "periods";
    private static final String FIELD_ORGANISATION_UNITS = "organisationUnits";
    
    private static final String FIELD_DIMENSION_TYPE = "dimensionType";
    private static final String FIELD_CATEGORY_COMBO = "categoryCombo";
    private static final String FIELD_DATA_ELEMENT_GROUP_SETS = "dataElementGroupSets";
    
    private static final String FIELD_DO_INDICATORS = "doIndicators";
    private static final String FIELD_DO_PERIODS = "doPeriods";
    private static final String FIELD_DO_ORGANISATION_UNITS = "doOrganisationUnits";
    
    private static final String FIELD_REPORTING_MONTH = "reportingMonth";
    private static final String FIELD_LAST_3_MONTHS = "last3Months";
    private static final String FIELD_LAST_6_MONTHS = "last6Months";
    private static final String FIELD_LAST_12_MONTHS = "last12Months";
    private static final String FIELD_SO_FAR_THIS_YEAR = "soFarThisYear";
    private static final String FIELD_LAST_3_TO_6_MONTHS = "last3To6Months";
    private static final String FIELD_LAST_6_TO_9_MONTHS = "last6To9Months";
    private static final String FIELD_LAST_9_TO_12_MONTHS = "last9To12Months";
    private static final String FIELD_LAST_12_INDIVIDUAL_MONHTS = "last12IndividualMonths";
    private static final String FIELD_INDIVIDUAL_MONTHS_THIS_YEAR = "individualMonthsThisYear";
    private static final String FIELD_INDIVIDUAL_QUARTERS_THIS_YEAR = "individualQuartersThisYear";
    
    private static final String FIELD_PARAM_REPORTING_MONTH = "paramReportingMonth";
    private static final String FIELD_PARAM_PARENT_ORG_UNIT = "paramParentOrganisationUnit";
    private static final String FIELD_PARAM_ORG_UNIT = "paramOrganisationUnit";
        
    private Map<Object, Integer> dataElementMapping;
    private Map<Object, Integer> dataElementGroupSetMapping;
    private Map<Object, Integer> categoryComboMapping;
    private Map<Object, Integer> indicatorMapping;
    private Map<Object, Integer> dataSetMapping;
    private Map<Object, Integer> periodMapping;
    private Map<Object, Integer> organisationUnitMapping;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     * 
     * @param reportTableService ReportTableService
     */
    public ReportTableConverter( ReportTableService reportTableService )
    {   
        this.reportTableService = reportTableService;
    }

    /**
     * Constructor for read operations.
     * 
     * @param reportTableService ReportTableService
     * @param importObjectService ImportObjectService
     * @param dataElementService DataElementService
     * @param categoryService CategoryService
     * @param indicatorService IndicatorService
     * @param dataSetService DataSetService
     * @param periodService PeriodService
     * @param organisationUnitService OrganisationUnitService
     * @param dataElementMapping
     * @param dataElementGroupSetMapping
     * @param categoryComboMapping
     * @param indicatorMapping
     * @param dataSetMapping
     * @param periodMapping
     * @param organisationUnitMapping
     */
    public ReportTableConverter( ReportTableService reportTableService,
        ImportObjectService importObjectService,
        DataElementService dataElementService,
        DataElementCategoryService categoryService,
        IndicatorService indicatorService,
        DataSetService dataSetService,
        PeriodService periodService,
        OrganisationUnitService organisationUnitService,
        Map<Object, Integer> dataElementMapping,
        Map<Object, Integer> dataElementGroupSetMapping,
        Map<Object, Integer> categoryComboMapping,
        Map<Object, Integer> indicatorMapping,
        Map<Object, Integer> dataSetMapping,
        Map<Object, Integer> periodMapping,
        Map<Object, Integer> organisationUnitMapping )
    {
        this.reportTableService = reportTableService;
        this.importObjectService = importObjectService;
        this.dataElementService = dataElementService;
        this.categoryService = categoryService;
        this.indicatorService = indicatorService;
        this.dataSetService = dataSetService;
        this.periodService = periodService;
        this.organisationUnitService = organisationUnitService;
        this.dataElementMapping = dataElementMapping;
        this.dataElementGroupSetMapping = dataElementGroupSetMapping;
        this.categoryComboMapping = categoryComboMapping;
        this.indicatorMapping = indicatorMapping;
        this.dataSetMapping = dataSetMapping;
        this.periodMapping = periodMapping;
        this.organisationUnitMapping = organisationUnitMapping;
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------

    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<ReportTable> reportTables = reportTableService.getReportTables( params.getReportTables() );
        
        if ( reportTables != null && reportTables.size() > 0 )
        {
            writer.openElement( COLLECTION_NAME );
            
            for ( ReportTable reportTable : reportTables )
            {
                writer.openElement( ELEMENT_NAME );
                
                writer.writeElement( FIELD_ID, String.valueOf( reportTable.getId() ) );
                writer.writeElement( FIELD_NAME, reportTable.getName() );
                writer.writeElement( FIELD_TABLE_NAME, reportTable.getTableName() );
                writer.writeElement( FIELD_EXISTING_TABLE_NAME, reportTable.getExistingTableName() );
                writer.writeElement( FIELD_MODE, reportTable.getMode() );
                writer.writeElement( FIELD_REGRESSION, String.valueOf( reportTable.isRegression() ) );
                
                writer.openElement( FIELD_DATA_ELEMENTS );
                for ( DataElement dataElement : reportTable.getDataElements() )
                {
                    writer.writeElement( FIELD_ID, String.valueOf( dataElement.getId() ) );
                }                    
                writer.closeElement();
                
                writer.openElement( FIELD_INDICATORS );
                for ( Indicator indicator : reportTable.getIndicators() )
                {
                    writer.writeElement( FIELD_ID, String.valueOf( indicator.getId() ) );
                }
                writer.closeElement();
                
                writer.openElement( FIELD_DATASETS );
                for ( DataSet dataSet : reportTable.getDataSets() )
                {
                    writer.writeElement( FIELD_ID, String.valueOf( dataSet.getId() ) );
                }
                writer.closeElement();  
                
                writer.openElement( FIELD_PERIODS );
                for ( Period period : reportTable.getPeriods() )
                {
                    writer.writeElement( FIELD_ID, String.valueOf( period.getId() ) );
                }
                writer.closeElement();
                
                writer.openElement( FIELD_ORGANISATION_UNITS );
                for ( OrganisationUnit unit : reportTable.getUnits() )
                {
                    writer.writeElement( FIELD_ID, String.valueOf( unit.getId() ) );
                }
                writer.closeElement();
                
                writer.writeElement( FIELD_DIMENSION_TYPE, reportTable.getDimensionType() != null ? reportTable.getDimensionType() : EMPTY );
                writer.writeElement( FIELD_CATEGORY_COMBO, reportTable.getCategoryCombo() != null ? String.valueOf( reportTable.getCategoryCombo().getId() ) : EMPTY );
                
                writer.openElement( FIELD_DATA_ELEMENT_GROUP_SETS );
                for ( DataElementGroupSet groupSet : reportTable.getDataElementGroupSets() )
                {
                    writer.writeElement( FIELD_ID, String.valueOf( groupSet.getId() ) );
                }
                writer.closeElement();
                
                writer.writeElement( FIELD_DO_INDICATORS, String.valueOf( reportTable.isDoIndicators() ) );
                writer.writeElement( FIELD_DO_PERIODS, String.valueOf( reportTable.isDoPeriods() ) );
                writer.writeElement( FIELD_DO_ORGANISATION_UNITS, String.valueOf( reportTable.isDoUnits() ) );
                
                writer.writeElement( FIELD_REPORTING_MONTH, String.valueOf( reportTable.getRelatives().isReportingMonth() ) );
                writer.writeElement( FIELD_LAST_3_MONTHS, String.valueOf( reportTable.getRelatives().isLast3Months() ) );
                writer.writeElement( FIELD_LAST_6_MONTHS, String.valueOf( reportTable.getRelatives().isLast6Months() ) );
                writer.writeElement( FIELD_LAST_12_MONTHS, String.valueOf( reportTable.getRelatives().isLast12Months() ) );
                writer.writeElement( FIELD_SO_FAR_THIS_YEAR, String.valueOf( reportTable.getRelatives().isSoFarThisYear() ) );
                writer.writeElement( FIELD_LAST_3_TO_6_MONTHS, String.valueOf( reportTable.getRelatives().isLast3To6Months() ) );
                writer.writeElement( FIELD_LAST_6_TO_9_MONTHS, String.valueOf( reportTable.getRelatives().isLast6To9Months() ) );
                writer.writeElement( FIELD_LAST_9_TO_12_MONTHS, String.valueOf( reportTable.getRelatives().isLast9To12Months() ) );
                writer.writeElement( FIELD_LAST_12_INDIVIDUAL_MONHTS, String.valueOf( reportTable.getRelatives().isLast12IndividualMonths() ) );
                writer.writeElement( FIELD_INDIVIDUAL_MONTHS_THIS_YEAR, String.valueOf( reportTable.getRelatives().isIndividualMonthsThisYear() ) );
                writer.writeElement( FIELD_INDIVIDUAL_QUARTERS_THIS_YEAR, String.valueOf( reportTable.getRelatives().isIndividualQuartersThisYear() ) );
                
                writer.writeElement( FIELD_PARAM_REPORTING_MONTH, String.valueOf( reportTable.getReportParams().isParamReportingMonth() ) );
                writer.writeElement( FIELD_PARAM_PARENT_ORG_UNIT, String.valueOf( reportTable.getReportParams().isParamParentOrganisationUnit() ) );
                writer.writeElement( FIELD_PARAM_ORG_UNIT, String.valueOf( reportTable.getReportParams().isParamOrganisationUnit() ) ); 
                
                writer.closeElement();
            }
            
            writer.closeElement();
        }
    }
    
    public void read( XMLReader reader, ImportParams params )
    {        
        while ( reader.moveToStartElement( ELEMENT_NAME, COLLECTION_NAME ) )
        {
            final ReportTable reportTable = new ReportTable();
            
            final RelativePeriods relatives = new RelativePeriods();
            reportTable.setRelatives( relatives );
            
            final ReportParams reportParams = new ReportParams();
            reportTable.setReportParams( reportParams );
            
            reader.moveToStartElement( FIELD_ID );
            reportTable.setId( Integer.parseInt( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_NAME );
            reportTable.setName( reader.getElementValue() );

            reader.moveToStartElement( FIELD_TABLE_NAME );
            reportTable.setTableName( reader.getElementValue() );

            reader.moveToStartElement( FIELD_EXISTING_TABLE_NAME );
            reportTable.setExistingTableName( reader.getElementValue() );

            reader.moveToStartElement( FIELD_MODE );
            reportTable.setMode( reader.getElementValue() );

            reader.moveToStartElement( FIELD_REGRESSION );
            reportTable.setRegression( Boolean.parseBoolean( reader.getElementValue() ) );

            while ( reader.moveToStartElement( FIELD_ID, FIELD_DATA_ELEMENTS ) )
            {
                int id = Integer.parseInt( reader.getElementValue() );
                reportTable.getDataElements().add( dataElementService.getDataElement( dataElementMapping.get( id ) ) );
            }

            while ( reader.moveToStartElement( FIELD_ID, FIELD_INDICATORS ) )
            {
                int id = Integer.parseInt( reader.getElementValue() );
                reportTable.getIndicators().add( indicatorService.getIndicator( indicatorMapping.get( id ) ) );
            }

            while ( reader.moveToStartElement( FIELD_ID, FIELD_DATASETS ) )
            {
                int id = Integer.parseInt( reader.getElementValue() );
                reportTable.getDataSets().add( dataSetService.getDataSet( dataSetMapping.get( id ) ) );
            }

            while ( reader.moveToStartElement( FIELD_ID, FIELD_PERIODS ) )
            {
                int id = Integer.parseInt( reader.getElementValue() );
                reportTable.getPeriods().add( periodService.getPeriod( periodMapping.get( id ) ) );
            }

            while ( reader.moveToStartElement( FIELD_ID, FIELD_ORGANISATION_UNITS ) )
            {
                int id = Integer.parseInt( reader.getElementValue() );
                reportTable.getUnits().add( organisationUnitService.getOrganisationUnit( organisationUnitMapping.get( id ) ) );
            }
            
            reader.moveToStartElement( FIELD_DIMENSION_TYPE );
            reportTable.setDimensionType( reader.getElementValue() );
            
            reader.moveToStartElement( FIELD_CATEGORY_COMBO );
            String cc = reader.getElementValue();
            reportTable.setCategoryCombo( cc != null ? categoryService.getDataElementCategoryCombo( categoryComboMapping.get( Integer.parseInt( cc ) ) ) : null );

            List<DataElementGroupSet> groupSets = new ArrayList<DataElementGroupSet>();
            while ( reader.moveToStartElement( FIELD_ID, FIELD_DATA_ELEMENT_GROUP_SETS ) )
            {
                int id = Integer.parseInt( reader.getElementValue() );
                groupSets.add( dataElementService.getDataElementGroupSet( dataElementGroupSetMapping.get( id ) ) );
            }
            reportTable.setDataElementGroupSets( groupSets );
            
            reader.moveToStartElement( FIELD_DO_INDICATORS );
            reportTable.setDoIndicators( Boolean.parseBoolean( reader.getElementValue() ) );

            reader.moveToStartElement( FIELD_DO_PERIODS );
            reportTable.setDoPeriods( Boolean.parseBoolean( reader.getElementValue() ) );

            reader.moveToStartElement( FIELD_DO_ORGANISATION_UNITS );
            reportTable.setDoUnits( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_REPORTING_MONTH );          
            reportTable.getRelatives().setReportingMonth( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_3_MONTHS );
            reportTable.getRelatives().setLast3Months( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_6_MONTHS );
            reportTable.getRelatives().setLast6Months( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_12_MONTHS );
            reportTable.getRelatives().setLast12Months( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_SO_FAR_THIS_YEAR );
            reportTable.getRelatives().setSoFarThisYear( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_3_TO_6_MONTHS );
            reportTable.getRelatives().setLast3To6Months( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_6_TO_9_MONTHS );
            reportTable.getRelatives().setLast6To9Months( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_9_TO_12_MONTHS );
            reportTable.getRelatives().setLast9To12Months( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_LAST_12_INDIVIDUAL_MONHTS );
            reportTable.getRelatives().setLast12IndividualMonths( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_INDIVIDUAL_MONTHS_THIS_YEAR );
            reportTable.getRelatives().setIndividualMonthsThisYear( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_INDIVIDUAL_QUARTERS_THIS_YEAR );
            reportTable.getRelatives().setIndividualQuartersThisYear( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_PARAM_REPORTING_MONTH );
            reportTable.getReportParams().setParamReportingMonth( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_PARAM_PARENT_ORG_UNIT );
            reportTable.getReportParams().setParamParentOrganisationUnit( Boolean.parseBoolean( reader.getElementValue() ) );
            
            reader.moveToStartElement( FIELD_PARAM_ORG_UNIT );
            reportTable.getReportParams().setParamOrganisationUnit( Boolean.parseBoolean( reader.getElementValue() ) );
            
            importObject( reportTable, params );
        }        
    }
}
