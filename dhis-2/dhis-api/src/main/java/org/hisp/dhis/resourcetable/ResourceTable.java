package org.hisp.dhis.resourcetable;

/*
 * Copyright (c) 2004-2015, University of Oslo
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

import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.CodeGenerator;

/**
 * @author Lars Helge Overland
 */
public abstract class ResourceTable<T>
{
    protected static final Log log = LogFactory.getLog( ResourceTable.class );
    
    protected static final String TEMP_TABLE_SUFFIX = "_temp";
    
    protected String tableName;
    
    protected List<T> objects;
    
    protected String columnQuote;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    protected ResourceTable()
    {
    }
    
    public ResourceTable( String tableName, List<T> objects, String columnQuote )
    {
        this.tableName = tableName;
        this.objects = objects;
        this.columnQuote = columnQuote;
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    public final String getTableName()
    {
        return tableName;
    }
    
    public final String getTempTableName()
    {
        return tableName + TEMP_TABLE_SUFFIX;
    }
    
    public final String getSwapTablesStatement()
    {
        final String sql = 
            "drop table " + getTableName() + ";" +
            "alter table " + getTempTableName() + " rename to " + getTableName() + ";";
        
        return sql;
    }

    // -------------------------------------------------------------------------
    // Protected methods
    // -------------------------------------------------------------------------

    protected String getRandomSuffix()
    {
        return CodeGenerator.generateCode( 5 );
    }
    
    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    public abstract String getCreateTempTableStatement();
    
    public abstract Optional<String> getPopulateTempTableStatement();
    
    public abstract Optional<List<Object[]>> getPopulateTempTableContent();
    
    /**
     * Get a SQL index create statement. Note that the index name must have a 
     * random component to avoid uniqueness conflicts.
     * 
     * @return an optional SQL statement.
     */
    public abstract Optional<String> getCreateIndexStatement();
}
