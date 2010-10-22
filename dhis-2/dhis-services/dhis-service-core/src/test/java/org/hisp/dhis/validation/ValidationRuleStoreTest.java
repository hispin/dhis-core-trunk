package org.hisp.dhis.validation;

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
import java.util.Set;

import org.hisp.dhis.DhisTest;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.expression.Expression;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.period.PeriodType;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id: ValidationRuleStoreTest.java 3679 2007-10-22 18:25:18Z larshelg
 *          $
 */
public class ValidationRuleStoreTest
    extends DhisTest
{
    private ValidationRuleStore validationRuleStore;

    private ExpressionService expressionService;

    private DataElement dataElementA;

    private DataElement dataElementB;

    private DataElement dataElementC;

    private DataElement dataElementD;

    private Set<DataElement> dataElements;

    private Expression expressionA;

    private Expression expressionB;

    private PeriodType periodType;

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
        throws Exception
    {
        validationRuleStore = (ValidationRuleStore) getBean( ValidationRuleStore.ID );

        dataElementService = (DataElementService) getBean( DataElementService.ID );

        expressionService = (ExpressionService) getBean( ExpressionService.ID );

        dataElementA = createDataElement( 'A' );
        dataElementB = createDataElement( 'B' );
        dataElementC = createDataElement( 'C' );
        dataElementD = createDataElement( 'D' );

        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );
        dataElementService.addDataElement( dataElementC );
        dataElementService.addDataElement( dataElementD );

        dataElements = new HashSet<DataElement>();

        dataElements.add( dataElementA );
        dataElements.add( dataElementB );
        dataElements.add( dataElementC );
        dataElements.add( dataElementD );

        expressionA = new Expression( "expressionA", "descriptionA", dataElements );
        expressionB = new Expression( "expressionB", "descriptionB", dataElements );

        expressionService.addExpression( expressionB );
        expressionService.addExpression( expressionA );

        periodType = PeriodType.getAvailablePeriodTypes().iterator().next();
    }

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }

    // -------------------------------------------------------------------------
    // ValidationRule
    // -------------------------------------------------------------------------

    @Test
    public void testSaveValidationRule()
    {
        ValidationRule validationRule = createValidationRule( 'A', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );

        int id = validationRuleStore.saveValidationRule( validationRule );

        validationRule = validationRuleStore.get( id );

        assertEquals( validationRule.getName(), "ValidationRuleA" );
        assertEquals( validationRule.getDescription(), "DescriptionA" );
        assertEquals( validationRule.getType(), ValidationRule.TYPE_ABSOLUTE );
        assertEquals( validationRule.getOperator(), ValidationRule.OPERATOR_EQUAL );
        assertNotNull( validationRule.getLeftSide().getExpression() );
        assertNotNull( validationRule.getRightSide().getExpression() );
        assertEquals( validationRule.getPeriodType(), periodType );
    }

    @Test
    public void testUpdateValidationRule()
    {
        ValidationRule validationRule = createValidationRule( 'A', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );

        int id = validationRuleStore.saveValidationRule( validationRule );

        validationRule = validationRuleStore.get( id );

        assertEquals( validationRule.getName(), "ValidationRuleA" );
        assertEquals( validationRule.getDescription(), "DescriptionA" );
        assertEquals( validationRule.getType(), ValidationRule.TYPE_ABSOLUTE );
        assertEquals( validationRule.getOperator(), ValidationRule.OPERATOR_EQUAL );

        validationRule.setName( "ValidationRuleB" );
        validationRule.setDescription( "DescriptionB" );
        validationRule.setType( ValidationRule.TYPE_STATISTICAL );
        validationRule.setOperator( ValidationRule.OPERATOR_GREATER );

        validationRuleStore.updateValidationRule( validationRule );

        validationRule = validationRuleStore.get( id );

        assertEquals( validationRule.getName(), "ValidationRuleB" );
        assertEquals( validationRule.getDescription(), "DescriptionB" );
        assertEquals( validationRule.getType(), ValidationRule.TYPE_STATISTICAL );
        assertEquals( validationRule.getOperator(), ValidationRule.OPERATOR_GREATER );
    }

    @Test
    public void testDeleteValidationRule()
    {
        ValidationRule validationRuleA = createValidationRule( 'A', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );
        ValidationRule validationRuleB = createValidationRule( 'B', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );

        int idA = validationRuleStore.saveValidationRule( validationRuleA );
        int idB = validationRuleStore.saveValidationRule( validationRuleB );

        assertNotNull( validationRuleStore.get( idA ) );
        assertNotNull( validationRuleStore.get( idB ) );

        validationRuleA.clearExpressions();

        validationRuleStore.delete( validationRuleA );

        assertNull( validationRuleStore.get( idA ) );
        assertNotNull( validationRuleStore.get( idB ) );

        validationRuleB.clearExpressions();

        validationRuleStore.delete( validationRuleB );

        assertNull( validationRuleStore.get( idA ) );
        assertNull( validationRuleStore.get( idB ) );
    }

    @Test
    public void testGetAllValidationRules()
    {
        ValidationRule validationRuleA = createValidationRule( 'A', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );
        ValidationRule validationRuleB = createValidationRule( 'B', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );

        validationRuleStore.saveValidationRule( validationRuleA );
        validationRuleStore.saveValidationRule( validationRuleB );

        Collection<ValidationRule> rules = validationRuleStore.getAll();

        assertTrue( rules.size() == 2 );
        assertTrue( rules.contains( validationRuleA ) );
        assertTrue( rules.contains( validationRuleB ) );
    }

    @Test
    public void testGetValidationRuleByName()
    {
        ValidationRule validationRuleA = createValidationRule( 'A', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );
        ValidationRule validationRuleB = createValidationRule( 'B', ValidationRule.OPERATOR_EQUAL, expressionA,
            expressionB, periodType );

        int id = validationRuleStore.saveValidationRule( validationRuleA );
        validationRuleStore.saveValidationRule( validationRuleB );

        ValidationRule rule = validationRuleStore.getByName( "ValidationRuleA" );

        assertEquals( rule.getId(), id );
        assertEquals( rule.getName(), "ValidationRuleA" );
    }
    
    @Test
    public void testGetValidationRulesByDataElements()
    {
        Set<DataElement> dataElementsA = new HashSet<DataElement>();
        dataElementsA.add( dataElementA );
        dataElementsA.add( dataElementB );

        Set<DataElement> dataElementsB = new HashSet<DataElement>();
        dataElementsB.add( dataElementC );
        dataElementsB.add( dataElementD );

        Set<DataElement> dataElementsC = new HashSet<DataElement>();
        
        Set<DataElement> dataElementsD = new HashSet<DataElement>();
        dataElementsD.addAll( dataElementsA );
        dataElementsD.addAll( dataElementsB );        
        
        Expression expression1 = new Expression( "Expression1", "Expression1", dataElementsA );
        Expression expression2 = new Expression( "Expression2", "Expression2", dataElementsB );
        Expression expression3 = new Expression( "Expression3", "Expression3", dataElementsC );
        
        expressionService.addExpression( expression1 );
        expressionService.addExpression( expression2 );
        expressionService.addExpression( expression3 );
        
        ValidationRule ruleA = createValidationRule( 'A', ValidationRule.OPERATOR_EQUAL, expression1, expression3, periodType );
        ValidationRule ruleB = createValidationRule( 'B', ValidationRule.OPERATOR_EQUAL, expression2, expression3, periodType );
        ValidationRule ruleC = createValidationRule( 'C', ValidationRule.OPERATOR_EQUAL, expression3, expression3, periodType );

        validationRuleStore.saveValidationRule( ruleA );
        validationRuleStore.saveValidationRule( ruleB );
        validationRuleStore.saveValidationRule( ruleC );
        
        Collection<ValidationRule> rules = validationRuleStore.getValidationRulesByDataElements( dataElementsA );
        
        assertNotNull( rules );
        assertEquals( 1, rules.size() );
        assertTrue( rules.contains( ruleA ) );

        rules = validationRuleStore.getValidationRulesByDataElements( dataElementsB );
        
        assertNotNull( rules );
        assertEquals( 1, rules.size() );
        assertTrue( rules.contains( ruleB ) );

        rules = validationRuleStore.getValidationRulesByDataElements( dataElementsD );
        
        assertNotNull( rules );
        assertEquals( 2, rules.size() );
        assertTrue( rules.contains( ruleA ) );
        assertTrue( rules.contains( ruleB ) );
    }
}
