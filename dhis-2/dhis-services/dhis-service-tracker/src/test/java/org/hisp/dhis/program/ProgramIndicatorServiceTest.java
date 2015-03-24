package org.hisp.dhis.program;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.hisp.dhis.program.ProgramIndicator.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementDomain;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 */
public class ProgramIndicatorServiceTest
    extends DhisSpringTest
{
    @Autowired
    private ProgramIndicatorService programIndicatorService;

    @Autowired
    private TrackedEntityAttributeService attributeService;
    
    @Autowired
    private TrackedEntityInstanceService entityInstanceService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramStageService programStageService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private TrackedEntityDataValueService dataValueService;
    
    @Autowired
    private DataElementService dataElementService;

    private Date incidenDate;

    private Date enrollmentDate;
    
    private ProgramStage psA;
    private ProgramStage psB;

    private Program programA;
    private Program programB;

    private ProgramInstance programInstance;
    
    private DataElement deA;    
    private DataElement deB;
    
    private TrackedEntityAttribute atA;
    private TrackedEntityAttribute atB;

    private ProgramIndicator indicatorDate;
    private ProgramIndicator indicatorInt;
    private ProgramIndicator indicatorC;
    private ProgramIndicator indicatorD;
    private ProgramIndicator indicatorE;
    
    @Override
    public void setUpTest()
    {
        OrganisationUnit organisationUnit = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnit );

        programA = createProgram( 'A', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programA );

        psA = new ProgramStage( "StageA", programA );
        psA.setSortOrder( 1 );
        programStageService.saveProgramStage( psA );

        psB = new ProgramStage( "StageB", programA );
        psB.setSortOrder( 2 );
        programStageService.saveProgramStage( psB );

        Set<ProgramStage> programStages = new HashSet<>();
        programStages.add( psA );
        programStages.add( psB );
        programA.setProgramStages( programStages );
        programService.updateProgram( programA );

        programB = createProgram( 'B', new HashSet<ProgramStage>(), organisationUnit );
        programService.addProgram( programB );

        TrackedEntityInstance entityInstance = createTrackedEntityInstance( 'A', organisationUnit );
        entityInstanceService.addTrackedEntityInstance( entityInstance );

        DateTime testDate1 = DateTime.now();
        testDate1.withTimeAtStartOfDay();
        testDate1 = testDate1.minusDays( 70 );
        incidenDate = testDate1.toDate();
        
        DateTime testDate2 = DateTime.now();
        testDate2.withTimeAtStartOfDay();
        enrollmentDate = testDate2.toDate();

        programInstance = programInstanceService.enrollTrackedEntityInstance( entityInstance, programA, enrollmentDate, incidenDate,
            organisationUnit );

        deA = createDataElement( 'A' );
        deA.setDomainType( DataElementDomain.TRACKER );
        deB = createDataElement( 'B' );
        deB.setDomainType( DataElementDomain.TRACKER );
        
        dataElementService.addDataElement( deA );
        dataElementService.addDataElement( deB );
        
        atA = createTrackedEntityAttribute( 'A' );
        atB = createTrackedEntityAttribute( 'B' );
        
        attributeService.addTrackedEntityAttribute( atA );
        attributeService.addTrackedEntityAttribute( atB );
        
        indicatorDate = new ProgramIndicator( "IndicatorA", "IndicatorDesA", ProgramIndicator.VALUE_TYPE_INT, "( " + ProgramIndicator.KEY_PROGRAM_VARIABLE + "{"
            + ProgramIndicator.INCIDENT_DATE + "} - " + ProgramIndicator.KEY_PROGRAM_VARIABLE + "{" + ProgramIndicator.ENROLLMENT_DATE + "} )  / 7" );
        indicatorDate.setUid( "UID-DATE" );
        indicatorDate.setShortName( "DATE" );
        indicatorDate.setProgram( programA );

        indicatorInt = new ProgramIndicator( "IndicatorB", "IndicatorDesB", ProgramIndicator.VALUE_TYPE_DATE, "70" );
        indicatorInt.setRootDate( ProgramIndicator.INCIDENT_DATE );
        indicatorInt.setUid( "UID-INT" );
        indicatorInt.setShortName( "INT" );
        indicatorInt.setProgram( programA );

        indicatorC = new ProgramIndicator( "IndicatorC", "IndicatorDesB", ProgramIndicator.VALUE_TYPE_INT, "0" );
        indicatorC.setUid( "UID-C" );
        indicatorC.setShortName( "C" );
        indicatorC.setProgram( programB );
        
        indicatorD = new ProgramIndicator( "IndicatorD", "IndicatorDesD", ProgramIndicator.VALUE_TYPE_INT, "0 + A + 4 + " + ProgramIndicator.KEY_PROGRAM_VARIABLE + "{"
            + ProgramIndicator.INCIDENT_DATE + "}" );
        indicatorD.setUid( "UID-D" );
        indicatorD.setShortName( "D" );
        indicatorD.setProgram( programB );
        
        indicatorE = new ProgramIndicator( "IndicatorE", "IndicatorDesE", VALUE_TYPE_INT, 
            KEY_DATAELEMENT + "{" + psA.getUid() + "." + deA.getUid() + "} + " + 
            KEY_DATAELEMENT + "{" + psB.getUid() + "." + deB.getUid() + "} - " + 
            KEY_ATTRIBUTE + "{" + atA.getUid() + "} + " + KEY_ATTRIBUTE + "{" + atB.getUid() + "}" );
    }

    // -------------------------------------------------------------------------
    // CRUD tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );
        int idB = programIndicatorService.addProgramIndicator( indicatorInt );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testDeleteProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );
        int idB = programIndicatorService.addProgramIndicator( indicatorInt );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );

        programIndicatorService.deleteProgramIndicator( indicatorDate );

        assertNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNotNull( programIndicatorService.getProgramIndicator( idB ) );

        programIndicatorService.deleteProgramIndicator( indicatorInt );

        assertNull( programIndicatorService.getProgramIndicator( idA ) );
        assertNull( programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testUpdateProgramIndicator()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );

        assertNotNull( programIndicatorService.getProgramIndicator( idA ) );

        indicatorDate.setName( "B" );
        programIndicatorService.updateProgramIndicator( indicatorDate );

        assertEquals( "B", programIndicatorService.getProgramIndicator( idA ).getName() );
    }

    @Test
    public void testGetProgramIndicatorById()
    {
        int idA = programIndicatorService.addProgramIndicator( indicatorDate );
        int idB = programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( indicatorDate, programIndicatorService.getProgramIndicator( idA ) );
        assertEquals( indicatorInt, programIndicatorService.getProgramIndicator( idB ) );
    }

    @Test
    public void testGetProgramIndicatorByName()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( "IndicatorA", programIndicatorService.getProgramIndicator( "IndicatorA" ).getName() );
        assertEquals( "IndicatorB", programIndicatorService.getProgramIndicator( "IndicatorB" ).getName() );
    }

    @Test
    public void testGetAllProgramIndicators()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertTrue( equals( programIndicatorService.getAllProgramIndicators(), indicatorDate, indicatorInt ) );
    }

    @Test
    public void testGetProgramIndicatorByShortName()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( "INT", programIndicatorService.getProgramIndicatorByShortName( "INT" ).getShortName() );
        assertEquals( "DATE", programIndicatorService.getProgramIndicatorByShortName( "DATE" ).getShortName() );
    }

    @Test
    public void testGetProgramIndicatorByUid()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        assertEquals( "UID-INT", programIndicatorService.getProgramIndicatorByUid( "UID-INT" ).getUid() );
        assertEquals( "UID-DATE", programIndicatorService.getProgramIndicatorByUid( "UID-DATE" ).getUid() );
    }

    @Test
    public void testGetProgramIndicatorsByProgram()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );
        programIndicatorService.addProgramIndicator( indicatorC );

        Collection<ProgramIndicator> indicators = programIndicatorService.getProgramIndicators( programA );
        assertEquals( 2, indicators.size() );
        assertTrue( indicators.contains( indicatorDate ) );
        assertTrue( indicators.contains( indicatorInt ) );

        indicators = programIndicatorService.getProgramIndicators( programB );
        assertEquals( 1, indicators.size() );
        assertTrue( indicators.contains( indicatorC ) );

    }

    // -------------------------------------------------------------------------
    // Logic tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetProgramStageDataElementsInExpression()
    {
        Set<ProgramStageDataElement> elements = programIndicatorService.getProgramStageDataElementsInExpression( indicatorE );
        
        assertEquals( 2, elements.size() );
        assertTrue( elements.contains( new ProgramStageDataElement( psA, deA ) ) );
        assertTrue( elements.contains( new ProgramStageDataElement( psB, deB ) ) );
    }
    
    @Test
    public void testGetAttributesInExpression()
    {
        Set<TrackedEntityAttribute> attributes = programIndicatorService.getAttributesInExpression( indicatorE );
        
        assertEquals( 2, attributes.size() );
        assertTrue( attributes.contains( atA ) );
        assertTrue( attributes.contains( atB ) );
    }
    
    @Test
    public void testGetProgramIndicatorValue()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        String valueINT = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorDate );
        assertEquals( "10.0", valueINT );

        String valueLDATE = programIndicatorService.getProgramIndicatorValue( programInstance, indicatorInt );
        assertEquals( DateUtils.getMediumDateString( enrollmentDate ), valueLDATE );
    }

    @Test
    public void testGetProgramIndicatorValues()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        Map<String, String> indicatorMap = programIndicatorService.getProgramIndicatorValues( programInstance );
        assertEquals( 2, indicatorMap.keySet().size() );
        assertEquals( "10.0", indicatorMap.get( "IndicatorA" ) );
        assertEquals( DateUtils.getMediumDateString( enrollmentDate ), indicatorMap.get( "IndicatorB" ) );

    }
    
    @Test
    public void testGetExpressionDescription()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );

        String description = programIndicatorService.getExpressionDescription( indicatorDate.getExpression() );
        assertEquals( "( Incident date - Enrollment date )  / 7", description);
        
        description = programIndicatorService.getExpressionDescription( indicatorInt.getExpression() );
        assertEquals( "70", description);
    }
    
    @Test
    public void testExpressionIsValid()
    {
        programIndicatorService.addProgramIndicator( indicatorDate );
        programIndicatorService.addProgramIndicator( indicatorInt );
        programIndicatorService.addProgramIndicator( indicatorD );

        assertEquals( ProgramIndicator.VALID, programIndicatorService.expressionIsValid( indicatorDate.getExpression() ) );
        assertEquals( ProgramIndicator.VALID, programIndicatorService.expressionIsValid( indicatorInt.getExpression() ) );
        assertEquals( ProgramIndicator.EXPRESSION_NOT_WELL_FORMED, programIndicatorService.expressionIsValid( indicatorD.getExpression() ) );
    }    
}
