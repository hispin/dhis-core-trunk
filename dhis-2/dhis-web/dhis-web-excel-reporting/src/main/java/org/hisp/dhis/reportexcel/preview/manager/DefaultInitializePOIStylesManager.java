package org.hisp.dhis.reportexcel.preview.manager;

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

import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * @author Dang Duy Hieu
 * @version $Id$
 * @since 2009-09-18 16:45:00Z
 */
public class DefaultInitializePOIStylesManager
    implements InitializePOIStylesManager
{

    /** ************************************************** */
    /** Variables */
    /** ************************************************** */
    private static final String STYLE_DEFAULT_TITLE_CENTER = "Center Header";

    private static final String STYLE_DEFAULT_TITLE_LEFT = "Left Header";

    private static final String STYLE_DEFAULT_TITLE_RIGHT = HSSFHeader.font( "Stencil-Normal", "Italic" )
        + HSSFHeader.fontSize( (short) 16 ) + "Right w/ Stencil-Normal Italic font and size 12";

    private static final String STYLE_DEFAULT_FONT_NAME = "Tahoma";

    private static final short STYLE_DEFAULT_FONT_HEIGHT = 11;

    private static final short STYLE_DEFAULT_FONT_WEIGHT = Font.BOLDWEIGHT_NORMAL;

    private static final short STYLE_DEFAULT_PATTERN = CellStyle.SOLID_FOREGROUND;

    private static final short STYLE_DEFAULT_BORDER = CellStyle.BORDER_THIN;

    private static final short STYLE_DEFAULT_FONT_COLOR = IndexedColors.BROWN.getIndex();

    private static final short STYLE_DEFAULT_BACK_FORE_GROUND_COLOR = IndexedColors.WHITE.getIndex();

    private static final short STYLE_DEFAULT_BORDER_COLOR = IndexedColors.DARK_BLUE.getIndex();

    /***************************************************************************
     * Default Methods - Using for XLS Extension
     **************************************************************************/

    @SuppressWarnings( "static-access" )
    public void initDefaultHeader( HSSFHeader test_header )
    {
        test_header.setCenter( this.STYLE_DEFAULT_TITLE_CENTER );
        test_header.setLeft( this.STYLE_DEFAULT_TITLE_LEFT );
        test_header.setRight( this.STYLE_DEFAULT_TITLE_RIGHT );
        
    }

    @SuppressWarnings( "static-access" )
    public void initDefaultFont( Font test_font )
    {
        test_font.setFontName( this.STYLE_DEFAULT_FONT_NAME );
        test_font.setFontHeightInPoints( this.STYLE_DEFAULT_FONT_HEIGHT );
        test_font.setBoldweight( this.STYLE_DEFAULT_FONT_WEIGHT );
        test_font.setColor( this.STYLE_DEFAULT_FONT_COLOR );
        
    }

    @SuppressWarnings( "static-access" )
    public void initDefaultCellStyle( CellStyle test_cs, Font test_font )
    {
        test_cs.setFont( test_font );
        test_cs.setFillBackgroundColor( this.STYLE_DEFAULT_BACK_FORE_GROUND_COLOR );
        test_cs.setFillForegroundColor( this.STYLE_DEFAULT_BACK_FORE_GROUND_COLOR );
        test_cs.setFillPattern( this.STYLE_DEFAULT_PATTERN );
        test_cs.setBorderBottom( this.STYLE_DEFAULT_BORDER );
        test_cs.setBottomBorderColor( this.STYLE_DEFAULT_BORDER_COLOR );
        test_cs.setBorderTop( this.STYLE_DEFAULT_BORDER );
        test_cs.setTopBorderColor( this.STYLE_DEFAULT_BORDER_COLOR );
        test_cs.setBorderLeft( this.STYLE_DEFAULT_BORDER );
        test_cs.setLeftBorderColor( this.STYLE_DEFAULT_BORDER_COLOR );
        test_cs.setBorderRight( this.STYLE_DEFAULT_BORDER );
        test_cs.setRightBorderColor( this.STYLE_DEFAULT_BORDER_COLOR );

    }

    /** ************************************************** */
    /** Customized Methods - Using for XLS Extension */
    /** ************************************************** */

    public void initHeader( HSSFHeader test_header, String center, String left, String right )
    {

        test_header.setCenter( center );
        test_header.setLeft( left );
        test_header.setRight( right );

    }

    public void initFont( Font test_font, String fontName, short fontHeightInPoints, short boldWeight, short fontColor )
    {

    	test_font.setFontName( fontName );
        test_font.setFontHeightInPoints( fontHeightInPoints );
        test_font.setBoldweight( boldWeight );
        test_font.setColor( fontColor );

    }

    public void initCellStyle( CellStyle test_cs, Font font, short borderBottom, short bottomBorderColor,
        short borderTop, short topBorderColor, short borderLeft, short leftBorderColor, short borderRight,
        short rightBorderColor, short alignment, boolean bAutoWrap )
    {

        test_cs.setFont( font );
        test_cs.setBorderBottom( borderBottom );
        test_cs.setBottomBorderColor( bottomBorderColor );
        test_cs.setBorderTop( borderTop );
        test_cs.setTopBorderColor( topBorderColor );
        test_cs.setBorderLeft( borderLeft );
        test_cs.setLeftBorderColor( leftBorderColor );
        test_cs.setBorderRight( borderRight );
        test_cs.setRightBorderColor( rightBorderColor );
        test_cs.setAlignment( alignment );
        test_cs.setWrapText( bAutoWrap );

    }

    public void initCellStyle( CellStyle test_cs, Font font, short fillBgColor, short fillFgColor, short fillPattern,
        short borderBottom, short bottomBorderColor, short borderTop, short topBorderColor, short borderLeft,
        short leftBorderColor, short borderRight, short rightBorderColor, short dataFormat, short alignment,
        boolean bAutoWrap )
    {

        test_cs.setFont( font );
        test_cs.setFillBackgroundColor( fillBgColor );
        test_cs.setFillForegroundColor( fillFgColor );
        test_cs.setFillPattern( fillPattern );
        test_cs.setBorderBottom( borderBottom );
        test_cs.setBottomBorderColor( bottomBorderColor );
        test_cs.setBorderTop( borderTop );
        test_cs.setTopBorderColor( topBorderColor );
        test_cs.setBorderLeft( borderLeft );
        test_cs.setLeftBorderColor( leftBorderColor );
        test_cs.setBorderRight( borderRight );
        test_cs.setRightBorderColor( rightBorderColor );
        test_cs.setDataFormat( dataFormat );
        test_cs.setAlignment( alignment );
        test_cs.setWrapText( bAutoWrap );
    }

}
