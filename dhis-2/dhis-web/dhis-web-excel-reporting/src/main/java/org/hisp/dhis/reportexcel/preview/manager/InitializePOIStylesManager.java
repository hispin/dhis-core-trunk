package org.hisp.dhis.reportexcel.preview.manager;

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

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHeader;

/**
 * @author Dang Duy Hieu
 * @version $Id: InitializePOIStylesManager.java 2009-09-18 16:45:00Z hieuduy$
 */
public interface InitializePOIStylesManager
{

    /** ************************************************** */
    /** Methods */
    /** ************************************************** */

    // HSSFHeader initDefaultHeader();
    void initDefaultHeader( HSSFHeader header );
    
    // HSSFFont initDefaultFont();
    void initDefaultFont( HSSFFont font );
    
    // HSSFCellStyle initDefaultCellStyle();
    void initDefaultCellStyle( HSSFCellStyle cs, HSSFFont font );
    
    // HSSFHeader initHeader( String sCenter, String sLeft, String sRight );

    // HSSFFont initFont( String sFontName, short fontHeightInPoints, short
    // boldWeight, short fontColor );

    // HSSFCellStyle initCellStyle( HSSFFont hssffont, short borderBottom, short
    // bottomBorderColor,
      // short borderTop, short topBorderColor, short borderLeft, short
        // leftBorderColor, short borderRight,
      // short rightBorderColor, short alignment );

    // HSSFCellStyle initCellStyle( HSSFFont hssffont, short fillBgColor, short
    // fillFgColor, short fillPattern,
      // short borderBottom, short bottomBorderColor, short borderTop, short
        // topBorderColor, short borderLeft,
      // short leftBorderColor, short borderRight, short rightBorderColor,
        // short dataFormat, short alignment );

    
    void initHeader(HSSFHeader header, String sCenter, String sLeft, String sRight );

    void initFont(HSSFFont test_font, String sFontName, short fontHeightInPoints, short boldWeight, short fontColor );

    void initCellStyle(HSSFCellStyle test_cs, HSSFFont hssffont, short borderBottom, short bottomBorderColor,
        short borderTop, short topBorderColor, short borderLeft, short leftBorderColor, short borderRight,
        short rightBorderColor, short alignment );

    void initCellStyle(HSSFCellStyle test_cs, HSSFFont hssffont, short fillBgColor, short fillFgColor, short fillPattern,
        short borderBottom, short bottomBorderColor, short borderTop, short topBorderColor, short borderLeft,
        short leftBorderColor, short borderRight, short rightBorderColor, short dataFormat, short alignment );

    
}
