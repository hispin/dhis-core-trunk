package org.hisp.dhis.datamart;

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

import java.util.Collection;

import org.hisp.dhis.aggregation.AggregatedDataValue;
import org.hisp.dhis.aggregation.AggregatedIndicatorValue;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DeflatedDataValue;
import org.hisp.dhis.dimension.DimensionOption;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.RelativePeriods;

/**
 * @author Lars Helge Overland
 */
public interface DataMartService
{
    final String ID = DataMartService.class.getName();

    // ----------------------------------------------------------------------
    // Export
    // ----------------------------------------------------------------------
    
    /**
     * Export to data mart for the given DataMartExport.
     * 
     * @id the DataMartExport identifier.
     * @return the number of exported values.
     */
    int export( int id );

    /**
     * Exports to data mart for the given arguments.
     * 
     * @param dataElementIds the data element identifiers.
     * @param indicatorIds the indicator identifiers.
     * @param periodIds the period identifiers.
     * @param organisationUnitIds the organisation unit identifiers.
     * @return the number of exported values.
     */
    int export( Collection<Integer> dataElementIds, Collection<Integer> indicatorIds,
        Collection<Integer> periodIds, Collection<Integer> organisationUnitIds );
    
    /**
     * Exports to data mart for the given arguments.
     * 
     * @param dataElementIds the data element identifiers.
     * @param indicatorIds the indicator identifiers.
     * @param periodIds the period identifiers.
     * @param organisationUnitIds the organisation unit identifiers.
     * @param relatives the RelativePeriods.
     * @return the number of exported values.
     */
    int export( Collection<Integer> dataElementIds, Collection<Integer> indicatorIds,
        Collection<Integer> periodIds, Collection<Integer> organisationUnitIds, RelativePeriods relatives );

    // ----------------------------------------------------------------------
    // AggregatedDataValue
    // ----------------------------------------------------------------------
    
    /**
     * Gets the total aggregated value from the datamart table for the given parameters.
     * 
     * @param dataElement The DataElement.
     * @param period The Period.
     * @param organisationUnit The OrganisationUnit.
     * @return the aggregated value.
     */
    Double getAggregatedValue( DataElement dataElement, Period period, OrganisationUnit organisationUnit );

    /**
     * Gets the total aggregated value from the datamart table for the given parameters.
     * 
     * @param dataElement The DataElement.
     * @param dimensionOptionElement the DimensionOptionElement.
     * @param period The Period.
     * @param organisationUnit The OrganisationUnit.
     * @return the aggregated value.
     */
    Double getAggregatedValue( DataElement dataElement, DimensionOption dimensionOption, Period period, OrganisationUnit organisationUnit );
    
    /**
     * Gets the aggregated value from the datamart table for the given parameters.
     * 
     * @param dataElement The DataElement.
     * @param categoryOptionCombo The DataElementCategoryOptionCombo.
     * @param period The Period.
     * @param organisationUnit The OrganisationUnit.
     * @return the aggregated value, or -1 if no value exists.
     */
    Double getAggregatedValue( DataElement dataElement, DataElementCategoryOptionCombo categoryOptionCombo, Period period, OrganisationUnit organisationUnit );
    
    /**
     * Gets a collection of AggregatedDataValues.
     * 
     * @param dataElementId the DataElement identifier.
     * @param periodIds the collection of Period identifiers.
     * @param organisationUnitIds the collection of OrganisationUnit identifiers.
     * @return a collection of AggregatedDataValues.
     */
    Collection<AggregatedDataValue> getAggregatedDataValues( int dataElementId, Collection<Integer> periodIds, Collection<Integer> organisationUnitIds );
    
    /**
     * Deletes all AggregatedDataValues.
     * 
     * @return the number of deleted AggregatedDataValues.
     * @throws AggregationStoreException
     */
    int deleteAggregatedDataValues();

    // ----------------------------------------------------------------------
    // AggregatedIndicatorValue
    // ----------------------------------------------------------------------

    /**
     * Gets the aggregated value from the datamart table for the given parameters.
     * 
     * @param indicator The Indicator.
     * @param period The Period.
     * @param organisationUnit The OrganisationUnit.
     * @return the aggregated value, or -1 if no value exists.
     */
    Double getAggregatedValue( Indicator indicator, Period period, OrganisationUnit unit );

    /**
     * Gets a collection of AggregatedIndicatorValues.
     * 
     * @param periodIds the Period identifiers.
     * @param organisationUnitIds the OrganisationUnit identifiers.
     * @return a collection of AggregatedIndicatorValues.
     */
    Collection<AggregatedIndicatorValue> getAggregatedIndicatorValues( Collection<Integer> periodIds, Collection<Integer> organisationUnitIds );
    
    /**
     * Gets a collection of AggregatedIndicatorValues.
     * 
     * @param indicatorIds the Indicator identifiers.
     * @param periodIds the Period identifiers.
     * @param organisationUnitIds the OrganisationUnit identifiers.
     * @return a collection of AggregatedIndicatorValues.
     */
    Collection<AggregatedIndicatorValue> getAggregatedIndicatorValues( Collection<Integer> indicatorIds,
        Collection<Integer> periodIds, Collection<Integer> organisationUnitIds );
    
    /**
     * Deletes all AggregatedIndicatorValue.
     * 
     * @return the number of deleted AggregatedIndicatorValues.
     * @throws AggregationStoreException
     */
    int deleteAggregatedIndicatorValues();

    // ----------------------------------------------------------------------
    // DataValue
    // ----------------------------------------------------------------------
    
    /**
     * Gets a Collection of DeflatedDataValues.
     * 
     * @param dataElementId the DataElement identifier.
     * @param periodId the Period identifier.
     * @param sourceIds the Collection of Source identifiers.
     */
    Collection<DeflatedDataValue> getDeflatedDataValues( int dataElementId, int periodId, Collection<Integer> sourceIds );
    
    /**
     * Gets a DataValues. Note that this is a "deflated" data value as the objects
     * in the composite identifier only has its id property populated.
     * 
     * @param dataElementId the DataElement identifier.
     * @param categoryOptionComboId the DataElementCategoryOptionCombo identifier.
     * @param periodId the Period identifier.
     * @param sourceId the Source identifier.
     */
    DataValue getDataValue( int dataElementId, int categoryOptionComboId, int periodId, int sourceId );

    // ----------------------------------------------------------------------
    // DataMartExport
    // ----------------------------------------------------------------------
    
    /**
     * Saves a DataMartExport.
     * 
     * @param export the DataMartExport to save.
     */
    void saveDataMartExport( DataMartExport export );

    /**
     * Retrieves the DataMartExport with the given identifier.
     * 
     * @param id the identifier of the DataMartExport.
     * @return the DataMartExport.
     */
    DataMartExport getDataMartExport( int id );
    
    /**
     * Deletes a DataMartExport.
     * 
     * @param export the DataMartExport to delete.
     */
    void deleteDataMartExport( DataMartExport export );
    
    /**
     * Retrieves all DataMartExports.
     * 
     * @return a Collection of DataMartExports.
     */
    Collection<DataMartExport> getAllDataMartExports();
    
    /**
     * Retrieves the DataMartExport with the given name.
     * 
     * @param name the name of the DataMartExport to retrieve.
     * @return the DataMartExport.
     */
    DataMartExport getDataMartExportByName( String name );    
}
