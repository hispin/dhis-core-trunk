package org.hisp.dhis.dataset;

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.source.Source;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DataSetServiceTest
    extends DhisSpringTest
{
    private DataSetService dataSetService;

    private PeriodType periodType;

    @Override
    public void setUpTest()
        throws Exception
    {
        dataSetService = (DataSetService) getBean( DataSetService.ID );

        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );
        
        periodType = PeriodType.getAvailablePeriodTypes().iterator().next();
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void assertEq( char uniqueCharacter, DataSet dataSet )
    {
        assertEquals( "DataSet" + uniqueCharacter, dataSet.getName() );
        assertEquals( "DataSetShort" + uniqueCharacter, dataSet.getShortName() );
        assertEquals( periodType, dataSet.getPeriodType() );
    }
    
    // -------------------------------------------------------------------------
    // DataSet
    // -------------------------------------------------------------------------

    @Test
    public void testAddDataSet()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        
        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );       
        
        dataSetA = dataSetService.getDataSet( idA );
        dataSetB = dataSetService.getDataSet( idB );
        
        assertEquals( idA, dataSetA.getId() );
        assertEq( 'A', dataSetA );
        
        assertEquals( idB, dataSetB.getId() );
        assertEq( 'B', dataSetB );
    }

    @Test
    public void testUpdateDataSet()
    {
        DataSet dataSet = createDataSet( 'A', periodType );
        
        int id = dataSetService.addDataSet( dataSet );
        
        dataSet = dataSetService.getDataSet( id );
        
        assertEq( 'A', dataSet );
        
        dataSet.setName( "DataSetB" );
        
        dataSetService.updateDataSet( dataSet );
        
        dataSet = dataSetService.getDataSet( id );
        
        assertEquals( dataSet.getName(), "DataSetB" );
    }

    @Test
    public void testDeleteAndGetDataSet()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        
        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );
        
        assertNotNull( dataSetService.getDataSet( idA ) );
        assertNotNull( dataSetService.getDataSet( idB ) );
        
        dataSetService.deleteDataSet( dataSetService.getDataSet( idA ) );
        
        assertNull( dataSetService.getDataSet( idA ) );
        assertNotNull( dataSetService.getDataSet( idB ) );

        dataSetService.deleteDataSet( dataSetService.getDataSet( idB ) );
        
        assertNull( dataSetService.getDataSet( idA ) );
        assertNull( dataSetService.getDataSet( idB ) );        
    }

    @Test
    public void testGetDataSetByName()
        throws Exception
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
    
        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );
    
        assertEquals( dataSetService.getDataSetByName( "DataSetA" ).getId(), idA );
        assertEquals( dataSetService.getDataSetByName( "DataSetB" ).getId(), idB );
        assertNull( dataSetService.getDataSetByName( "DataSetC" ) );
    }

    @Test
    public void testGetDataSetByShortName()
        throws Exception
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );

        assertEquals( dataSetService.getDataSetByShortName( "DataSetShortA" ).getId(), idA );
        assertEquals( dataSetService.getDataSetByShortName( "DataSetShortB" ).getId(), idB );
        assertNull( dataSetService.getDataSetByShortName( "DataSetShortC" ) );
    }

    @Test
    public void testGetAllDataSets()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
    
        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );
        
        Collection<DataSet> dataSets = dataSetService.getAllDataSets();
        
        assertEquals( dataSets.size(), 2 );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );            
    }

    @Test
    public void testGetDataSetsBySource()
    {
        OrganisationUnit sourceA = createOrganisationUnit( 'A' );
        OrganisationUnit sourceB = createOrganisationUnit( 'B' );
        OrganisationUnit sourceC = createOrganisationUnit( 'C' );
        
        organisationUnitService.addOrganisationUnit( sourceA );
        organisationUnitService.addOrganisationUnit( sourceB );
        organisationUnitService.addOrganisationUnit( sourceC );
        
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        DataSet dataSetC = createDataSet( 'C', periodType );
        
        dataSetA.getSources().add( sourceA );
        dataSetA.getSources().add( sourceB );
        
        dataSetB.getSources().add( sourceB );
        dataSetB.getSources().add( sourceC );        

        dataSetC.getSources().add( sourceA );
        dataSetC.getSources().add( sourceC );
        
        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );
        dataSetService.addDataSet( dataSetC );
        
        Collection<DataSet> dataSets = dataSetService.getDataSetsBySource( sourceA );
        
        assertEquals( 2, dataSets.size() );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetC ) );
        
        dataSets = dataSetService.getDataSetsBySource( sourceB );        

        assertEquals( 2, dataSets.size() );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );

        dataSets = dataSetService.getDataSetsBySource( sourceC );
        
        assertEquals( 2, dataSets.size() );
        assertTrue( dataSets.contains( dataSetB ) );
        assertTrue( dataSets.contains( dataSetC ) );        
    }

    @Test
    public void testGetDataSetsBySources()
    {
        OrganisationUnit unitA = createOrganisationUnit( 'A' );
        OrganisationUnit unitB = createOrganisationUnit( 'B' );  
        OrganisationUnit unitC = createOrganisationUnit( 'C' );  
        organisationUnitService.addOrganisationUnit( unitA );
        organisationUnitService.addOrganisationUnit( unitB );
        organisationUnitService.addOrganisationUnit( unitC );

        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        DataSet dataSetC = createDataSet( 'C', periodType );
        DataSet dataSetD = createDataSet( 'D', periodType );
        dataSetA.getSources().add( unitA );
        dataSetA.getSources().add( unitB );
        dataSetB.getSources().add( unitA );
        dataSetC.getSources().add( unitB );
        
        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );
        dataSetService.addDataSet( dataSetC );
        dataSetService.addDataSet( dataSetD );
        
        Collection<Source> sources = new HashSet<Source>();
        sources.add( unitA );
        sources.add( unitB );
        
        Collection<DataSet> dataSets = dataSetService.getDataSetsBySources( sources );

        assertEquals( 3, dataSets.size() );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );
        assertTrue( dataSets.contains( dataSetC ) );

        sources = new HashSet<Source>();
        sources.add( unitA );
        
        dataSets = dataSetService.getDataSetsBySources( sources );
        
        assertEquals( 2, dataSets.size() );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );
    }
    
    @Test
    public void testGetSourcesAssociatedWithDataSet()
    {
        OrganisationUnit sourceA = createOrganisationUnit( 'A' );
        OrganisationUnit sourceB = createOrganisationUnit( 'B' );
        OrganisationUnit sourceC = createOrganisationUnit( 'C' );
        OrganisationUnit sourceD = createOrganisationUnit( 'D' );
        OrganisationUnit sourceE = createOrganisationUnit( 'E' );
        OrganisationUnit sourceF = createOrganisationUnit( 'F' );
        
        organisationUnitService.addOrganisationUnit( sourceA );
        organisationUnitService.addOrganisationUnit( sourceB );
        organisationUnitService.addOrganisationUnit( sourceC );
        organisationUnitService.addOrganisationUnit( sourceD );
        organisationUnitService.addOrganisationUnit( sourceE );
        organisationUnitService.addOrganisationUnit( sourceF );
        
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        
        dataSetA.getSources().add( sourceA );
        dataSetA.getSources().add( sourceB );
        dataSetA.getSources().add( sourceC );

        dataSetB.getSources().add( sourceC );
        dataSetB.getSources().add( sourceD );
        dataSetB.getSources().add( sourceE );       
        
        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );

        Collection<Source> sources = new HashSet<Source>();
        
        sources.add( sourceA );
        sources.add( sourceB );
        sources.add( sourceD );
        sources.add( sourceE );
        
        assertEquals( 2, dataSetService.getSourcesAssociatedWithDataSet( dataSetA, sources ) );
        assertEquals( 2, dataSetService.getSourcesAssociatedWithDataSet( dataSetB, sources ) );
    }
}
