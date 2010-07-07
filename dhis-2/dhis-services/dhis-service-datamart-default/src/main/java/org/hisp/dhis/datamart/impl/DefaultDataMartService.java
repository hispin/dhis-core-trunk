package org.hisp.dhis.datamart.impl;

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

import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;

import java.util.Collection;
import java.util.HashSet;

import org.hisp.dhis.aggregation.AggregatedDataValue;
import org.hisp.dhis.aggregation.AggregatedIndicatorValue;
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datamart.DataMartExport;
import org.hisp.dhis.datamart.DataMartService;
import org.hisp.dhis.datamart.DataMartStore;
import org.hisp.dhis.datamart.engine.DataMartEngine;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.dimension.DimensionOption;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.RelativePeriods;
import org.hisp.dhis.system.process.OutputHolderState;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 */
public class DefaultDataMartService
    implements DataMartService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataMartEngine dataMartEngine;
    
    public void setDataMartEngine( DataMartEngine dataMartEngine )
    {
        this.dataMartEngine = dataMartEngine;
    }

    private DataMartStore dataMartStore;
    
    public void setDataMartStore( DataMartStore dataMartStore )
    {
        this.dataMartStore = dataMartStore;
    }

    private GenericIdentifiableObjectStore<DataMartExport> dataMartExportStore;

    public void setDataMartExportStore( GenericIdentifiableObjectStore<DataMartExport> dataMartExportStore )
    {
        this.dataMartExportStore = dataMartExportStore;
    }
    
    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    // -------------------------------------------------------------------------
    // Export
    // -------------------------------------------------------------------------

    @Transactional
    public int export( Collection<Integer> dataElementIds, Collection<Integer> indicatorIds,
        Collection<Integer> periodIds, Collection<Integer> organisationUnitIds )
    {
        return export( dataElementIds, indicatorIds, periodIds, organisationUnitIds, null );
    }
    
    @Transactional
    public int export( int id )
    {
        DataMartExport dataMartExport = getDataMartExport( id );
        
        Collection<Period> allPeriods = new HashSet<Period>( dataMartExport.getPeriods() );
        
        if ( dataMartExport.getRelatives() != null )
        {
            allPeriods.addAll( periodService.reloadPeriods( dataMartExport.getRelatives().getRelativePeriods( 1, null, false ) ) );
        }
        
        return dataMartEngine.export( 
            getIdentifiers( DataElement.class, dataMartExport.getDataElements() ), 
            getIdentifiers( Indicator.class, dataMartExport.getIndicators() ), 
            getIdentifiers( Period.class, allPeriods ),
            getIdentifiers( OrganisationUnit.class, dataMartExport.getOrganisationUnits() ), 
            new OutputHolderState() );
    }
    
    @Transactional
    public int export( Collection<Integer> dataElementIds, Collection<Integer> indicatorIds,
        Collection<Integer> periodIds, Collection<Integer> organisationUnitIds, RelativePeriods relatives )
    {
        if ( relatives != null )
        {
            periodIds.addAll( getIdentifiers( Period.class, periodService.reloadPeriods( relatives.getRelativePeriods( 1, null, false ) ) ) );
        }
        
        return dataMartEngine.export( dataElementIds, indicatorIds, periodIds, organisationUnitIds, new OutputHolderState() );
    }

    // ----------------------------------------------------------------------
    // AggregatedDataValue
    // ----------------------------------------------------------------------
    
    public Double getAggregatedValue( DataElement dataElement, Period period, OrganisationUnit organisationUnit )
    {
        return dataMartStore.getAggregatedValue( dataElement, period, organisationUnit );
    }
    
    public Double getAggregatedValue( DataElement dataElement, DimensionOption dimensionOption, Period period, OrganisationUnit organisationUnit )
    {
        return dataMartStore.getAggregatedValue( dataElement, dimensionOption, period, organisationUnit );
    }
    
    public Double getAggregatedValue( DataElement dataElement, DataElementCategoryOptionCombo categoryOptionCombo, Period period, OrganisationUnit organisationUnit )
    {
        return dataMartStore.getAggregatedValue( dataElement, categoryOptionCombo, period, organisationUnit );
    }
    
    public Collection<AggregatedDataValue> getAggregatedDataValues( int dataElementId, Collection<Integer> periodIds, Collection<Integer> organisationUnitIds )
    {
        return dataMartStore.getAggregatedDataValues( dataElementId, periodIds, organisationUnitIds );
    }
    
    public int deleteAggregatedDataValues()
    {
        return dataMartStore.deleteAggregatedDataValues();
    }

    // -------------------------------------------------------------------------
    // AggregatedIndicatorValue
    // -------------------------------------------------------------------------
    
    public Double getAggregatedValue( Indicator indicator, Period period, OrganisationUnit unit )
    {
        return dataMartStore.getAggregatedValue( indicator, period, unit );
    }
    
    public Collection<AggregatedIndicatorValue> getAggregatedIndicatorValues( Collection<Integer> periodIds, Collection<Integer> organisationUnitIds )
    {
        return dataMartStore.getAggregatedIndicatorValues( periodIds, organisationUnitIds );
    }
    
    public Collection<AggregatedIndicatorValue> getAggregatedIndicatorValues( Collection<Integer> indicatorIds,
        Collection<Integer> periodIds, Collection<Integer> organisationUnitIds )
    {
        return dataMartStore.getAggregatedIndicatorValues( indicatorIds, periodIds, organisationUnitIds );
    }
    
    public int deleteAggregatedIndicatorValues()
    {
        return dataMartStore.deleteAggregatedIndicatorValues();
    }

    // ----------------------------------------------------------------------
    // DataValue
    // ----------------------------------------------------------------------
    
    public Collection<DeflatedDataValue> getDeflatedDataValues( int dataElementId, int periodId, Collection<Integer> sourceIds )
    {
        return dataMartStore.getDeflatedDataValues( dataElementId, periodId, sourceIds );
    }
    
    public DataValue getDataValue( int dataElementId, int categoryOptionComboId, int periodId, int sourceId )
    {
        return dataMartStore.getDataValue( dataElementId, categoryOptionComboId, periodId, sourceId );
    }
    
    // -------------------------------------------------------------------------
    // DataMartExport
    // -------------------------------------------------------------------------
    
    @Transactional
    public void saveDataMartExport( DataMartExport export )
    {
        dataMartExportStore.save( export );
    }

    @Transactional
    public void deleteDataMartExport( DataMartExport export )
    {
        dataMartExportStore.delete( export );
    }

    @Transactional
    public DataMartExport getDataMartExport( int id )
    {
        return dataMartExportStore.get( id );
    }

    @Transactional
    public Collection<DataMartExport> getAllDataMartExports()
    {
        return dataMartExportStore.getAll();
    }

    @Transactional
    public DataMartExport getDataMartExportByName( String name )
    {
        return dataMartExportStore.getByName( name );
    }
}
