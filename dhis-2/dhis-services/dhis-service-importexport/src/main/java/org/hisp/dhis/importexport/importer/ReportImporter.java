package org.hisp.dhis.importexport.importer;

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

import org.hisp.dhis.importexport.GroupMemberType;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.Importer;
import org.hisp.dhis.report.Report;
import org.hisp.dhis.report.ReportService;

/**
 * @author Lars Helge Overland
 */
public class ReportImporter
    extends AbstractImporter<Report> implements Importer<Report>
{
    protected ReportService reportService;
    
    public ReportImporter()
    {
    }
    
    public ReportImporter( ReportService reportService )
    {
        this.reportService = reportService;
    }
    
    @Override
    public void importObject( Report object, ImportParams params )
    {
        read( object, GroupMemberType.NONE, params );
    }

    @Override
    protected void importUnique( Report object )
    {
        reportService.saveReport( object );
    }

    @Override
    protected void importMatching( Report object, Report match )
    {
        match.setName( match.getName() );
        match.setDesign( object.getDesign() );
        match.setDesignContent( object.getDesignContent() );
        match.setType( object.getType() );
        
        reportService.saveReport( match );
    }

    @Override
    protected Report getMatching( Report object )
    {
        return reportService.getReportByName( object.getName() );
    }

    @Override
    protected boolean isIdentical( Report object, Report existing )
    {
        if ( !object.getName().equals( existing.getName() ) )
        {
            return false;
        }
        if ( !isSimiliar( object.getDesign(), existing.getDesign() ) || ( isNotNull( object.getDesign(), existing.getDesign() ) && !object.getDesign().equals( existing.getDesign() ) ) )
        {
            return false;
        }
        if ( !isSimiliar( object.getDesignContent(), existing.getDesignContent() ) || ( isNotNull( object.getDesignContent(), existing.getDesignContent() ) && !object.getDesignContent().equals( existing.getDesignContent() ) ) )
        {
            return false;
        }
        if ( !isSimiliar( object.getType(), existing.getType() ) || ( isNotNull( object.getType(), existing.getType() ) && !object.getType().equals( existing.getType() ) ) )
        {
            return false;
        }
        
        return true;
    }
}
