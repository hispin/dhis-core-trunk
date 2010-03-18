package org.hisp.dhis.dataelement;

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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.common.comparator.CategoryComboSizeComparator;
import org.hisp.dhis.hierarchy.HierarchyViolationException;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.system.util.Filter;
import org.hisp.dhis.system.util.FilterUtils;
import org.hisp.dhis.system.util.UUIdUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Kristian Nordal
 * @version $Id: DefaultDataElementService.java 5243 2008-05-25 10:18:58Z
 *          larshelg $
 */
@Transactional
public class DefaultDataElementService
    implements DataElementService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementStore dataElementStore;

    public void setDataElementStore( DataElementStore dataElementStore )
    {
        this.dataElementStore = dataElementStore;
    }

    private GenericIdentifiableObjectStore<DataElementGroup> dataElementGroupStore;

    public void setDataElementGroupStore( GenericIdentifiableObjectStore<DataElementGroup> dataElementGroupStore )
    {
        this.dataElementGroupStore = dataElementGroupStore;
    }

    private GenericIdentifiableObjectStore<DataElementGroupSet> dataElementGroupSetStore;

    public void setDataElementGroupSetStore(
        GenericIdentifiableObjectStore<DataElementGroupSet> dataElementGroupSetStore )
    {
        this.dataElementGroupSetStore = dataElementGroupSetStore;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    // -------------------------------------------------------------------------
    // DataElement
    // -------------------------------------------------------------------------

    public int addDataElement( DataElement dataElement )
    {
        if ( dataElement.getUuid() == null )
        {
            dataElement.setUuid( UUIdUtils.getUUId() );
        }

        dataElement.setLastUpdated( new Date() );

        int id = dataElementStore.addDataElement( dataElement );

        i18nService.addObject( dataElement );

        return id;
    }

    public void updateDataElement( DataElement dataElement )
    {
        dataElement.setLastUpdated( new Date() );

        dataElementStore.updateDataElement( dataElement );

        i18nService.verify( dataElement );
    }

    public void deleteDataElement( DataElement dataElement )
        throws HierarchyViolationException
    {
        i18nService.removeObject( dataElement );

        dataElementStore.deleteDataElement( dataElement );
    }

    public DataElement getDataElement( int id )
    {
        return i18n( i18nService, dataElementStore.getDataElement( id ) );
    }

    public DataElement getDataElement( String uuid )
    {
        return i18n( i18nService, dataElementStore.getDataElement( uuid ) );
    }

    public Collection<DataElement> getAllDataElements()
    {
        return i18n( i18nService, dataElementStore.getAllDataElements() );
    }

    public Collection<DataElement> getNonCalculatedDataElements()
    {
        Collection<DataElement> dataElements = new ArrayList<DataElement>();

        for ( DataElement dataElement : getAllDataElements() )
        {
            if ( !(dataElement instanceof CalculatedDataElement) )
            {
                dataElements.add( dataElement );
            }
        }

        return dataElements;
    }

    public Collection<DataElement> getDataElements( final Collection<Integer> identifiers )
    {
        Collection<DataElement> dataElements = getAllDataElements();

        return identifiers == null ? dataElements : FilterUtils.filter( dataElements, new Filter<DataElement>()
        {
            public boolean retain( DataElement dataElement )
            {
                return identifiers.contains( dataElement.getId() );
            }
        } );
    }

    public void setZeroIsSignificantForDataElements( Collection<Integer> dataElementIds, boolean zeroIsSignificant )
    {
        if ( dataElementIds != null && !dataElementIds.isEmpty() )
        {
            dataElementStore.setZeroIsSignificantForDataElements( dataElementIds, zeroIsSignificant );
        }
    }

    public Collection<DataElement> getDataElementsByZeroIsSignificant( boolean zeroIsSignificant )
    {
        return dataElementStore.getDataElementsByZeroIsSignificant( zeroIsSignificant );
    }

    public Collection<DataElement> getDataElementsByZeroIsSignificantAndGroup( boolean zeroIsSignificant,
        DataElementGroup dataElementGroup )
    {
        Collection<DataElement> dataElements = getDataElementsByZeroIsSignificant( zeroIsSignificant );

        dataElements.retainAll( dataElementGroup.getMembers() );

        return dataElements;
    }

    public Collection<DataElement> getNonCalculatedDataElements( Collection<Integer> identifiers )
    {
        if ( identifiers == null )
        {
            return getNonCalculatedDataElements();
        }

        return getDataElements( identifiers );
    }

    public Collection<DataElement> getAggregateableDataElements()
    {
        return i18n( i18nService, dataElementStore.getAggregateableDataElements() );
    }

    public Collection<DataElement> getAllActiveDataElements()
    {
        return i18n( i18nService, dataElementStore.getAllActiveDataElements() );
    }

    public DataElement getDataElementByName( String name )
    {
        return i18n( i18nService, dataElementStore.getDataElementByName( name ) );
    }

    public DataElement getDataElementByAlternativeName( String alternativeName )
    {
        return i18n( i18nService, dataElementStore.getDataElementByAlternativeName( alternativeName ) );
    }

    public DataElement getDataElementByShortName( String shortName )
    {
        return i18n( i18nService, dataElementStore.getDataElementByShortName( shortName ) );
    }

    public Collection<DataElement> getDataElementsByAggregationOperator( String aggregationOperator )
    {
        return i18n( i18nService, dataElementStore.getDataElementsByAggregationOperator( aggregationOperator ) );
    }

    public Collection<DataElement> getDataElementsByType( String type )
    {
        return i18n( i18nService, dataElementStore.getDataElementsByType( type ) );
    }
    
    public Collection<DataElement> getDataElementsByPeriodType( final PeriodType periodType )
    {
        Collection<DataElement> dataElements = getAllDataElements();

        return FilterUtils.filter( dataElements, new Filter<DataElement>()
        {
            public boolean retain( DataElement dataElement )
            {
                return dataElement.getPeriodType() != null && dataElement.getPeriodType().equals( periodType );
            }
        } );
    }

    public Collection<DataElement> getDataElementsByDomainType( String domainType )
    {
        return i18n( i18nService, dataElementStore.getDataElementsByDomainType( domainType ) );
    }

    public Collection<DataElement> getDataElementByCategoryCombo( DataElementCategoryCombo categoryCombo )
    {
        return i18n( i18nService, dataElementStore.getDataElementByCategoryCombo( categoryCombo ) );
    }

    public Map<DataElementCategoryCombo, List<DataElement>> getGroupedDataElementsByCategoryCombo(
        List<DataElement> dataElements )
    {
        Map<DataElementCategoryCombo, List<DataElement>> mappedDataElements = new HashMap<DataElementCategoryCombo, List<DataElement>>();

        for ( DataElement de : dataElements )
        {
            if ( mappedDataElements.containsKey( de.getCategoryCombo() ) )
            {
                mappedDataElements.get( de.getCategoryCombo() ).add( de );
            }
            else
            {
                List<DataElement> des = new ArrayList<DataElement>();
                des.add( de );

                mappedDataElements.put( de.getCategoryCombo(), des );
            }
        }

        return mappedDataElements;
    }

    public List<DataElementCategoryCombo> getDataElementCategoryCombos( List<DataElement> dataElements )
    {
        Set<DataElementCategoryCombo> setCategoryCombos = new HashSet<DataElementCategoryCombo>();

        for ( DataElement de : dataElements )
        {
            setCategoryCombos.add( de.getCategoryCombo() );
        }

        List<DataElementCategoryCombo> listCategoryCombos = new ArrayList<DataElementCategoryCombo>( setCategoryCombos );

        Collections.sort( listCategoryCombos, new CategoryComboSizeComparator() );

        return listCategoryCombos;
    }

    public Collection<DataElement> getDataElementsWithGroupSets()
    {
        return i18n( i18nService, dataElementStore.getDataElementsWithGroupSets() );
    }

    public Collection<DataElement> getDataElementsByGroupSets( Set<DataElementGroupSet> groupSets )
    {
        Collection<DataElement> dataElements = new HashSet<DataElement>();

        for ( DataElement dataElement : getDataElementsWithGroupSets() )
        {
            if ( dataElement.getGroupSets() != null
                && new HashSet<DataElementGroupSet>( dataElement.getGroupSets() ).equals( groupSets ) )
            {
                dataElements.add( dataElement );
            }
        }

        return dataElements;
    }
    
    public Collection<DataElement> getDataElementsWithoutGroups()
    {
        return i18n( i18nService, dataElementStore.getDataElementsWithoutGroups() );
    }
    
    public Collection<DataElement> getDataElementsWithoutDataSets()
    {
        return i18n( i18nService, dataElementStore.getDataElementsWithoutDataSets() );
    }

    public boolean dataElementExists( int id )
    {
        return dataElementStore.dataElementExists( id );
    }
    
    public boolean dataElementCategoryOptionComboExists( int id )
    {
        return dataElementStore.dataElementCategoryOptionComboExists( id );
    }
    
    // -------------------------------------------------------------------------
    // CalculatedDataElement
    // -------------------------------------------------------------------------

    public Collection<CalculatedDataElement> getAllCalculatedDataElements()
    {
        return i18n( i18nService, dataElementStore.getAllCalculatedDataElements() );
    }

    public Collection<CalculatedDataElement> getCalculatedDataElements( final Collection<Integer> identifiers )
    {
        Collection<CalculatedDataElement> dataElements = getAllCalculatedDataElements();

        return identifiers == null ? dataElements : FilterUtils.filter( dataElements,
            new Filter<CalculatedDataElement>()
            {
                public boolean retain( CalculatedDataElement dataElement )
                {
                    return identifiers.contains( dataElement.getId() );
                }
            } );
    }

    public CalculatedDataElement getCalculatedDataElementByDataElement( DataElement dataElement )
    {
        return i18n( i18nService, dataElementStore.getCalculatedDataElementByDataElement( dataElement ) );
    }

    public Collection<CalculatedDataElement> getCalculatedDataElementsByDataElements(
        Collection<DataElement> dataElements )
    {
        return i18n( i18nService, dataElementStore.getCalculatedDataElementsByDataElements( dataElements ) );
    }

    public Map<DataElement, Integer> getDataElementFactors( CalculatedDataElement calculatedDataElement )
    {
        Map<DataElement, Integer> factorMap = new HashMap<DataElement, Integer>();

        Pattern pattern = Pattern.compile( "\\[(\\d+)\\]\\s*\\*\\s*(\\d+)" );

        // ---------------------------------------------------------------------
        // In readable form: \[(\d+)\]\s*\*\s*(\d+)
        // Meaning any expression on the form "[id] * factor"
        // ---------------------------------------------------------------------

        Matcher matcher = pattern.matcher( calculatedDataElement.getExpression().getExpression() );

        while ( matcher.find() )
        {
            // -----------------------------------------------------------------
            // Key: Datelementid
            // Value: Factor
            // -----------------------------------------------------------------

            factorMap.put( getDataElement( Integer.parseInt( matcher.group( 1 ) ) ), Integer.parseInt( matcher
                .group( 2 ) ) );
        }

        return factorMap;
    }

    public Map<String, Integer> getOperandFactors( CalculatedDataElement calculatedDataElement )
    {
        Map<String, Integer> factorMap = new HashMap<String, Integer>();

        Pattern pattern = Pattern.compile( "\\[(\\d+\\.\\d+)\\]\\s*\\*\\s*(-?\\d+)" );

        // ---------------------------------------------------------------------
        // In readable form: \[(\d+)\]\s*\*\s*(\d+)
        // Meaning any expression on the form "[id] * factor"
        // ---------------------------------------------------------------------

        Matcher matcher = pattern.matcher( calculatedDataElement.getExpression().getExpression() );

        while ( matcher.find() )
        {
            // -----------------------------------------------------------------
            // Key: Datelementid.optioncomboid
            // Value: Factor
            // -----------------------------------------------------------------

            factorMap.put( matcher.group( 1 ), Integer.parseInt( matcher.group( 2 ) ) );
        }

        return factorMap;
    }

    public Collection<String> getOperandIds( CalculatedDataElement calculatedDataElement )
    {
        Collection<String> operands = new ArrayList<String>();

        Pattern pattern = Pattern.compile( "\\[(\\d+\\.\\d+)\\]" );

        // ---------------------------------------------------------------------
        // In readable form: \[(\d+)\]\s*\*\s*(\d+)
        // Meaning any expression on the form "[id] * factor"
        // ---------------------------------------------------------------------

        Matcher matcher = pattern.matcher( calculatedDataElement.getExpression().getExpression() );

        while ( matcher.find() )
        {
            // -----------------------------------------------------------------
            // Datelementid.optioncomboid
            // -----------------------------------------------------------------

            operands.add( matcher.group( 1 ) );
        }

        return operands;
    }

    public Map<Integer, String> getCalculatedDataElementExpressionMap( Collection<Integer> identifiers )
    {
        Collection<CalculatedDataElement> dataElements = getCalculatedDataElements( identifiers );

        Map<Integer, String> map = new HashMap<Integer, String>();

        for ( CalculatedDataElement element : dataElements )
        {
            map.put( element.getId(), element.getExpression().getExpression() );
        }

        return map;
    }

    // -------------------------------------------------------------------------
    // DataElementGroup
    // -------------------------------------------------------------------------

    public int addDataElementGroup( DataElementGroup dataElementGroup )
    {
        if ( dataElementGroup.getUuid() == null )
        {
            dataElementGroup.setUuid( UUIdUtils.getUUId() );
        }

        int id = dataElementGroupStore.save( dataElementGroup );

        i18nService.addObject( dataElementGroup );

        return id;
    }

    public void updateDataElementGroup( DataElementGroup dataElementGroup )
    {
        dataElementGroupStore.update( dataElementGroup );

        i18nService.verify( dataElementGroup );
    }

    public void deleteDataElementGroup( DataElementGroup dataElementGroup )
    {
        i18nService.removeObject( dataElementGroup );

        dataElementGroupStore.delete( dataElementGroup );
    }

    public DataElementGroup getDataElementGroup( int id )
    {
        return i18n( i18nService, dataElementGroupStore.get( id ) );
    }

    public Collection<DataElementGroup> getDataElementGroups( final Collection<Integer> identifiers )
    {
        Collection<DataElementGroup> groups = getAllDataElementGroups();

        return identifiers == null ? groups : FilterUtils.filter( groups, new Filter<DataElementGroup>()
        {
            public boolean retain( DataElementGroup object )
            {
                return identifiers.contains( object.getId() );
            }
        } );
    }

    public DataElementGroup getDataElementGroup( String uuid )
    {
        return i18n( i18nService, dataElementGroupStore.getByUuid( uuid ) );
    }

    public Collection<DataElementGroup> getAllDataElementGroups()
    {
        return i18n( i18nService, dataElementGroupStore.getAll() );
    }

    public DataElementGroup getDataElementGroupByName( String name )
    {
        return i18n( i18nService, dataElementGroupStore.getByName( name ) );
    }

    public Collection<DataElementGroup> getGroupsContainingDataElement( DataElement dataElement )
    {
        Collection<DataElementGroup> groups = getAllDataElementGroups();

        Iterator<DataElementGroup> iterator = groups.iterator();

        while ( iterator.hasNext() )
        {
            if ( !iterator.next().getMembers().contains( dataElement ) )
            {
                iterator.remove();
            }
        }

        return groups;
    }

    // -------------------------------------------------------------------------
    // DataElementGroupSet
    // -------------------------------------------------------------------------

    public int addDataElementGroupSet( DataElementGroupSet groupSet )
    {
        int id = dataElementGroupSetStore.save( groupSet );

        i18nService.addObject( groupSet );

        return id;
    }

    public void updateDataElementGroupSet( DataElementGroupSet groupSet )
    {
        dataElementGroupSetStore.update( groupSet );

        i18nService.verify( groupSet );
    }

    public void deleteDataElementGroupSet( DataElementGroupSet groupSet )
    {
        i18nService.removeObject( groupSet );

        dataElementGroupSetStore.delete( groupSet );
    }

    public DataElementGroupSet getDataElementGroupSet( int id )
    {
        return i18n( i18nService, dataElementGroupSetStore.get( id ) );
    }

    public DataElementGroupSet getDataElementGroupSetByName( String name )
    {
        return i18n( i18nService, dataElementGroupSetStore.getByName( name ) );
    }

    public Collection<DataElementGroupSet> getAllDataElementGroupSets()
    {
        return i18n( i18nService, dataElementGroupSetStore.getAll() );
    }

    public Collection<DataElementGroupSet> getDataElementGroupSets( final Collection<Integer> identifiers )
    {
        Collection<DataElementGroupSet> groupSets = getAllDataElementGroupSets();

        return identifiers == null ? groupSets : FilterUtils.filter( groupSets, new Filter<DataElementGroupSet>()
        {
            public boolean retain( DataElementGroupSet object )
            {
                return identifiers.contains( object.getId() );
            }
        } );
    }

    // -------------------------------------------------------------------------
    // Operand
    // -------------------------------------------------------------------------
    
    public Collection<DataElementOperand> getAllGeneratedOperands()
    {
        return dataElementStore.getAllGeneratedOperands();
    }
}
