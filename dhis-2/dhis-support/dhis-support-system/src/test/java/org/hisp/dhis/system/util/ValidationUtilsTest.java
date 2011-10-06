package org.hisp.dhis.system.util;

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

import static junit.framework.Assert.*;
import static org.hisp.dhis.system.util.ValidationUtils.*;

import org.junit.Test;

/**
 * @author Lars Helge Overland
 */
public class ValidationUtilsTest
{
    @Test
    public void testCoordinateIsValid()
    {
        assertTrue( coordinateIsValid( "[+37.99034,-28.94221]" ) );
        assertTrue( coordinateIsValid( "[37.99034,-28.94221]" ) );
        assertTrue( coordinateIsValid( "[+37.99034,28.94221]" ) );
        assertTrue( coordinateIsValid( "[37.99034,28.94221]" ) );
        assertTrue( coordinateIsValid( "[+37,-28.94221]" ) );
        assertTrue( coordinateIsValid( "[37.99034,28]" ) );
        
        assertFalse( coordinateIsValid( "23.34343,56.3232" ) );
        assertFalse( coordinateIsValid( "23.34343 56.3232" ) );
        assertFalse( coordinateIsValid( "[23.34f43,56.3232]" ) );
        assertFalse( coordinateIsValid( "23.34343,56.323.2" ) );
        assertFalse( coordinateIsValid( "S-0.27726 E37.08472" ) );        
        assertFalse( coordinateIsValid( null ) );
    }
    
    @Test
    public void testGetLatitude()
    {
        assertEquals( "+37.99034", getLatitude( "[+37.99034,-28.94221]" ) );
        assertEquals( "37.99034", getLatitude( "[37.99034,28.94221]" ) );
        assertNull( getLatitude( "23.34343,56.3232" ) );
        assertNull( getLatitude( null ) );
    }
    
    @Test
    public void testGetLongitude()
    {
        assertEquals( "-28.94221", getLongitude( "[+37.99034,-28.94221]" ) );
        assertEquals( "28.94221", getLongitude( "[37.99034,28.94221]" ) );
        assertNull( getLongitude( "23.34343,56.3232" ) );
        assertNull( getLongitude( null ) );
    }
}
