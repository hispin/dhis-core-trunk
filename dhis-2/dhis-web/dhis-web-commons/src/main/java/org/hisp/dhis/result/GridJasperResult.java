package org.hisp.dhis.result;

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

import java.io.StringWriter;
import java.sql.Connection;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.amplecode.quick.StatementManager;
import org.apache.struts2.ServletActionContext;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.system.util.StreamUtils;
import org.hisp.dhis.util.ContextUtils;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;

/**
 * @author Lars Helge Overland
 */
public class GridJasperResult
    implements Result
{
    private static final String KEY_GRID = "grid";
    private static final String TEMPLATE = "jrxmlGrid.vm";
    private static final String ENCODING = "UTF-8";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementManager statementManager;

    public void setStatementManager( StatementManager statementManager )
    {
        this.statementManager = statementManager;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private Grid grid;
    
    public void setGrid( Grid grid )
    {
        this.grid = grid;
    }

    // -------------------------------------------------------------------------
    // Result implementation
    // -------------------------------------------------------------------------

    @Override
    public void execute( ActionInvocation invocation )
        throws Exception
    {
        // ---------------------------------------------------------------------
        // Get grid
        // ---------------------------------------------------------------------

        Grid _grid = (Grid) invocation.getStack().findValue( "grid" );
        
        grid = _grid != null ? _grid : grid; 

        // ---------------------------------------------------------------------
        // Configure response
        // ---------------------------------------------------------------------

        HttpServletResponse response = ServletActionContext.getResponse();

        ContextUtils.configureResponse( response, ContextUtils.CONTENT_TYPE_PDF, true, null, false );
        
        // ---------------------------------------------------------------------
        // Write jrxml based on Velocity template
        // ---------------------------------------------------------------------

        StringWriter writer = new StringWriter();
        
        VelocityContext context = new VelocityContext();
        
        context.put( KEY_GRID, grid );
        
        Velocity.mergeTemplate( TEMPLATE, ENCODING, context, writer );
        
        String report = writer.toString();

        // ---------------------------------------------------------------------
        // Write Jasper report
        // ---------------------------------------------------------------------

        JasperReport jasperReport = JasperCompileManager.compileReport( StreamUtils.getInputStream( report ) );
        
        Connection connection = statementManager.getHolder().getConnection();
        
        JasperPrint print = null;
        
        try
        {
            print = JasperFillManager.fillReport( jasperReport, null, connection );
        }
        finally
        {        
            connection.close();
        }
        
        if ( print != null )
        {
            JasperExportManager.exportReportToPdfStream( print, response.getOutputStream() );
        }
    }
}
