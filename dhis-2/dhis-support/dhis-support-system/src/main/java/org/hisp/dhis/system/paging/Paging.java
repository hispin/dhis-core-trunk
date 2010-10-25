package org.hisp.dhis.system.paging;

/*
 * Copyright (c) 2004-2009, University of Oslo
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

/**
 * @author Quang Nguyen
 */
public class Paging
{
    static final int MAX_ALLOWED_PAGE_SIZE = 50;

    private int currentPage;

    private int pageSize;

    private int total;

    private String link;

    public Paging()
    {
    }

    public Paging( String link, int pageSize )
    {
        currentPage = 1;
        this.pageSize = pageSize;
        total = 0;
        this.link = link;
    }

    public String getBaseLink()
    {
        return link.indexOf( "?" ) < 0 ? ( link + "?" ) : ( link + "&" );
    }

    public int getNumberOfPages()
    {
        return  total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }

    public int getStartPage()
    {
        int startPage = 1;
        
        if ( currentPage > 2 )
        {
            startPage = currentPage - 2;
            
            if ( getNumberOfPages() - startPage < 4 )
            {
                startPage = getNumberOfPages() - 4;
                
                if ( startPage <= 0 )
                {
                    startPage = 1;
                }
            }
        }
        return startPage;
    }

    public int getStartPos()
    {
        return currentPage <= 0 ? 0 : (currentPage - 1) * pageSize;
    }

    public int getEndPos()
    {
        int endPos = (getStartPos() + getPageSize()) - 1;
        endPos = endPos >= getTotal() ? getTotal() - 1 : endPos;
        return endPos;
    }

    public int getCurrentPage()
    {
        if ( currentPage > total )
        {
            currentPage = total;
        }
        
        return currentPage;
    }

    public void setCurrentPage( int currentPage )
    {
        this.currentPage = currentPage > 0 ? currentPage : 1;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( int pageSize )
    {
        this.pageSize = pageSize > 0 ? pageSize : 50;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal( int total )
    {
        this.total = total;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink( String link )
    {
        this.link = link;
    }
}
