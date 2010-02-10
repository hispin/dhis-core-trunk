package org.hisp.dhis.importexport;

/*
 * Copyright (c) 2004-2007, University of Oslo
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
import java.util.Date;
import java.util.HashSet;

import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;

/**
 * @author Lars Helge Overland
 * @version $Id: ExportParams.java 5960 2008-10-17 14:07:50Z larshelg $
 */
public class ExportParams
{
    private boolean includeDataValues;
    
    private boolean includeCompleteDataSetRegistrations;
    
    private boolean extendedMode;
    
    private boolean aggregatedData;
    
    private Date startDate;
    
    private Date endDate;
    
    private Collection<Integer> categories = new ArrayList<Integer>();
    
    private Collection<Integer> categoryOptions = new ArrayList<Integer>();
    
    private Collection<Integer> categoryCombos = new ArrayList<Integer>();

    private Collection<Integer> categoryOptionCombos = new ArrayList<Integer>();

    private Collection<Integer> dataElements = new ArrayList<Integer>();
    
    private Collection<Integer> calculatedDataElements = new ArrayList<Integer>();
    
    private Collection<Integer> dataElementGroups = new ArrayList<Integer>();
    
    private Collection<Integer> dataElementGroupSets = new ArrayList<Integer>();
    
    private Collection<Integer> indicators = new ArrayList<Integer>();
    
    private Collection<Integer> indicatorGroups = new ArrayList<Integer>();
    
    private Collection<Integer> indicatorGroupSets = new ArrayList<Integer>();
    
    private Collection<Integer> indicatorTypes = new ArrayList<Integer>();
    
    private Collection<Integer> dataDictionaries = new ArrayList<Integer>();
    
    private Collection<Integer> dataSets = new ArrayList<Integer>();
    
    private Collection<Integer> periods = new ArrayList<Integer>();
    
    private Collection<Integer> organisationUnits = new ArrayList<Integer>();
    
    private Collection<Integer> organisationUnitGroups = new ArrayList<Integer>();
    
    private Collection<Integer> organisationUnitGroupSets = new ArrayList<Integer>();
    
    private Collection<Integer> organisationUnitLevels = new HashSet<Integer>();
    
    private Collection<Integer> validationRules = new ArrayList<Integer>();
    
    private Collection<Integer> reportTables = new ArrayList<Integer>();
    
    private Collection<Integer> olapUrls = new ArrayList<Integer>();
    
    private I18n i18n;
    
    private I18nFormat format;
    
    private String fileBaseName;
    
    // -------------------------------------------------------------------------
    // Constructur
    // -------------------------------------------------------------------------
    
    public ExportParams()
    {   
    }

    // -------------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------------

    public Collection<Integer> getAllDataElements()
    {
        if ( dataElements == null && calculatedDataElements == null )
        {
            return null;
        }
        else if ( dataElements == null && calculatedDataElements != null )
        {
            return calculatedDataElements;
        }
        else if ( dataElements != null && calculatedDataElements == null )
        {
            return dataElements;
        }
        else
        {
            final Collection<Integer> elements = getDataElements();
            
            elements.addAll( getCalculatedDataElements() );
            
            return elements;
        }
        
    }
    
    // -------------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------------

    public boolean isIncludeDataValues()
    {
        return includeDataValues;
    }

    public void setIncludeDataValues( boolean includeDataValues )
    {
        this.includeDataValues = includeDataValues;
    }

    public boolean isIncludeCompleteDataSetRegistrations()
    {
        return includeCompleteDataSetRegistrations;
    }

    public void setIncludeCompleteDataSetRegistrations( boolean includeCompleteDataSetRegistrations )
    {
        this.includeCompleteDataSetRegistrations = includeCompleteDataSetRegistrations;
    }
    
    public boolean isExtendedMode()
    {
        return extendedMode;
    }

    public void setExtendedMode( boolean extendedMode )
    {
        this.extendedMode = extendedMode;
    }

    public boolean isAggregatedData()
    {
        return aggregatedData;
    }

    public void setAggregatedData( boolean aggregatedData )
    {
        this.aggregatedData = aggregatedData;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }
    
