package org.hisp.dhis.common;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import org.hisp.dhis.dataelement.DataElement;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Lars Helge Overland
 */
public class BaseIdentifiableObjectTest
{
    @Test
    public void testGetValue()
    {
        DataElement deA = new DataElement();
        deA.setUid( "A1234567890" );
        deA.setCode( "CodeA" );
        deA.setName( "NameA" );        

        DataElement deB = new DataElement();

        IdScheme idSchemeUid = IdScheme.from( IdentifiableProperty.UID );
        IdScheme idSchemeCode = IdScheme.from( IdentifiableProperty.CODE );
        IdScheme idSchemeName = IdScheme.from( IdentifiableProperty.NAME );
        
        assertEquals( "A1234567890", deA.getPropertyValue( idSchemeUid ).get() );
        assertEquals( "CodeA", deA.getPropertyValue( idSchemeCode ).get() );
        assertEquals( "NameA", deA.getPropertyValue( idSchemeName ).get() );
        assertTrue( "NameA", deA.getPropertyValue( idSchemeName ).isPresent() );
        
        assertFalse( deB.getPropertyValue( idSchemeCode ).isPresent() );
    }
}
