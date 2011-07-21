package org.hisp.dhis.dataelement;

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

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.hierarchy.HierarchyViolationException;

/**
 * Defines the functionality for persisting DataElements and DataElementGroups.
 * 
 * @author Torgeir Lorange Ostby
 * @version $Id: DataElementStore.java 6289 2008-11-14 17:53:24Z larshelg $
 */
public interface DataElementStore
{
    String ID = DataElementStore.class.getName();

    // -------------------------------------------------------------------------
    // DataElement
    // -------------------------------------------------------------------------

    /**
     * Adds a DataElement.
     * 
     * @param dataElement the DataElement to add.
     * @return a generated unique id of the added DataElement.
     */
    int addDataElement( DataElement dataElement );

    /**
     * Updates a DataElement.
     * 
     * @param dataElement the DataElement to update.
     */
    void updateDataElement( DataElement dataElement );

    /**
     * Deletes a DataElement. The DataElement is also removed from any
     * DataElementGroups it is a member of. It is not possible to delete a
     * DataElement with children.
     * 
     * @param dataElement the DataElement to delete.
     * @throws HierarchyViolationException if the DataElement has children.
     */
    void deleteDataElement( DataElement dataElement )
        throws HierarchyViolationException;

    /**
     * Returns a DataElement.
     * 
     * @param id the id of the DataElement to return.
     * @return the DataElement with the given id, or null if no match.
     */
    DataElement getDataElement( int id );

    /**
     * Returns the DataElement with the given UUID.
     * 
     * @param uuid the UUID.
     * @return the DataElement with the given UUID, or null if no match.
     */
    DataElement getDataElement( String uuid );

    /**
     * Returns a DataElement with a given name.
     * 
     * @param name the name of the DataElement to return.
     * @return the DataElement with the given name, or null if no match.
     */
    DataElement getDataElementByName( String name );
    
    /**
     * Returns List of DataElements with a given key.
     * 
     * @param key the name of the DataElement to return.
     * @return List of DataElements with a given key, or all dataelements if no match.
     */
    Collection<DataElement> searchDataElementByName( String key );

    /**
     * Returns a DataElement with a given alternative name.
     * 
     * @param alternativeName the alternative name of the DataElement to return.
     * @return the DataElement with the given alternative name, or null if no
     *         match.
     */
    DataElement getDataElementByAlternativeName( String alternativeName );

    /**
     * Returns a DataElement with a given short name.
     * 
     * @param shortName the short name of the DataElement to return.
     * @return the DataElement with the given short name, or null if no match.
     */
    DataElement getDataElementByShortName( String shortName );

    /**
     * Returns all DataElements.
     * 
     * @return a collection of all DataElements, or an empty collection if there
     *         are no DataElements.
     */
    Collection<DataElement> getAllDataElements();

    /**
     * Returns all DataElements with types that are possible to aggregate. The
     * types are currently INT and BOOL.
     * 
     * @return all DataElements with types that are possible to aggregate.
     */
    Collection<DataElement> getAggregateableDataElements();

    /**
     * Returns all active DataElements.
     * 
     * @return a collection of all active DataElements, or an empty collection
     *         if there are no active DataElements.
     */
    Collection<DataElement> getAllActiveDataElements();

    /**
     * Returns all DataElements with a given aggregantion operator.
     * 
     * @param aggregationOperator the aggregation operator of the DataElements
     *        to return.
     * @return a collection of all DataElements with the given aggregation
     *         operator, or an empty collection if no DataElements have the
     *         aggregation operator.
     */
    Collection<DataElement> getDataElementsByAggregationOperator( String aggregationOperator );

    /**
     * Returns all DataElements with the given domain type.
     * 
     * @param domainType the domainType.
     * @return all DataElements with the given domainType.
     */
    Collection<DataElement> getDataElementsByDomainType( String domainType );

    /**
     * Returns all DataElements with the given type.
     * 
     * @param type the type.
     * @return all DataElements with the given type.
     */

    Collection<DataElement> getDataElementsByType( String type );

    /**
     * Returns all DataElements with the given category combo.
     * 
     * @param categoryCombo the DataElementCategoryCombo.
     * @return all DataElements with the given category combo.
     */
    Collection<DataElement> getDataElementByCategoryCombo( DataElementCategoryCombo categoryCombo );

    /**
     * Returns all DataElements which are associated with one or more
     * DataElementGroupSets.
     * 
     * @return all DataElements which are associated with one or more
     *         DataElementGroupSets.
     */
    Collection<DataElement> getDataElementsWithGroupSets();

    /**
     * Defines the given data elements as zero is significant. 
     * 
     * @param dataElementIds identifiers of data elements where zero is significant.
     */
    void setZeroIsSignificantForDataElements( Collection<Integer> dataElementIds );
    
    /**
     * Returns all DataElement which zeroIsSignificant property is true or false
     * @param zeroIsSignificant is zeroIsSignificant property 
     * @return a collection of all DataElement
     */
    Collection<DataElement> getDataElementsByZeroIsSignificant( boolean zeroIsSignificant );
    
    /**
     * Returns all DataElements which are not member of any DataElementGroups.
     * 
     * @return all DataElements which are not member of any DataElementGroups.
     */
    Collection<DataElement> getDataElementsWithoutGroups();
    
    /**
     * Returns all DataElements which are not assigned to any DataSets.
     * 
     * @return all DataElements which are not assigned to any DataSets.
     */
    Collection<DataElement> getDataElementsWithoutDataSets();
    
    /**
     * Returns all DataElements which are assigned to at least one DataSet.
     * 
     * @return all DataElements which are assigned to at least one DataSet.
     */
    Collection<DataElement> getDataElementsWithDataSets();
    
    /**
     * Checks whether a DataElement with the given identifier exists.
     * 
     * @param id the DataElement identifier.
     * @return true or false.
     */
    boolean dataElementExists( int id );
    
    /**
     * Checks whether a DataElementCategoryOptionCombo with the given identifier exists.
     * 
     * @param id the DataElementCategoryOptionCombo identifier.
     * @return true or false.
     */
    boolean dataElementCategoryOptionComboExists( int id );
    
    Collection<DataElement> getDataElementsByDataSets( Collection<DataSet> dataSets );

    Collection<DataElement> getDataElementsLikeName( String name );
    
    Collection<DataElement> getDataElementsBetween( int first, int max );
    
    Collection<DataElement> getDataElementsBetweenByName( String name, int first, int max );
    
    int getDataElementCount();
    
    int getDataElementCountByName( String name );
    
    // -------------------------------------------------------------------------
    // DataElementOperand
    // -------------------------------------------------------------------------

    /**
     * Returns all generated permutations of Operands. Requires the 
     * categoryoptioncomboname resource table to be populated.
     * 
     * @return a collection of all Operands.
     */
    Collection<DataElementOperand> getAllGeneratedOperands();

    /**
     * Returns all generated permutations of Operands for the given collection of
     * DataElements. Requires the categoryoptioncomboname resource table to be populated.
     * 
     * @param dataElements the DataElements.
     * @return a collection of all Operands.
     */
    Collection<DataElementOperand> getAllGeneratedOperands( Collection<DataElement> dataElements );
}
