package org.hisp.dhis.indicator;

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Collection;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.system.util.UUIdUtils;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id: IndicatorStoreTest.java 3286 2007-05-07 18:05:21Z larshelg $
 */
@SuppressWarnings( "unchecked" )
public class IndicatorStoreTest
    extends DhisSpringTest
{
    private IndicatorStore indicatorStore;
    
    private GenericIdentifiableObjectStore<IndicatorType> indicatorTypeStore;

    // -------------------------------------------------------------------------
    // Set up/tear down
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
        throws Exception
    {
        indicatorStore = (IndicatorStore) getBean( IndicatorStore.ID );
        
        indicatorTypeStore = (GenericIdentifiableObjectStore<IndicatorType>) getBean( "org.hisp.dhis.indicator.IndicatorTypeStore" );
        
        indicatorService = (IndicatorService) getBean( IndicatorService.ID );
    }

    // -------------------------------------------------------------------------
    // Support methods
    // -------------------------------------------------------------------------

    private void assertEq( char uniqueCharacter, Indicator indicator )
    {
        assertEquals( "Indicator" + uniqueCharacter, indicator.getName() );
        assertEquals( "AlternativeName" + uniqueCharacter, indicator.getAlternativeName() );
        assertEquals( "ShortName" + uniqueCharacter, indicator.getShortName() );
        assertEquals( "Code" + uniqueCharacter, indicator.getCode() );
        assertEquals( "Description" + uniqueCharacter, indicator.getDescription() );
    }
    
    // -------------------------------------------------------------------------
    // IndicatorType
    // -------------------------------------------------------------------------

    @Test
    public void testAddIndicatorType()
        throws Exception
    {        
        IndicatorType typeA = new IndicatorType( "IndicatorTypeA", 100 );
        IndicatorType typeB = new IndicatorType( "IndicatorTypeB", 1 );
        IndicatorType typeC = new IndicatorType( "IndicatorTypeA", 100 );
        
        int idA = indicatorTypeStore.save( typeA );
        int idB = indicatorTypeStore.save( typeB );
        
        try
        {
            indicatorTypeStore.save( typeC );
            fail( "Expected unique constraint exception" );
        }
        catch ( Exception ex )
        {
        }

        typeA = indicatorTypeStore.get( idA );
        assertNotNull( typeA );
        assertEquals( idA, typeA.getId() );
        
        typeB = indicatorTypeStore.get( idB );
        assertNotNull( typeB );
        assertEquals( idB, typeB.getId() );
    }

    @Test
    public void testUpdateIndicatorType()
        throws Exception
    {
        IndicatorType typeA = new IndicatorType( "IndicatorTypeA", 100 );
        int idA = indicatorTypeStore.save( typeA );
        typeA = indicatorTypeStore.get( idA );
        assertEquals( typeA.getName(), "IndicatorTypeA" );
        
        typeA.setName( "IndicatorTypeB" );
        indicatorTypeStore.update( typeA );
        typeA = indicatorTypeStore.get( idA );
        assertNotNull( typeA );
        assertEquals( typeA.getName(), "IndicatorTypeB" );
    }

    @Test
    public void testGetAndDeleteIndicatorType()
        throws Exception
    {
        IndicatorType typeA = new IndicatorType( "IndicatorTypeA", 100 );
        IndicatorType typeB = new IndicatorType( "IndicatorTypeB", 1 );
        
        int idA = indicatorTypeStore.save( typeA );
        int idB = indicatorTypeStore.save( typeB );
        
        assertNotNull( indicatorTypeStore.get( idA ) );
        assertNotNull( indicatorTypeStore.get( idB ) );
        
        indicatorTypeStore.delete( typeA );

        assertNull( indicatorTypeStore.get( idA ) );
        assertNotNull( indicatorTypeStore.get( idB ) );

        indicatorTypeStore.delete( typeB );

        assertNull( indicatorTypeStore.get( idA ) );
        assertNull( indicatorTypeStore.get( idB ) );        
    }

    @Test
    public void testGetAllIndicatorTypes()
        throws Exception
    {
        IndicatorType typeA = new IndicatorType( "IndicatorTypeA", 100 );
        IndicatorType typeB = new IndicatorType( "IndicatorTypeB", 1 );
        
        indicatorTypeStore.save( typeA );
        indicatorTypeStore.save( typeB );
        
        Collection<IndicatorType> types = indicatorTypeStore.getAll();
        
        assertEquals( types.size(), 2 );
        assertTrue( types.contains( typeA ) );
        assertTrue( types.contains( typeB ) );
    }

    @Test
    public void testGetIndicatorTypeByName()
        throws Exception
    {
        IndicatorType typeA = new IndicatorType( "IndicatorTypeA", 100 );
        IndicatorType typeB = new IndicatorType( "IndicatorTypeB", 1 );
        
        int idA = indicatorTypeStore.save( typeA );
        int idB = indicatorTypeStore.save( typeB );
        
        assertNotNull( indicatorTypeStore.get( idA ) );
        assertNotNull( indicatorTypeStore.get( idB ) );
        
        typeA = indicatorTypeStore.getByName( "IndicatorTypeA" );
        assertNotNull( typeA );
        assertEquals( typeA.getId(), idA );
        
        IndicatorType typeC = indicatorTypeStore.getByName( "IndicatorTypeC" );
        assertNull( typeC );
    }
    
    
    // -------------------------------------------------------------------------
    // Indicator
    // -------------------------------------------------------------------------

    @Test
    public void testAddIndicator()
        throws Exception
    {        
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );
        
        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );
        Indicator indicatorC = createIndicator( 'A', type );
        
        int idA = indicatorStore.addIndicator( indicatorA );
        int idB = indicatorStore.addIndicator( indicatorB );
        
        try
        {
            indicatorStore.addIndicator( indicatorC );
            fail( "Expected unique constraint exception" );
        }
        catch ( Exception ex )
        {
        }

        indicatorA = indicatorStore.getIndicator( idA );
        assertNotNull( indicatorA );
        assertEq( 'A', indicatorA );
        
        indicatorB = indicatorStore.getIndicator( idB );
        assertNotNull( indicatorB );
        assertEq( 'B', indicatorB );
    }

    @Test
    public void testUpdateIndicator()
        throws Exception
    {
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        int idA = indicatorStore.addIndicator( indicatorA );
        indicatorA = indicatorStore.getIndicator( idA );
        assertEq( 'A', indicatorA );
        
        indicatorA.setName( "IndicatorB" );
        indicatorStore.updateIndicator( indicatorA );
        indicatorA = indicatorStore.getIndicator( idA );
        assertNotNull( indicatorA );
        assertEquals( indicatorA.getName(), "IndicatorB" );
    }

    @Test
    public void testGetAndDeleteIndicator()
        throws Exception
    {
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );

        int idA = indicatorStore.addIndicator( indicatorA );
        int idB = indicatorStore.addIndicator( indicatorB );
        
        assertNotNull( indicatorStore.getIndicator( idA ) );
        assertNotNull( indicatorStore.getIndicator( idB ) );
        
        indicatorStore.deleteIndicator( indicatorA );

        assertNull( indicatorStore.getIndicator( idA ) );
        assertNotNull( indicatorStore.getIndicator( idB ) );

        indicatorStore.deleteIndicator( indicatorB );

        assertNull( indicatorStore.getIndicator( idA ) );
        assertNull( indicatorStore.getIndicator( idB ) );        
    }

    @Test
    public void testGetIndicatorByUUID()
        throws Exception
    {
        String uuid = UUIdUtils.getUUId();
        
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );
        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        indicatorA.setUuid( uuid );
        
        indicatorStore.addIndicator( indicatorA );
        
        indicatorA = indicatorStore.getIndicator( uuid );
        
        assertNotNull( indicatorA );
        assertEquals( indicatorA.getUuid(), uuid );
    }

    @Test
    public void testGetAllIndicators()
        throws Exception
    {
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );

        indicatorStore.addIndicator( indicatorA );
        indicatorStore.addIndicator( indicatorB );
        
        Collection<Indicator> indicators = indicatorStore.getAllIndicators();
        
        assertEquals( indicators.size(), 2 );
        assertTrue( indicators.contains( indicatorA ) );
        assertTrue( indicators.contains( indicatorB ) );
    }

    @Test
    public void testGetIndicatorByName()
        throws Exception
    {
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );

        int idA = indicatorStore.addIndicator( indicatorA );
        int idB = indicatorStore.addIndicator( indicatorB );
        
        assertNotNull( indicatorStore.getIndicator( idA ) );
        assertNotNull( indicatorStore.getIndicator( idB ) );
        
        indicatorA = indicatorStore.getIndicatorByName( "IndicatorA" );
        assertNotNull( indicatorA );
        assertEq( 'A', indicatorA );
        
        Indicator indicatorC = indicatorStore.getIndicatorByName( "IndicatorC" );
        assertNull( indicatorC );
    }    

    @Test
    public void testGetIndicatorByAlternativeName()
        throws Exception
    {
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );

        int idA = indicatorStore.addIndicator( indicatorA );
        int idB = indicatorStore.addIndicator( indicatorB );
        
        assertNotNull( indicatorStore.getIndicator( idA ) );
        assertNotNull( indicatorStore.getIndicator( idB ) );
        
        indicatorA = indicatorStore.getIndicatorByAlternativeName( "AlternativeNameA" );
        assertNotNull( indicatorA );
        assertEq( 'A', indicatorA );
        
        Indicator indicatorC = indicatorStore.getIndicatorByAlternativeName( "AlternativeNameC" );
        assertNull( indicatorC );
    }

    @Test
    public void testGetIndicatorByShortName()
        throws Exception
    {
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );
    
        int idA = indicatorStore.addIndicator( indicatorA );
        int idB = indicatorStore.addIndicator( indicatorB );
        
        assertNotNull( indicatorStore.getIndicator( idA ) );
        assertNotNull( indicatorStore.getIndicator( idB ) );
        
        indicatorA = indicatorStore.getIndicatorByShortName( "ShortNameA" );
        assertNotNull( indicatorA );
        assertEq( 'A', indicatorA );
        
        Indicator indicatorC = indicatorStore.getIndicatorByShortName( "ShortNameC" );
        assertNull( indicatorC );
    }    

    @Test
    public void testGetIndicatorsWithGroupSets()
        throws Exception
    {
        IndicatorGroupSet groupSetA = createIndicatorGroupSet( 'A' );
        
        indicatorService.addIndicatorGroupSet( groupSetA );
        
        IndicatorType type = new IndicatorType( "IndicatorType", 100 );

        indicatorTypeStore.save( type );
        
        Indicator indicatorA = createIndicator( 'A', type );
        Indicator indicatorB = createIndicator( 'B', type );
        Indicator indicatorC = createIndicator( 'C', type );
        Indicator indicatorD = createIndicator( 'D', type );
    
        indicatorB.getGroupSets().add( groupSetA );
        indicatorD.getGroupSets().add( groupSetA );        
        
        indicatorStore.addIndicator( indicatorA );
        indicatorStore.addIndicator( indicatorB );
        indicatorStore.addIndicator( indicatorC );
        indicatorStore.addIndicator( indicatorD );
        
        Collection<Indicator> indicators = indicatorStore.getIndicatorsWithGroupSets();
        
        assertEquals( 2, indicators.size() );
        assertTrue( indicators.contains( indicatorB ) );
        assertTrue( indicators.contains( indicatorD ) );
    }
}
