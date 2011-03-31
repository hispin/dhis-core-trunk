package org.hisp.dhis.system.grid;

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

import static org.hisp.dhis.system.util.CsvUtils.NEWLINE;
import static org.hisp.dhis.system.util.CsvUtils.SEPARATOR_B;
import static org.hisp.dhis.system.util.CsvUtils.csvEncode;
import static org.hisp.dhis.system.util.PDFUtils.addTableToDocument;
import static org.hisp.dhis.system.util.PDFUtils.closeDocument;
import static org.hisp.dhis.system.util.PDFUtils.getEmptyCell;
import static org.hisp.dhis.system.util.PDFUtils.getItalicCell;
import static org.hisp.dhis.system.util.PDFUtils.getSubtitleCell;
import static org.hisp.dhis.system.util.PDFUtils.getTextCell;
import static org.hisp.dhis.system.util.PDFUtils.getTitleCell;
import static org.hisp.dhis.system.util.PDFUtils.openDocument;
import static org.hisp.dhis.system.util.PDFUtils.resetPaddings;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.system.util.CodecUtils;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.system.util.StreamUtils;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;

/**
 * @author Lars Helge Overland
 */
public class GridUtils
{
    private static final String EMPTY = "";
    
    private static final String XLS_DEFAULT_SHEET_NAME = "Sheet 1";
    
    private static final WritableCellFormat XLS_FORMAT_TTTLE = new WritableCellFormat( new WritableFont(
        WritableFont.TAHOMA, 13, WritableFont.NO_BOLD, false ) );
    
    private static final WritableCellFormat XLS_FORMAT_LABEL = new WritableCellFormat( new WritableFont(
        WritableFont.ARIAL, 11, WritableFont.NO_BOLD, true ) );
    
    private static final WritableCellFormat XLS_FORMAT_TEXT = new WritableCellFormat( new WritableFont( WritableFont.ARIAL,
        11, WritableFont.NO_BOLD, false ) );

    private static final String KEY_GRID = "grid";
    private static final String TEMPLATE = "grid.vm";
    private static final String RESOURCE_LOADER_NAME = "class";

    /**
     * Writes a PDF representation of the given Grid to the given OutputStream.
     */
    public static void toPdf( Grid grid, OutputStream out )
    {
        Document document = openDocument( out );

        PdfPTable table = new PdfPTable( grid.getVisibleWidth() );

        table.setHeaderRows( 1 );
        table.setWidthPercentage( 100f );
        table.setKeepTogether( false );

        table.addCell( resetPaddings( getTitleCell( grid.getTitle(), grid.getVisibleWidth() ), 0, 45, 0, 0 ) );
        table.addCell( getSubtitleCell( grid.getSubtitle(), grid.getVisibleWidth() ) );
        table.addCell( getEmptyCell( grid.getVisibleWidth(), 30 ) );

        for ( GridHeader header : grid.getVisibleHeaders() )
        {
            table.addCell( getItalicCell( header.getName() ) );
        }

        table.addCell( getEmptyCell( grid.getVisibleWidth(), 10 ) );

        for ( List<Object> row : grid.getVisibleRows() )
        {
            for ( Object col : row )
            {
                table.addCell( getTextCell( col ) );
            }
        }

        addTableToDocument( document, table );

        closeDocument( document );
    }

    /**
     * Writes a XLS (Excel workbook) representation of the given Grid to the given OutputStream.
     */
    public static void toXls( Grid grid, OutputStream out )
        throws Exception
    {
        WritableWorkbook workbook = Workbook.createWorkbook( out );

        String sheetName = CodecUtils.filenameEncode( StringUtils.defaultIfEmpty( grid.getTitle(), XLS_DEFAULT_SHEET_NAME ) );
        
        WritableSheet sheet = workbook.createSheet( sheetName, 0 );

        int rowNumber = 1;

        int columnIndex = 0;

        sheet.addCell( new Label( 0, rowNumber++, grid.getTitle(), XLS_FORMAT_TTTLE ) );

        rowNumber++;

        for ( GridHeader header : grid.getVisibleHeaders() )
        {
            sheet.addCell( new Label( columnIndex++, rowNumber, header.getName(), XLS_FORMAT_LABEL ) );
        }

        rowNumber++;

        for ( List<Object> row : grid.getVisibleRows() )
        {
            columnIndex = 0;

            for ( Object column : row )
            {
                if ( column != null && MathUtils.isNumeric( String.valueOf( column ) ) )
                {
                    sheet.addCell( new Number( columnIndex++, rowNumber, Double.valueOf( String.valueOf( column ) ), XLS_FORMAT_TEXT ) );
                }
                else
                {
                    String content = column != null ? String.valueOf( column ) : EMPTY;
                    
                    sheet.addCell( new Label( columnIndex++, rowNumber, content, XLS_FORMAT_TEXT ) );
                }
            }

            rowNumber++;
        }

        workbook.write();

        workbook.close();
    }

    /**
     * Writes a CSV representation of the given Grid to the given OutputStream.
     */
    public static void toCsv( Grid grid, OutputStream out )
        throws Exception
    {
        Iterator<GridHeader> headers = grid.getHeaders().iterator();
        
        while ( headers.hasNext() )
        {
            out.write( csvEncode( headers.next().getName() ).getBytes() );
            
            if ( headers.hasNext() )
            {
                out.write( SEPARATOR_B );
            }
        }

        out.write( NEWLINE );
        
        for ( List<Object> row : grid.getRows() )
        {
            Iterator<Object> columns = row.iterator();
            
            while ( columns.hasNext() )
            {
                out.write( csvEncode( columns.next() ).getBytes() );
                
                if ( columns.hasNext() )
                {
                    out.write( SEPARATOR_B );
                }
            }
            
            out.write( NEWLINE );
        }
    }

    /**
     * Writes a Jasper Reports representation of the given Grid to the given OutputStream.
     */
    public static void toJasperReport( Grid grid, OutputStream out )
        throws Exception
    {
        final StringWriter writer = new StringWriter();
        
        final VelocityEngine velocity = new VelocityEngine();
        
        velocity.setProperty( Velocity.RESOURCE_LOADER, RESOURCE_LOADER_NAME );
        velocity.setProperty( RESOURCE_LOADER_NAME + ".resource.loader.class", ClasspathResourceLoader.class.getName() );
        velocity.init();
        
        final VelocityContext context = new VelocityContext();
        
        context.put( KEY_GRID, grid );
        
        velocity.getTemplate( TEMPLATE ).merge( context, writer );
        
        String report = writer.toString();

        JasperReport jasperReport = JasperCompileManager.compileReport( StreamUtils.getInputStream( report ) );
        
        JasperPrint print = JasperFillManager.fillReport( jasperReport, null, grid );
        
        JasperExportManager.exportReportToPdfStream( print, out );
    }

    /**
     * Writes a JRXML (Jasper Reports XML) representation of the given Grid to the given Writer.
     */
    public static void toJrxml( Grid grid, Writer writer )
        throws Exception
    {
        final VelocityEngine velocity = new VelocityEngine();
        
        velocity.setProperty( Velocity.RESOURCE_LOADER, RESOURCE_LOADER_NAME );
        velocity.setProperty( RESOURCE_LOADER_NAME + ".resource.loader.class", ClasspathResourceLoader.class.getName() );
        velocity.init();
        
        final VelocityContext context = new VelocityContext();
        
        context.put( KEY_GRID, grid );
        
        velocity.getTemplate( TEMPLATE ).merge( context, writer );
    }
}
