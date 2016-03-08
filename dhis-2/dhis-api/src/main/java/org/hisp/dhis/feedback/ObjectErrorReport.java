package org.hisp.dhis.feedback;

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

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class ObjectErrorReport
{
    private final Class<?> objectClass;

    private Integer objectIndex;

    private Map<ErrorCode, List<ErrorReport>> errorReportsByCode = new HashMap<>();

    public ObjectErrorReport( Class<?> objectClass )
    {
        this.objectClass = objectClass;
    }

    public ObjectErrorReport( Class<?> objectClass, Integer objectIndex )
    {
        this.objectClass = objectClass;
        this.objectIndex = objectIndex;
    }

    public Class<?> getObjectClass()
    {
        return objectClass;
    }

    public Integer getObjectIndex()
    {
        return objectIndex;
    }

    public void addErrorReports( List<? extends ErrorReport> errorReports )
    {
        errorReports.forEach( this::addErrorReport );
    }

    public void addErrorReport( ErrorReport errorReport )
    {
        if ( !errorReportsByCode.containsKey( errorReport.getErrorCode() ) )
        {
            errorReportsByCode.put( errorReport.getErrorCode(), new ArrayList<>() );
        }

        errorReportsByCode.get( errorReport.getErrorCode() ).add( errorReport );
    }

    public List<ErrorCode> getErrorCodes()
    {
        return new ArrayList<>( errorReportsByCode.keySet() );
    }

    public List<ErrorReport> getErrorReports()
    {
        List<ErrorReport> errorReports = new ArrayList<>();
        errorReportsByCode.values().forEach( errorReports::addAll );

        return errorReports;
    }

    public Map<ErrorCode, List<ErrorReport>> getErrorReportsByCode()
    {
        return errorReportsByCode;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "objectClass", objectClass )
            .add( "objectIndex", objectIndex )
            .add( "errorReportsByCode", errorReportsByCode )
            .toString();
    }
}