package org.hisp.dhis.web.api.service;

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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.comparator.DataElementSortOrderComparator;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.DailyPeriodType;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.period.WeeklyPeriodType;
import org.hisp.dhis.period.YearlyPeriodType;
import org.hisp.dhis.web.api.model.DataElement;
import org.hisp.dhis.web.api.model.DataSet;
import org.hisp.dhis.web.api.model.DataSetValue;
import org.hisp.dhis.web.api.model.DataValue;
import org.hisp.dhis.web.api.model.OrgUnit;
import org.hisp.dhis.web.api.model.Section;
import org.hisp.dhis.web.api.utils.LocaleUtil;
import org.hisp.dhis.web.api.utils.PeriodUtil;
import org.springframework.beans.factory.annotation.Required;

public class FacilityReportingServiceImpl
    implements FacilityReportingService
{
    private static Log log = LogFactory.getLog( FacilityReportingServiceImpl.class );

    private static boolean DEBUG = log.isDebugEnabled();

    private DataElementSortOrderComparator dataElementComparator = new DataElementSortOrderComparator();

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    private org.hisp.dhis.dataelement.DataElementCategoryService categoryService;

    private org.hisp.dhis.datavalue.DataValueService dataValueService;

    private org.hisp.dhis.dataset.DataSetService dataSetService;

    private org.hisp.dhis.i18n.I18nService i18nService;

    private org.hisp.dhis.datalock.DataSetLockService dataSetLockService;

    private org.hisp.dhis.web.api.service.ModelMapping modelMapping;

    // -------------------------------------------------------------------------
    // Service methods
    // -------------------------------------------------------------------------

    public List<DataSet> getMobileDataSetsForUnit( OrganisationUnit unit, String localeString )
    {

        List<DataSet> datasets = new ArrayList<DataSet>();
        Locale locale = LocaleUtil.getLocale( localeString );

        if ( DEBUG )
            log.debug( "Getting data sets for unit " + unit.getName() );

        for ( org.hisp.dhis.dataset.DataSet dataSet : dataSetService.getDataSetsForMobile( unit ) )
        {
            PeriodType periodType = dataSet.getPeriodType();
            if ( periodType instanceof DailyPeriodType || periodType instanceof WeeklyPeriodType
                || periodType instanceof MonthlyPeriodType || periodType instanceof YearlyPeriodType
                || periodType instanceof QuarterlyPeriodType )
            {
                if ( DEBUG )
                    log.debug( "Found data set " + dataSet.getName() );

                datasets.add( getDataSetForLocale( dataSet.getId(), locale ) );
            }
            else
            {
                log.warn( "Dataset '" + dataSet.getName()
                    + "' set to be reported from mobile, but not of a supported period type: " + periodType.getName() );
            }
        }

        return datasets;
    }

    public DataSet getDataSetForLocale( int dataSetId, Locale locale )
    {
        org.hisp.dhis.dataset.DataSet dataSet = dataSetService.getDataSet( dataSetId );
        dataSet = i18n( i18nService, locale, dataSet );
        Set<org.hisp.dhis.dataset.Section> sections = dataSet.getSections();

        DataSet ds = new DataSet();

        ds.setId( dataSet.getId() );
        // Name defaults to short name with fallback to name if empty
        String name = dataSet.getShortName();
        if ( emptyString( name ) )
        {
            name = dataSet.getName();
        }

        ds.setName( name );
        ds.setVersion( dataSet.getVersion() );
        ds.setPeriodType( dataSet.getPeriodType().getName() );

        List<Section> sectionList = new ArrayList<Section>();
        ds.setSections( sectionList );

        if ( sections == null || sections.size() == 0 )
        {
            List<org.hisp.dhis.dataelement.DataElement> dataElements = new ArrayList<org.hisp.dhis.dataelement.DataElement>(
                dataSet.getDataElements() );

            Collections.sort( dataElements, dataElementComparator );

            // Fake Section to store Data Elements
            Section section = new Section();
            section.setId( 0 );
            section.setName( "" );

            section.setDataElements( getDataElements( locale, dataElements ) );
            sectionList.add( section );
        }
        else
        {
            for ( org.hisp.dhis.dataset.Section s : sections )
            {
                Section section = new Section();
                section.setId( s.getId() );
                section.setName( s.getName() );

                List<DataElement> dataElementList = getDataElements( locale, s.getDataElements() );
                section.setDataElements( dataElementList );
                sectionList.add( section );
            }
        }

        return ds;
    }

    private List<DataElement> getDataElements( Locale locale, List<org.hisp.dhis.dataelement.DataElement> dataElements )
    {
        List<DataElement> dataElementList = new ArrayList<DataElement>();

        for ( org.hisp.dhis.dataelement.DataElement dataElement : dataElements )
        {
            dataElement = i18n( i18nService, locale, dataElement );

            DataElement de = modelMapping.getDataElement( dataElement );

            // For facility Reporting, no data elements are mandatory
            de.setCompulsory( false );

            dataElementList.add( de );
        }
        return dataElementList;
    }

    @Override
    public void saveDataSetValues( OrganisationUnit unit, DataSetValue dataSetValue )
        throws NotAllowedException
    {

        org.hisp.dhis.dataset.DataSet dataSet = dataSetService.getDataSet( dataSetValue.getId() );

        if ( !dataSetService.getDataSetsBySource( unit ).contains( dataSet ) )
        {
            throw new NotAllowedException( "INVALID_DATASET_ASSOCIATION" );
        }

        Period selectedPeriod = getPeriod( dataSetValue.getPeriodName(), dataSet.getPeriodType() );

        if ( selectedPeriod == null )
        {
            throw new NotAllowedException( "INVALID_PERIOD" );
        }

        if ( isDataSetLocked( unit, dataSet, selectedPeriod ) )
        {
            throw new NotAllowedException( "DATASET_LOCKED" );
        }

        Map<Integer, org.hisp.dhis.dataelement.DataElement> dataElementMap = new HashMap<Integer, org.hisp.dhis.dataelement.DataElement>();
        for ( org.hisp.dhis.dataelement.DataElement dataElement : dataSet.getDataElements() )
        {
            dataElementMap.put( dataElement.getId(), dataElement );
        }

        Set<Integer> handled = new HashSet<Integer>();

        for ( DataValue dv : dataSetValue.getDataValues() )
        {
            int elementId = dv.getId();

            if ( handled.contains( elementId ) )
            {
                log.info( "Multiple values for element " + elementId + " submitted. Not handling this value." );
                continue;
            }

            org.hisp.dhis.dataelement.DataElement dataElement = dataElementMap.get( elementId );

            if ( dataElement == null )
            {
                log.info( "Data element value submitted for data element " + elementId + ", that is not in data set '"
                    + dataSet.getName() + "'" );
                handled.add( elementId );
                continue;
            }

            if ( emptyString( dv.getValue() ) )
            {
                log.info( "Empty data value for data element " + elementId + " not saved" );
                handled.add( elementId );
                continue;
            }

            saveValue(unit, selectedPeriod, dataElement, dv);

            handled.add( elementId );
        }
        reportMissingValues( dataSet, handled );
    }

    private void saveValue(OrganisationUnit unit, Period period, org.hisp.dhis.dataelement.DataElement dataElement, DataValue dv) {

        String value = dv.getValue().trim();

        DataElementCategoryOptionCombo cateOptCombo = categoryService.getDataElementCategoryOptionCombo( dv
            .getCategoryOptComboID() );

        org.hisp.dhis.datavalue.DataValue dataValue = dataValueService.getDataValue( unit, dataElement,
            period, cateOptCombo );

        if ( dataValue == null )
        {
            dataValue = new org.hisp.dhis.datavalue.DataValue( dataElement, period, unit, value, "",
                new Date(), "", cateOptCombo );
            dataValueService.addDataValue( dataValue );
        }
        else
        {
            dataValue.setValue( value );
            dataValue.setTimestamp( new Date() );
            dataValueService.updateDataValue( dataValue );
        }

    }
    
    private void reportMissingValues( org.hisp.dhis.dataset.DataSet dataSet, Set<Integer> handled )
    {
        Collection<org.hisp.dhis.dataelement.DataElement> dataElements = dataSet.getDataElements();
        for ( org.hisp.dhis.dataelement.DataElement element : dataElements )
        {
            if ( !handled.contains( element.getId() ) )
            {
                log.info( "Submitted values for dataset '" + dataSet.getName() + "' missing data element '" + element.getName() + "'" );

            }
        }
    }

    // -------------------------------------------------------------------------
    // Supportive method
    // -------------------------------------------------------------------------

    private boolean isDataSetLocked( OrganisationUnit unit, org.hisp.dhis.dataset.DataSet dataSet, Period selectedPeriod )
    {
        if ( dataSetLockService.getDataSetLockByDataSetPeriodAndSource( dataSet, selectedPeriod, unit ) != null )
            return true;
        return false;
    }

    private boolean emptyString( String value )
    {
        return value == null || value.trim().isEmpty();
    }

    public Period getPeriod( String periodName, PeriodType periodType )
    {
        Period period = PeriodUtil.getPeriod( periodName, periodType );

        if ( period == null )
        {
            return null;
        }

        Period persistedPeriod = periodService.getPeriod( period.getStartDate(), period.getEndDate(), periodType );

        if ( persistedPeriod == null )
        {
            periodService.addPeriod( period );
            persistedPeriod = periodService.getPeriod( period.getStartDate(), period.getEndDate(), periodType );
        }

        return persistedPeriod;
    }

    // -------------------------------------------------------------------------
    // Dependency setters
    // -------------------------------------------------------------------------

    @Required
    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    @Required
    public void setCategoryService( org.hisp.dhis.dataelement.DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    @Required
    public void setDataValueService( org.hisp.dhis.datavalue.DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    @Required
    public void setDataSetService( org.hisp.dhis.dataset.DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    @Required
    public void setI18nService( org.hisp.dhis.i18n.I18nService i18nService )
    {
        this.i18nService = i18nService;
    }

    @Required
    public void setDataSetLockService( org.hisp.dhis.datalock.DataSetLockService dataSetLockService )
    {
        this.dataSetLockService = dataSetLockService;
    }

    @Required
    public void setModelMapping( org.hisp.dhis.web.api.service.ModelMapping modelMapping )
    {
        this.modelMapping = modelMapping;
    }

}