    public I18n getI18n()
    {
        return i18n;
    }

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }
    
    public I18nFormat getFormat()
    {
        return format;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public Collection<Integer> getCategories()
    {
        return categories;
    }

    public void setCategories( Collection<Integer> categories )
    {
        this.categories = categories;
    }

    public Collection<Integer> getCategoryOptions()
    {
        return categoryOptions;
    }

    public void setCategoryOptions( Collection<Integer> categoryOptions )
    {
        this.categoryOptions = categoryOptions;
    }

    public Collection<Integer> getCategoryCombos()
    {
        return categoryCombos;
    }

    public void setCategoryCombos( Collection<Integer> categoryCombos )
    {
        this.categoryCombos = categoryCombos;
    }

    public Collection<Integer> getCategoryOptionCombos()
    {
        return categoryOptionCombos;
    }

    public void setCategoryOptionCombos( Collection<Integer> categoryOptionCombos )
    {
        this.categoryOptionCombos = categoryOptionCombos;
    }

    public Collection<Integer> getDataElements()
    {
        return dataElements;
    }

    public void setDataElements( Collection<Integer> dataElements )
    {
        this.dataElements = dataElements;
    }

    public Collection<Integer> getCalculatedDataElements()
    {
        return calculatedDataElements;
    }

    public void setCalculatedDataElements( Collection<Integer> calculatedDataElements )
    {
        this.calculatedDataElements = calculatedDataElements;
    }

    public Collection<Integer> getDataElementGroups()
    {
        return dataElementGroups;
    }

    public void setDataElementGroups( Collection<Integer> dataElementGroups )
    {
        this.dataElementGroups = dataElementGroups;
    }

    public Collection<Integer> getDataElementGroupSets()
    {
        return dataElementGroupSets;
    }

    public void setDataElementGroupSets( Collection<Integer> dataElementGroupSets )
    {
        this.dataElementGroupSets = dataElementGroupSets;
    }

    public Collection<Integer> getIndicators()
    {
        return indicators;
    }

    public void setIndicators( Collection<Integer> indicators )
    {
        this.indicators = indicators;
    }

    public Collection<Integer> getIndicatorGroups()
    {
        return indicatorGroups;
    }

    public void setIndicatorGroups( Collection<Integer> indicatorGroups )
    {
        this.indicatorGroups = indicatorGroups;
    }

    public Collection<Integer> getIndicatorGroupSets()
    {
        return indicatorGroupSets;
    }

    public void setIndicatorGroupSets( Collection<Integer> indicatorGroupSets )
    {
        this.indicatorGroupSets = indicatorGroupSets;
    }

    public Collection<Integer> getIndicatorTypes()
    {
        return indicatorTypes;
    }

    public void setIndicatorTypes( Collection<Integer> indicatorTypes )
    {
        this.indicatorTypes = indicatorTypes;
    }

    public Collection<Integer> getDataDictionaries()
    {
        return dataDictionaries;
    }

    public void setDataDictionaries( Collection<Integer> dataDictionaries )
    {
        this.dataDictionaries = dataDictionaries;
    }

    public Collection<Integer> getDataSets()
    {
        return dataSets;
    }

    public void setDataSets( Collection<Integer> dataSets )
    {
        this.dataSets = dataSets;
    }

    public Collection<Integer> getPeriods()
    {
        return periods;
    }

    public void setPeriods( Collection<Integer> periods )
    {
        this.periods = periods;
    }

    public Collection<Integer> getOrganisationUnits()
    {
        return organisationUnits;
    }

    public void setOrganisationUnits( Collection<Integer> organisationUnits )
    {
        this.organisationUnits = organisationUnits;
    }

    public Collection<Integer> getOrganisationUnitGroups()
    {
        return organisationUnitGroups;
    }

    public void setOrganisationUnitGroups( Collection<Integer> organisationUnitGroups )
    {
        this.organisationUnitGroups = organisationUnitGroups;
    }

    public Collection<Integer> getOrganisationUnitGroupSets()
    {
        return organisationUnitGroupSets;
    }

    public void setOrganisationUnitGroupSets( Collection<Integer> organisationUnitGroupSets )
    {
        this.organisationUnitGroupSets = organisationUnitGroupSets;
    }

    public Collection<Integer> getOrganisationUnitLevels()
    {
        return organisationUnitLevels;
    }

    public void setOrganisationUnitLevels( Collection<Integer> organisationUnitLevels )
    {
        this.organisationUnitLevels = organisationUnitLevels;
    }

    public Collection<Integer> getValidationRules()
    {
        return validationRules;
    }

    public void setValidationRules( Collection<Integer> validationRules )
    {
        this.validationRules = validationRules;
    }

    public Collection<Integer> getReportTables()
    {
        return reportTables;
    }

    public void setReportTables( Collection<Integer> reportTables )
    {
        this.reportTables = reportTables;
    }

    public Collection<Integer> getOlapUrls()
    {
        return olapUrls;
    }

    public void setOlapUrls( Collection<Integer> olapUrls )
    {
        this.olapUrls = olapUrls;
    }

    public String getFileBaseName()
    {
        return fileBaseName;
    }

    public void setFileBaseName( String fileBaseName )
    {
        this.fileBaseName = fileBaseName;
    }
}
