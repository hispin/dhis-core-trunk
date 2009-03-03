package org.hisp.dhis.system.grid;

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
import java.util.List;

import junit.framework.TestCase;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class GridTest
    extends TestCase
{
    private Grid grid;
    
    @Override
    public void setUp()
    {
        grid = new Grid();

        grid.nextRow();        
        grid.addValue( "11" );
        grid.addValue( "12" );
        grid.addValue( "13" );

        grid.nextRow();        
        grid.addValue( "21" );
        grid.addValue( "22" );
        grid.addValue( "23" );

        grid.nextRow();        
        grid.addValue( "31" );
        grid.addValue( "32" );
        grid.addValue( "33" );
    }
    
    public void testGetHeight()
    {
        assertEquals( 3, grid.getHeight() );
    }
    
    public void testGetWidth()
    {
        assertEquals( 3, grid.getWidth() );
    }
    
    public void testGetRow()
    {
        List<String> rowA = grid.getRow( 0 );
        
        assertTrue( rowA.size() == 3 );
        assertTrue( rowA.contains( "11" ) );
        assertTrue( rowA.contains( "12" ) );
        assertTrue( rowA.contains( "13" ) );
        
        List<String> rowB = grid.getRow( 1 );
        
        assertTrue( rowB.size() == 3 );
        assertTrue( rowB.contains( "21" ) );
        assertTrue( rowB.contains( "22" ) );
        assertTrue( rowB.contains( "23" ) );
    }
    
    public void testGetRows()
    {
        assertEquals( 3, grid.getRows().size() );
    }
    
    public void testGetColumn()
    {        
        List<String> columnB = grid.getColumn( 1 );
        
        assertTrue( columnB.size() == 3 );
        assertTrue( columnB.contains( "12" ) );
        assertTrue( columnB.contains( "22" ) );
        assertTrue( columnB.contains( "32" ) );

        List<String> columnC = grid.getColumn( 2 );
        
        assertTrue( columnC.size() == 3 );
        assertTrue( columnC.contains( "13" ) );
        assertTrue( columnC.contains( "23" ) );
        assertTrue( columnC.contains( "33" ) ); 
    }
    
    public void testAddColumn()
    {
        List<String> columnValues = new ArrayList<String>( 3 );
        columnValues.add( "14" );
        columnValues.add( "24" );
        columnValues.add( "34" );        
        
        grid.addColumn( columnValues );
        
        List<String> columnD = grid.getColumn( 3 );
        
        assertTrue( columnD.size() == 3 );
        assertTrue( columnD.contains( "14" ) );
        assertTrue( columnD.contains( "24" ) );
        assertTrue( columnD.contains( "34" ) );
        
        List<String> rowB = grid.getRow( 1 );
        
        assertTrue( rowB.size() == 4 );
        assertTrue( rowB.contains( "21" ) );
        assertTrue( rowB.contains( "22" ) );
        assertTrue( rowB.contains( "23" ) );
        assertTrue( rowB.contains( "24" ) );
    }
    
    public void testAddRegressionColumn()
    {
        grid = new Grid();        

        grid.nextRow();        
        grid.addValue( "10.0" );
        grid.nextRow();        
        grid.addValue( "50.0" );
        grid.nextRow();        
        grid.addValue( "20.0" );
        grid.nextRow();        
        grid.addValue( "60.0" );
        
        grid.addRegressionColumn( 0 );
        
        List<String> column = grid.getColumn( 1 );
        
        assertTrue( column.size() == 4 );
        assertTrue( column.contains( "17.0" ) );
        assertTrue( column.contains( "29.0" ) );
        assertTrue( column.contains( "41.0" ) );
        assertTrue( column.contains( "53.0" ) );
    }
}
