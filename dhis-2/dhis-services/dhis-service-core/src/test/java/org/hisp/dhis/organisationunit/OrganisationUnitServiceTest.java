package org.hisp.dhis.organisationunit;

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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.DhisSpringTest;
import org.junit.Test;

/**
 * @author Kristian Nordal
 * @version $Id: OrganisationUnitServiceTest.java 6251 2008-11-10 14:37:05Z larshelg $
 */
public class OrganisationUnitServiceTest
    extends DhisSpringTest
{
    private OrganisationUnitService organisationUnitService;

    private OrganisationUnitGroupService organisationUnitGroupService;
    
    @Override
    public void setUpTest()
        throws Exception
    {
        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );

        organisationUnitGroupService = (OrganisationUnitGroupService) getBean( OrganisationUnitGroupService.ID );
        
        organisationUnitService.removeOrganisationUnitHierarchies( OrganisationUnitHierarchyVerifier.START_OF_TIME );
    }

    // -------------------------------------------------------------------------
    // OrganisationUnit
    // -------------------------------------------------------------------------

    @Test
    public void testBasicOrganisationUnitCoarseGrained()
        throws Exception
    {
        // Single OrganisationUnit
        String organisationUnitName1 = "organisationUnitName1";
        OrganisationUnit organisationUnit1 = new OrganisationUnit( organisationUnitName1, "shortName1",
            "organisationUnitCode1", new Date(), new Date(), true, "comment" );

        int id1 = organisationUnitService.addOrganisationUnit( organisationUnit1 );

        // assert getOrganisationUnit
        assertNotNull( organisationUnitService.getOrganisationUnit( id1 ) );

        assertNull( organisationUnitService.getOrganisationUnit( -1 ) );

        // OrganisationUnit with parent
        String organisationUnitName2 = "organisationUnitName2";
        OrganisationUnit organisationUnit2 = new OrganisationUnit( organisationUnitName2, organisationUnit1,
            "shortName2", "organisationUnitCode2", new Date(), new Date(), true, "comment" );
        
        int id2 = organisationUnitService.addOrganisationUnit( organisationUnit2 );

        assertTrue( organisationUnitService.getOrganisationUnit( id2 ).getParent().getId() == id1 );
        
        organisationUnitService.deleteOrganisationUnit( organisationUnitService.getOrganisationUnit( id2 ) );
        
        organisationUnitService.deleteOrganisationUnit( organisationUnitService.getOrganisationUnit( id1 ) );
        
        // assert delOrganisationUnit
        assertNull( organisationUnitService.getOrganisationUnit( id1 ) );
        assertNull( organisationUnitService.getOrganisationUnit( id2 ) );
    }

    @Test
    public void testUpdateOrganisationUnit()
        throws Exception
    {
        String name = "name";
        String shortName = "shortName";
        String updatedName = "updatedName";
        String updatedShortName = "updatedShortName";

        OrganisationUnit organisationUnit = new OrganisationUnit( name, shortName, "organisationUnitCode", new Date(),
            new Date(), true, "comment" );

        int id = organisationUnitService.addOrganisationUnit( organisationUnit );

        organisationUnit.setName( updatedName );
        organisationUnit.setShortName( updatedShortName );

        organisationUnitService.updateOrganisationUnit( organisationUnit );

        OrganisationUnit updatedOrganisationUnit = organisationUnitService.getOrganisationUnit( id );

        assertEquals( updatedOrganisationUnit.getName(), updatedName );
        assertEquals( updatedOrganisationUnit.getShortName(), updatedShortName );
    }

    @Test
    public void testGetOrganisationUnitWithChildren()
        throws Exception
    {
        OrganisationUnit unit1 = new OrganisationUnit( "name1", "shortName1", "organisationUnitCode1", new Date(),
            new Date(), true, "comment" );
        OrganisationUnit unit2 = new OrganisationUnit( "name2", unit1, "shortName2", "organisationUnitCode2",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit3 = new OrganisationUnit( "name3", unit2, "shortName3", "organisationUnitCode3",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit4 = new OrganisationUnit( "name4", "shortName4", "organisationUnitCode4", new Date(),
            new Date(), true, "comment" );

        int id1 = organisationUnitService.addOrganisationUnit( unit1 );
        unit1.getChildren().add( unit2 );
        organisationUnitService.addOrganisationUnit( unit2 );
        unit2.getChildren().add( unit3 );
        organisationUnitService.addOrganisationUnit( unit3 );
        organisationUnitService.addOrganisationUnit( unit4 );

        assertTrue( organisationUnitService.getOrganisationUnitWithChildren( id1 ).size() == 3 );
    }

    @Test
    public void testGetOrganisationUnitsByFields()
        throws Exception
    {
        String oU1Name = "OU1name";
        String oU2Name = "OU2name";
        String oU3Name = "OU3name";
        String oU1ShortName = "OU1ShortName";
        String oU2ShortName = "OU2ShortName";
        String oU3ShortName = "OU3ShortName";
        String oU1Code = "OU1Code";
        String oU2Code = "OU2Code";
        String oU3Code = "OU3Code";

        OrganisationUnit organisationUnit1 = new OrganisationUnit( oU1Name, null, oU1ShortName, oU1Code, null, null,
            true, null );
        OrganisationUnit organisationUnit2 = new OrganisationUnit( oU2Name, null, oU2ShortName, oU2Code, null, null,
            true, null );
        OrganisationUnit organisationUnit3 = new OrganisationUnit( oU3Name, null, oU3ShortName, oU3Code, null, null,
            false, null );

        organisationUnitService.addOrganisationUnit( organisationUnit1 );
        organisationUnitService.addOrganisationUnit( organisationUnit2 );
        organisationUnitService.addOrganisationUnit( organisationUnit3 );

        OrganisationUnit unit1 = organisationUnitService.getOrganisationUnitByName( oU1Name );
        assertEquals( unit1.getName(), oU1Name );

        OrganisationUnit unit2 = organisationUnitService.getOrganisationUnitByShortName( oU2ShortName );
        assertEquals( unit2.getShortName(), oU2ShortName );

        OrganisationUnit unit4 = organisationUnitService.getOrganisationUnitByName( "foo" );
        assertNull( unit4 );
    }

    @Test
    public void testGetOrganisationUnitByUUID()
        throws Exception
    {
        OrganisationUnit unit1 = new OrganisationUnit( "name1", "shortName1", "organisationUnitCode1", new Date(), new Date(), true, "comment" );        
        int id1 = organisationUnitService.addOrganisationUnit( unit1 );
        
        unit1 = organisationUnitService.getOrganisationUnit( id1 );        
        String uuid = unit1.getUuid();
        
        OrganisationUnit unit2 = organisationUnitService.getOrganisationUnit( uuid );
        
        assertEquals( unit1, unit2 );
    }

    @Test
    public void testGetOrganisationUnitGraph()
        throws Exception
    {
        OrganisationUnit organisationUnit1 = new OrganisationUnit( "Foo", "shortName1", "organisationUnitCode1",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit organisationUnit2 = new OrganisationUnit( "Bar", organisationUnit1, "shortName2",
            "organisationUnitCode2", new Date(), new Date(), true, "comment" );
        OrganisationUnit organisationUnit3 = new OrganisationUnit( "Foobar", organisationUnit2, "shortName3",
            "organisationUnitCode3", new Date(), new Date(), true, "comment" );

        int orgId1 = organisationUnitService.addOrganisationUnit( organisationUnit1 );
        int orgId2 = organisationUnitService.addOrganisationUnit( organisationUnit2 );
        int orgId3 = organisationUnitService.addOrganisationUnit( organisationUnit3 );

        List<OrganisationUnit> graph = organisationUnitService.getOrganisationUnitBranch( orgId3 );
        assertNotNull( graph );
        assertEquals( 3, graph.size() );
        OrganisationUnit orgUnit = graph.get( 0 );
        assertEquals( orgId1, orgUnit.getId() );
        orgUnit = graph.get( 1 );
        assertEquals( orgId2, orgUnit.getId() );
        orgUnit = graph.get( 2 );
        assertEquals( orgId3, orgUnit.getId() );
    }

    @Test
    public void testGetAllOrganisationUnitsAndGetRootOrganisationUnit()
        throws Exception
    {
        // creating a tree with two roots ( id1 and id4 )

        OrganisationUnit unit1 = new OrganisationUnit( "OU1name", "OU1sname", "OU1code", null, null, true, null );
        OrganisationUnit unit2 = new OrganisationUnit( "OU2name", unit1, "OU2sname", "OU2code", null, null, true, null );
        OrganisationUnit unit3 = new OrganisationUnit( "OU3name", unit1, "OU3sname", "OU3code", null, null, true, null );
        OrganisationUnit unit4 = new OrganisationUnit( "OU4name", "OU4sname", "OU4code", null, null, true, null );
        OrganisationUnit unit5 = new OrganisationUnit( "OU5name", unit4, "OU5sname", "OU5code", null, null, true, null );

        organisationUnitService.addOrganisationUnit( unit1 );
        organisationUnitService.addOrganisationUnit( unit2 );
        organisationUnitService.addOrganisationUnit( unit3 );
        organisationUnitService.addOrganisationUnit( unit4 );
        organisationUnitService.addOrganisationUnit( unit5 );

        Collection<OrganisationUnit> units = organisationUnitService.getAllOrganisationUnits();
        
        assertNotNull( units );
        assertEquals( 5, units.size() );
        assertTrue( units.contains( unit1 ) );
        assertTrue( units.contains( unit2 ) );
        assertTrue( units.contains( unit3 ) );
        assertTrue( units.contains( unit4 ) );
        assertTrue( units.contains( unit5 ) );

        units = organisationUnitService.getRootOrganisationUnits();

        assertNotNull( units );
        assertEquals( 2, units.size() );
        assertTrue( units.contains( unit1 ) );
        assertTrue( units.contains( unit4 ) );
    }

    @Test
    public void testGetOrganisationUnitsAtLevel()
        throws Exception
    {
        OrganisationUnit unit1 = new OrganisationUnit( "orgUnitName1", "shortName1", "organisationUnitCode1",
            new Date(), new Date(), true, "comment" );
        organisationUnitService.addOrganisationUnit( unit1 );

        OrganisationUnit unit2 = new OrganisationUnit( "orgUnitName2", unit1, "shortName2", "organisationUnitCode2",
            new Date(), new Date(), true, "comment" );
        unit1.getChildren().add( unit2 );
        organisationUnitService.addOrganisationUnit( unit2 );

        OrganisationUnit unit3 = new OrganisationUnit( "orgUnitName3", unit2, "shortName3", "organisationUnitCode3",
            new Date(), new Date(), true, "comment" );
        unit2.getChildren().add( unit3 );
        organisationUnitService.addOrganisationUnit( unit3 );

        OrganisationUnit unit4 = new OrganisationUnit( "orgUnitName4", unit2, "shortName4", "organisationUnitCode4",
            new Date(), new Date(), true, "comment" );
        unit2.getChildren().add( unit4 );
        organisationUnitService.addOrganisationUnit( unit4 );

        OrganisationUnit unit5 = new OrganisationUnit( "orgUnitName5", unit2, "shortName5", "organisationUnitCode5",
            new Date(), new Date(), true, "comment" );
        unit2.getChildren().add( unit5 );
        organisationUnitService.addOrganisationUnit( unit5 );

        OrganisationUnit unit6 = new OrganisationUnit( "orgUnitName6", unit3, "shortName6", "organisationUnitCode6",
            new Date(), new Date(), true, "comment" );
        unit3.getChildren().add( unit6 );
        organisationUnitService.addOrganisationUnit( unit6 );

        OrganisationUnit unit7 = new OrganisationUnit( "orgUnitName7", "shortName7", "organisationUnitCode7",
            new Date(), new Date(), true, "comment" );
        organisationUnitService.addOrganisationUnit( unit7 );

        assertTrue( organisationUnitService.getOrganisationUnitsAtLevel( 1 ).size() == 2 );
        assertTrue( organisationUnitService.getOrganisationUnitsAtLevel( 3 ).size() == 3 );
        assertTrue( organisationUnitService.getNumberOfOrganisationalLevels() == 4 );
        assertTrue( organisationUnitService.getLevelOfOrganisationUnit( unit4 ) == 3 );
        assertTrue( organisationUnitService.getLevelOfOrganisationUnit( unit1 ) == 1 );
        assertTrue( organisationUnitService.getLevelOfOrganisationUnit( unit6 ) == 4 );
    }

    @Test
    public void testGetOrganisationUnitAtLevelAndBranch()
        throws Exception
    {
        OrganisationUnit unitA = createOrganisationUnit( 'A' );
        OrganisationUnit unitB = createOrganisationUnit( 'B', unitA );
        OrganisationUnit unitC = createOrganisationUnit( 'C', unitA );
        OrganisationUnit unitD = createOrganisationUnit( 'D', unitB );
        OrganisationUnit unitE = createOrganisationUnit( 'E', unitB );
        OrganisationUnit unitF = createOrganisationUnit( 'F', unitC );
        OrganisationUnit unitG = createOrganisationUnit( 'G', unitC );
        OrganisationUnit unitH = createOrganisationUnit( 'H', unitD );
        OrganisationUnit unitI = createOrganisationUnit( 'I', unitD );
        OrganisationUnit unitJ = createOrganisationUnit( 'J', unitE );
        OrganisationUnit unitK = createOrganisationUnit( 'K', unitE );
        OrganisationUnit unitL = createOrganisationUnit( 'L', unitF );
        OrganisationUnit unitM = createOrganisationUnit( 'M', unitF );
        OrganisationUnit unitN = createOrganisationUnit( 'N', unitG );
        OrganisationUnit unitO = createOrganisationUnit( 'O', unitG );
        
        unitA.getChildren().add( unitB );
        unitA.getChildren().add( unitC );
        unitB.getChildren().add( unitD );
        unitB.getChildren().add( unitE );
        unitC.getChildren().add( unitF );
        unitC.getChildren().add( unitG );
        unitD.getChildren().add( unitH );
        unitD.getChildren().add( unitI );
        unitE.getChildren().add( unitJ );
        unitE.getChildren().add( unitK );
        unitF.getChildren().add( unitL );
        unitF.getChildren().add( unitM );
        unitG.getChildren().add( unitN );
        unitG.getChildren().add( unitO );
        
        organisationUnitService.addOrganisationUnit( unitA );
        organisationUnitService.addOrganisationUnit( unitB );
        organisationUnitService.addOrganisationUnit( unitC );
        organisationUnitService.addOrganisationUnit( unitD );
        organisationUnitService.addOrganisationUnit( unitE );
        organisationUnitService.addOrganisationUnit( unitF );
        organisationUnitService.addOrganisationUnit( unitG );
        organisationUnitService.addOrganisationUnit( unitH );
        organisationUnitService.addOrganisationUnit( unitI );
        organisationUnitService.addOrganisationUnit( unitJ );
        organisationUnitService.addOrganisationUnit( unitK );
        organisationUnitService.addOrganisationUnit( unitL );
        organisationUnitService.addOrganisationUnit( unitM );
        organisationUnitService.addOrganisationUnit( unitN );
        organisationUnitService.addOrganisationUnit( unitO );
        
        assertTrue( equals( organisationUnitService.getOrganisationUnitsAtLevel( 2, unitB ), unitB ) );
        assertTrue( equals( organisationUnitService.getOrganisationUnitsAtLevel( 3, unitB ), unitD, unitE ) );
        assertTrue( equals( organisationUnitService.getOrganisationUnitsAtLevel( 4, unitB ), unitH, unitI, unitJ, unitK ) );
    }
    
    // -------------------------------------------------------------------------
    // OrganisationUnitGroup
    // -------------------------------------------------------------------------

    @Test
    public void testAddAndDelOrganisationUnitGroup()
        throws Exception
    {
        OrganisationUnitGroup organisationUnitGroup1 = new OrganisationUnitGroup( "OUGname" );

        int id1 = organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup1 );

        // assert getOrganisationUnitGroup
        assertNotNull( organisationUnitGroupService.getOrganisationUnitGroup( id1 ) );

        assertEquals( organisationUnitGroupService.getOrganisationUnitGroup( id1 ).getName(), "OUGname" );

        organisationUnitGroupService.deleteOrganisationUnitGroup( organisationUnitGroupService
            .getOrganisationUnitGroup( id1 ) );

        // assert delOrganisationUnitGroup
        assertNull( organisationUnitGroupService.getOrganisationUnitGroup( id1 ) );
    }

    @Test
    public void testUpdateOrganisationUnitGroup()
        throws Exception
    {
        OrganisationUnitGroup organisationUnitGroup = new OrganisationUnitGroup( "OUGname" );

        OrganisationUnit organisationUnit1 = new OrganisationUnit( "OU1name", null, "OU1sname", "OU1code", null, null,
            true, null );
        OrganisationUnit organisationUnit2 = new OrganisationUnit( "OU2name", null, "OU2sname", "OU2code", null, null,
            true, null );

        organisationUnitGroup.getMembers().add( organisationUnit1 );
        organisationUnitGroup.getMembers().add( organisationUnit2 );

        organisationUnitService.addOrganisationUnit( organisationUnit1 );
        organisationUnitService.addOrganisationUnit( organisationUnit2 );

        int ougid = organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup );

        assertTrue( organisationUnitGroupService.getOrganisationUnitGroup( ougid ).getMembers().size() == 2 );
        
        organisationUnitGroup.getMembers().remove( organisationUnit1 );

        organisationUnitGroupService.updateOrganisationUnitGroup( organisationUnitGroup );

        assertTrue( organisationUnitGroupService.getOrganisationUnitGroup( ougid ).getMembers().size() == 1 );
    }

    @Test
    public void testGetAllOrganisationUnitGroups()
        throws Exception
    {
        OrganisationUnitGroup group1 = new OrganisationUnitGroup( "organisationUnitGroupName1" );
        int gid1 = organisationUnitGroupService.addOrganisationUnitGroup( group1 );

        OrganisationUnitGroup group2 = new OrganisationUnitGroup( "organisationUnitGroupName2" );
        int gid2 = organisationUnitGroupService.addOrganisationUnitGroup( group2 );

        OrganisationUnitGroup group3 = new OrganisationUnitGroup( "organisationUnitGroupName3" );
        int gid3 = organisationUnitGroupService.addOrganisationUnitGroup( group3 );

        OrganisationUnitGroup group4 = new OrganisationUnitGroup( "organisationUnitGroupName4" );
        int gid4 = organisationUnitGroupService.addOrganisationUnitGroup( group4 );

        Iterator<OrganisationUnitGroup> iterator = organisationUnitGroupService.getAllOrganisationUnitGroups().iterator();

        OrganisationUnitGroup organisationUnitGroup1 = iterator.next();
        assertTrue( organisationUnitGroup1.getId() == gid1 );

        OrganisationUnitGroup organisationUnitGroup2 = iterator.next();
        assertTrue( organisationUnitGroup2.getId() == gid2 );

        OrganisationUnitGroup organisationUnitGroup3 = iterator.next();
        assertTrue( organisationUnitGroup3.getId() == gid3 );

        OrganisationUnitGroup organisationUnitGroup4 = iterator.next();
        assertTrue( organisationUnitGroup4.getId() == gid4 );
    }

    @Test
    public void testGetOrganisationUnitGroupByName()
        throws Exception
    {
        String oUG1Name = "OUG1Name";
        String oUG2Name = "OUG2Name";

        OrganisationUnitGroup organisationUnitGroup1 = new OrganisationUnitGroup( oUG1Name );
        OrganisationUnitGroup organisationUnitGroup2 = new OrganisationUnitGroup( oUG2Name );

        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup1 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup2 );

        OrganisationUnitGroup group1 = organisationUnitGroupService.getOrganisationUnitGroupByName( oUG1Name );
        assertEquals( group1.getName(), oUG1Name );

        OrganisationUnitGroup group2 = organisationUnitGroupService.getOrganisationUnitGroupByName( oUG2Name );
        assertEquals( group2.getName(), oUG2Name );
    }

    @Test
    public void testGetOrganisationUnitGroupByUUID()
        throws Exception
    {
        OrganisationUnitGroup group1 = new OrganisationUnitGroup( "name1" );        
        int id1 = organisationUnitGroupService.addOrganisationUnitGroup( group1 );
        
        group1 = organisationUnitGroupService.getOrganisationUnitGroup( id1 );        
        String uuid = group1.getUuid();
        
        OrganisationUnitGroup group2 = organisationUnitGroupService.getOrganisationUnitGroup( uuid );
        
        assertEquals( group1, group2 );
    }

    // -------------------------------------------------------------------------
    // OrganisationUnitHierarchy
    // -------------------------------------------------------------------------

    @Test
    public void testAddGetOrganisationUnitHierarchy()
        throws Exception
    {
        // creates a tree
        OrganisationUnit unit1 = new OrganisationUnit( "orgUnitName1", "shortName1", "organisationUnitCode1",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit2 = new OrganisationUnit( "orgUnitName2", unit1, "shortName2", "organisationUnitCode2",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit3 = new OrganisationUnit( "orgUnitName3", unit1, "shortName3", "organisationUnitCode3",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit4 = new OrganisationUnit( "orgUnitName4", unit2, "shortName4", "organisationUnitCode4",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit5 = new OrganisationUnit( "orgUnitName5", unit2, "shortName5", "organisationUnitCode5",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit6 = new OrganisationUnit( "orgUnitName6", unit5, "shortName6", "organisationUnitCode6",
            new Date(), new Date(), true, "comment" );

        organisationUnitService.addOrganisationUnit( unit1 );
        int id2 = organisationUnitService.addOrganisationUnit( unit2 );
        organisationUnitService.addOrganisationUnit( unit3 );
        int id4 = organisationUnitService.addOrganisationUnit( unit4 );
        int id5 = organisationUnitService.addOrganisationUnit( unit5 );
        int id6 = organisationUnitService.addOrganisationUnit( unit6 );

        Calendar calendar = Calendar.getInstance();
        calendar.set( 2005, Calendar.JULY, 1 );
        Date date1 = calendar.getTime();
        calendar.set( 2005, Calendar.AUGUST, 1 );
        Date date2 = calendar.getTime();
        calendar.set( 2005, Calendar.SEPTEMBER, 1 );
        Date date3 = calendar.getTime();

        // creates a hierarchy-version
        organisationUnitService.addOrganisationUnitHierarchy( date2 );

        // asserts hierarchy-version is created and returns id
        Collection<OrganisationUnitHierarchy> hierarchies = organisationUnitService.getOrganisationUnitHierarchies(
            date1, date3 );

        OrganisationUnitHierarchy hierarchy = hierarchies.iterator().next();

        // retrieves children from hierarchyVersion ver_id and parentId id2
        Collection<Integer> children1 = organisationUnitService.getChildren( hierarchy.getId(), unit2.getId() );
        
        // assert 4, 5, 6 are children of 2
        assertEquals( 4, children1.size() );
        assertTrue( children1.contains( id2 ) );
        assertTrue( children1.contains( id4 ) );
        assertTrue( children1.contains( id5 ) );
        assertTrue( children1.contains( id6 ) );
                
        // retrieves children from hierarchyVersion ver_id and parentId id1
        Collection<Integer> children2 = organisationUnitService.getChildren( hierarchy.getId(), unit1.getId() );

        // assert the number of children
        assertTrue( children2.size() == 6 );

        // retrieves children from hierarchyVersion ver_id and parentId id5
        Collection<Integer> children3 = organisationUnitService.getChildren( hierarchy.getId(), unit5.getId() );
        
        // assert 6 is children of 5
        assertEquals( 2, children3.size() );
        assertTrue( children3.contains( id5 ) );
        assertTrue( children3.contains( id6 ) );        
    }

    @Test
    public void testGetLatestOrganisationUnitHierarchy()
    {
        Calendar cal = Calendar.getInstance();
        
        cal.set( 1990, Calendar.JANUARY, 1 );
        
        organisationUnitService.addOrganisationUnitHierarchy( cal.getTime() );
        
        cal.set( 1995, Calendar.JANUARY, 1 );
        
        int idB = organisationUnitService.addOrganisationUnitHierarchy( cal.getTime() );
        
        cal.set( 1985, Calendar.JANUARY, 1 );
        
        organisationUnitService.addOrganisationUnitHierarchy( cal.getTime() );
        
        OrganisationUnitHierarchy hierarchy = organisationUnitService.getLatestOrganisationUnitHierarchy();
        
        assertEquals( hierarchy.getId(), idB );
    }

    @Test
    public void testAddOrganisationUnitHierarchiesOnSameDate()
        throws Exception
    {
        OrganisationUnit unit1 = new OrganisationUnit( "orgUnitName1", "shortName1", "organisationUnitCode1",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit2 = new OrganisationUnit( "orgUnitName2", unit1, "shortName2", "organisationUnitCode2",
            new Date(), new Date(), true, "comment" );
        organisationUnitService.addOrganisationUnit( unit1 );
        organisationUnitService.addOrganisationUnit( unit2 );

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set( 2005, Calendar.JULY, 1 );
        Date startDate = calendar.getTime();
        calendar.set( 2005, Calendar.SEPTEMBER, 1 );
        Date endDate = calendar.getTime();
        calendar.set( 2005, Calendar.AUGUST, 1 );
        Date date = calendar.getTime();

        int id1 = organisationUnitService.addOrganisationUnitHierarchy( date );

        Collection<OrganisationUnitHierarchy> c1 = organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate );
        Iterator<OrganisationUnitHierarchy> i1 = c1.iterator();
        OrganisationUnitHierarchy h1 = i1.next();

        assertTrue( c1.size() == 1 );
        assertTrue( h1.getId() == id1 );

        OrganisationUnit unit3 = new OrganisationUnit( "orgUnitName3", "shortName3", "organisationUnitCode3",
            new Date(), new Date(), true, "comment" );
        organisationUnitService.addOrganisationUnit( unit3 );

        organisationUnitService.addOrganisationUnitHierarchy( date );

        OrganisationUnit unit4 = new OrganisationUnit( "orgUnitName4", "shortName4", "organisationUnitCode4",
            new Date(), new Date(), true, "comment" );
        organisationUnitService.addOrganisationUnit( unit4 );

        int id3 = organisationUnitService.addOrganisationUnitHierarchy( date );

        Collection<OrganisationUnitHierarchy> c2 = organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate );
        Iterator<OrganisationUnitHierarchy> i2 = c2.iterator();
        OrganisationUnitHierarchy h2 = i2.next();

        assertTrue( c2.size() == 1 );
        assertTrue( h2.getId() == id3 );

    }

    @Test
    public void testGetOrganisationUnitHierarchies()
        throws Exception
    {
        // creates a tree
        OrganisationUnit unit1 = new OrganisationUnit( "orgUnitName1", "shortName1", "organisationUnitCode1",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit2 = new OrganisationUnit( "orgUnitName2", unit1, "shortName2", "organisationUnitCode2",
            new Date(), new Date(), true, "comment" );
        organisationUnitService.addOrganisationUnit( unit1 );
        organisationUnitService.addOrganisationUnit( unit2 );

        // creates a start and end-date for a period
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set( 2005, Calendar.JULY, 1 );
        Date startDate = calendar.getTime();
        calendar.set( 2005, Calendar.SEPTEMBER, 1 );
        Date endDate = calendar.getTime();

        // creates a HierarchyVersion
        calendar.set( 2005, Calendar.MAY, 15 );
        organisationUnitService.addOrganisationUnitHierarchy( calendar.getTime() );

        // creates a HierarchyVersion
        calendar.set( 2005, Calendar.JUNE, 15 );
        Date jun15 = calendar.getTime();
        int ver_id2 = organisationUnitService.addOrganisationUnitHierarchy( jun15 );

        // asserts getOrganisationUnitHierarchies
        Iterator<OrganisationUnitHierarchy> iterator1 = organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate ).iterator();
        OrganisationUnitHierarchy hierVerA1 = iterator1.next();
        assertTrue( hierVerA1.getId() == ver_id2 );
        assertTrue( iterator1.hasNext() == false );

        // creates a HierarchyVersiony
        calendar.set( 2005, Calendar.JULY, 15 );
        Date jul15 = calendar.getTime();
        int ver_id3 = organisationUnitService.addOrganisationUnitHierarchy( jul15 );

        // asserts getOrganisationUnitHierarchies
        Iterator<OrganisationUnitHierarchy> iterator2 = organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate ).iterator();
        OrganisationUnitHierarchy hierVerB1 = iterator2.next();
        OrganisationUnitHierarchy hierVerB2 = iterator2.next();
        assertTrue( hierVerB1.getId() == ver_id2 );
        assertTrue( hierVerB2.getId() == ver_id3 );
        assertTrue( iterator2.hasNext() == false );

        // creates a HierarchyVersion
        calendar.set( 2005, Calendar.AUGUST, 15 );
        int ver_id4 = organisationUnitService.addOrganisationUnitHierarchy( calendar.getTime() );

        // creates a HierarchyVersion
        calendar.set( 2005, Calendar.SEPTEMBER, 15 );
        organisationUnitService.addOrganisationUnitHierarchy( calendar.getTime() );

        // asserts right number of HierarchyVersions are retrieved
        assertTrue( organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate ).size() == 3 );

        // asserts getOrganisationUnitHierarchies
        Iterator<OrganisationUnitHierarchy> iterator3 = organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate ).iterator();
        OrganisationUnitHierarchy hierVerC1 = iterator3.next();
        OrganisationUnitHierarchy hierVerC2 = iterator3.next();
        OrganisationUnitHierarchy hierVerC3 = iterator3.next();
        assertTrue( hierVerC1.getId() == ver_id2 );
        assertTrue( hierVerC2.getId() == ver_id3 );
        assertTrue( hierVerC3.getId() == ver_id4 );
        assertTrue( iterator3.hasNext() == false );
        
        // asserts getOrganisationUnitHierarchy 
        OrganisationUnitHierarchy hierarchy2 = organisationUnitService.getOrganisationUnitHierarchy( ver_id2 );
        assertTrue( hierarchy2.getDate().equals( jun15 ) );
        
        OrganisationUnitHierarchy hierarchy3 = organisationUnitService.getOrganisationUnitHierarchy( ver_id3 );
        assertTrue( hierarchy3.getDate().equals( jul15 ) );
    }

    @Test
    public void testClearOrganisationUnitHierarchyHistory()
        throws Exception
    {
        OrganisationUnit unit1 = new OrganisationUnit( "orgUnitName1", "shortName1", "organisationUnitCode1",
            new Date(), new Date(), true, "comment" );
        OrganisationUnit unit2 = new OrganisationUnit( "orgUnitName2", unit1, "shortName2", "organisationUnitCode2",
            new Date(), new Date(), true, "comment" );
        
        organisationUnitService.addOrganisationUnit( unit1 );
        organisationUnitService.addOrganisationUnit( unit2 );

        Calendar calendar = Calendar.getInstance();

        calendar.clear();
        calendar.set( 2005, Calendar.JULY, 1 );
        organisationUnitService.addOrganisationUnitHierarchy( calendar.getTime() );

        calendar.clear();
        calendar.set( 2005, Calendar.SEPTEMBER, 1 );
        organisationUnitService.addOrganisationUnitHierarchy( calendar.getTime() );

        calendar.clear();
        calendar.set( 2005, Calendar.AUGUST, 1 );
        organisationUnitService.addOrganisationUnitHierarchy( calendar.getTime() );

        calendar.set( 2005, Calendar.JUNE, 1 );
        Date startDate = calendar.getTime();

        calendar.set( 2005, Calendar.OCTOBER, 1 );
        Date endDate = calendar.getTime();

        Collection<OrganisationUnitHierarchy> hierarchies = organisationUnitService.getOrganisationUnitHierarchies(
            startDate, endDate );

        assertTrue( hierarchies.size() == 3 );

        organisationUnitService.clearOrganisationUnitHierarchyHistory();

        Collection<OrganisationUnitHierarchy> clearedHierarchies = organisationUnitService.getOrganisationUnitHierarchies( startDate, endDate );

        Iterator<OrganisationUnitHierarchy> iterator = clearedHierarchies.iterator();
        
        calendar.set( 1970, Calendar.JANUARY, 1 );
        Date epoch = calendar.getTime();
        
        OrganisationUnitHierarchy clearedHierarchy = iterator.next();
        
        assertTrue( clearedHierarchies.size() == 1 );
        assertTrue( clearedHierarchy.getDate().equals( epoch ) );
    }

    // -------------------------------------------------------------------------
    // OrganisationUnitGroupSets
    // -------------------------------------------------------------------------

    @Test
    public void testOrganisationUnitGroupSetsBasic()
        throws Exception
    {
        OrganisationUnitGroup organisationUnitGroup1 = new OrganisationUnitGroup();
        organisationUnitGroup1.setName( "oug1" );
        OrganisationUnitGroup organisationUnitGroup2 = new OrganisationUnitGroup();
        organisationUnitGroup2.setName( "oug2" );
        OrganisationUnitGroup organisationUnitGroup3 = new OrganisationUnitGroup();
        organisationUnitGroup3.setName( "oug3" );
        OrganisationUnitGroup organisationUnitGroup4 = new OrganisationUnitGroup();
        organisationUnitGroup4.setName( "oug4" );

        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup1 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup2 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup3 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup4 );

        OrganisationUnitGroupSet organisationUnitGroupSet1 = new OrganisationUnitGroupSet();
        organisationUnitGroupSet1.setName( "ougs1" );
        organisationUnitGroupSet1.setCompulsory( true );
        organisationUnitGroupSet1.getOrganisationUnitGroups().add( organisationUnitGroup1 );
        organisationUnitGroupSet1.getOrganisationUnitGroups().add( organisationUnitGroup2 );
        organisationUnitGroupSet1.getOrganisationUnitGroups().add( organisationUnitGroup3 );

        int id1 = organisationUnitGroupService.addOrganisationUnitGroupSet( organisationUnitGroupSet1 );

        // assert add
        assertNotNull( organisationUnitGroupService.getOrganisationUnitGroupSet( id1 ) );

        assertEquals( organisationUnitGroupService.getOrganisationUnitGroupSet( id1 ).getName(), "ougs1" );

        assertTrue( organisationUnitGroupService.getOrganisationUnitGroupSet( id1 ).getOrganisationUnitGroups().size() == 3 );
        
        organisationUnitGroupSet1.getOrganisationUnitGroups().remove( organisationUnitGroup3 );

        organisationUnitGroupService.updateOrganisationUnitGroupSet( organisationUnitGroupSet1 );

        // assert update
        assertTrue( organisationUnitGroupService.getOrganisationUnitGroupSet( id1 ).getOrganisationUnitGroups().size() == 2 );
        
        OrganisationUnitGroupSet organisationUnitGroupSet2 = new OrganisationUnitGroupSet();
        organisationUnitGroupSet2.setName( "ougs2" );
        organisationUnitGroupSet2.setCompulsory( true );
        organisationUnitGroupSet2.getOrganisationUnitGroups().add( organisationUnitGroup4 );

        int id2 = organisationUnitGroupService.addOrganisationUnitGroupSet( organisationUnitGroupSet2 );

        // assert getAll
        assertTrue( organisationUnitGroupService.getAllOrganisationUnitGroupSets().size() == 2 );
        
        organisationUnitGroupService.deleteOrganisationUnitGroupSet( organisationUnitGroupSet1 );
        organisationUnitGroupService.deleteOrganisationUnitGroupSet( organisationUnitGroupSet2 );
        
        assertNull( organisationUnitGroupService.getOrganisationUnitGroupSet( id1 ) );
        assertNull( organisationUnitGroupService.getOrganisationUnitGroupSet( id2 ) );
    }

    @Test
    public void testGetOrganisationUnitGroupSetsByName()
        throws Exception
    {
        OrganisationUnitGroup organisationUnitGroup1 = new OrganisationUnitGroup();
        organisationUnitGroup1.setName( "oug1" );
        OrganisationUnitGroup organisationUnitGroup2 = new OrganisationUnitGroup();
        organisationUnitGroup2.setName( "oug2" );
        OrganisationUnitGroup organisationUnitGroup3 = new OrganisationUnitGroup();
        organisationUnitGroup3.setName( "oug3" );
        OrganisationUnitGroup organisationUnitGroup4 = new OrganisationUnitGroup();
        organisationUnitGroup4.setName( "oug4" );

        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup1 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup2 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup3 );
        organisationUnitGroupService.addOrganisationUnitGroup( organisationUnitGroup4 );

        String ougs1 = "ougs1";
        String ougs2 = "ougs2";

        OrganisationUnitGroupSet organisationUnitGroupSet1 = new OrganisationUnitGroupSet();
        organisationUnitGroupSet1.setName( ougs1 );
        organisationUnitGroupSet1.setCompulsory( true );
        organisationUnitGroupSet1.getOrganisationUnitGroups().add( organisationUnitGroup1 );
        organisationUnitGroupSet1.getOrganisationUnitGroups().add( organisationUnitGroup2 );
        organisationUnitGroupSet1.getOrganisationUnitGroups().add( organisationUnitGroup3 );

        OrganisationUnitGroupSet organisationUnitGroupSet2 = new OrganisationUnitGroupSet();
        organisationUnitGroupSet2.setName( ougs2 );
        organisationUnitGroupSet2.setCompulsory( false );
        organisationUnitGroupSet2.getOrganisationUnitGroups().add( organisationUnitGroup4 );

        organisationUnitGroupService.addOrganisationUnitGroupSet( organisationUnitGroupSet1 );
        organisationUnitGroupService.addOrganisationUnitGroupSet( organisationUnitGroupSet2 );

        OrganisationUnitGroupSet set1 = organisationUnitGroupService.getOrganisationUnitGroupSetByName( ougs1 );
        OrganisationUnitGroupSet set2 = organisationUnitGroupService.getOrganisationUnitGroupSetByName( ougs2 );

        assertEquals( set1.getName(), ougs1 );
        assertEquals( set2.getName(), ougs2 );

        Collection<OrganisationUnitGroupSet> compulsorySets = organisationUnitGroupService
            .getCompulsoryOrganisationUnitGroupSets();
        assertEquals( compulsorySets.size(), 1 );
    }

    // -------------------------------------------------------------------------
    // OrganisationUnitLevel
    // -------------------------------------------------------------------------

    @Test
    public void testAddGetOrganisationUnitLevel()
    {
        OrganisationUnitLevel levelA = new OrganisationUnitLevel( 1, "National" );
        OrganisationUnitLevel levelB = new OrganisationUnitLevel( 2, "District" );
        
        int idA = organisationUnitService.addOrganisationUnitLevel( levelA );
        int idB = organisationUnitService.addOrganisationUnitLevel( levelB );
        
        assertEquals( levelA, organisationUnitService.getOrganisationUnitLevel( idA ) );
        assertEquals( levelB, organisationUnitService.getOrganisationUnitLevel( idB ) );        
    }

    @Test
    public void testGetOrganisationUnitLevels()
    {
        OrganisationUnitLevel level1 = new OrganisationUnitLevel( 1, "National" );
        OrganisationUnitLevel level2 = new OrganisationUnitLevel( 2, "District" );
        OrganisationUnitLevel level4 = new OrganisationUnitLevel( 4, "PHU" );
        
        organisationUnitService.addOrganisationUnitLevel( level1 );
        organisationUnitService.addOrganisationUnitLevel( level2 );
        organisationUnitService.addOrganisationUnitLevel( level4 );
        
        OrganisationUnit unitA = createOrganisationUnit( 'A' );
        OrganisationUnit unitB = createOrganisationUnit( 'B', unitA );
        OrganisationUnit unitC = createOrganisationUnit( 'C', unitB );
        OrganisationUnit unitD = createOrganisationUnit( 'D', unitC );
        
        unitA.getChildren().add( unitB );
        unitB.getChildren().add( unitC );
        unitC.getChildren().add( unitD );
        
        organisationUnitService.addOrganisationUnit( unitA );
        organisationUnitService.addOrganisationUnit( unitB );
        organisationUnitService.addOrganisationUnit( unitC );
        organisationUnitService.addOrganisationUnit( unitD );
                
        Iterator<OrganisationUnitLevel> actual = organisationUnitService.getOrganisationUnitLevels().iterator();
        
        assertNotNull( actual );
        assertEquals( level1, actual.next() );
        assertEquals( level2, actual.next() );
        
        level4 = actual.next();
        
        assertEquals( 4, level4.getLevel() );
        assertEquals( "PHU", level4.getName() );
    }

    @Test
    public void testRemoveOrganisationUnitLevel()
    {
        OrganisationUnitLevel levelA = new OrganisationUnitLevel( 1, "National" );
        OrganisationUnitLevel levelB = new OrganisationUnitLevel( 2, "District" );
        
        int idA = organisationUnitService.addOrganisationUnitLevel( levelA );
        int idB = organisationUnitService.addOrganisationUnitLevel( levelB );
        
        assertNotNull( organisationUnitService.getOrganisationUnitLevel( idA ) );
        assertNotNull( organisationUnitService.getOrganisationUnitLevel( idB ) );
        
        organisationUnitService.deleteOrganisationUnitLevel( levelA );

        assertNull( organisationUnitService.getOrganisationUnitLevel( idA ) );
        assertNotNull( organisationUnitService.getOrganisationUnitLevel( idB ) );

        organisationUnitService.deleteOrganisationUnitLevel( levelB );

        assertNull( organisationUnitService.getOrganisationUnitLevel( idA ) );
        assertNull( organisationUnitService.getOrganisationUnitLevel( idB ) );        
    }
}
