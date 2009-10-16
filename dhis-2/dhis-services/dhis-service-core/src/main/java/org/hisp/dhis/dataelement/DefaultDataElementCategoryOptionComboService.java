package org.hisp.dhis.dataelement;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.common.GenericStore;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Abyot Asalefew
 * @version $Id$
 */
@Transactional
public class DefaultDataElementCategoryOptionComboService
    implements DataElementCategoryOptionComboService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private GenericStore<DataElementCategoryOptionCombo> dataElementCategoryOptionComboStore;

    public void setDataElementCategoryOptionComboStore(
        GenericStore<DataElementCategoryOptionCombo> dataElementCategoryOptionComboStore )
    {
        this.dataElementCategoryOptionComboStore = dataElementCategoryOptionComboStore;
    }

    private DataElementCategoryComboService dataElementCategoryComboService;

    public void setDataElementCategoryComboService( DataElementCategoryComboService dataElementCategoryComboService )
    {
        this.dataElementCategoryComboService = dataElementCategoryComboService;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }

    private DataElementCategoryOptionService dataElementCategoryOptionService;

    public void setDataElementCategoryOptionService( DataElementCategoryOptionService dataElementCategoryOptionService )
    {
        this.dataElementCategoryOptionService = dataElementCategoryOptionService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    // -------------------------------------------------------------------------
    // DataElementCategoryOptionCombo
    // -------------------------------------------------------------------------

    public int addDataElementCategoryOptionCombo( DataElementCategoryOptionCombo dataElementCategoryOptionCombo )
    {
        return dataElementCategoryOptionComboStore.save( dataElementCategoryOptionCombo );
    }

    public void updateDataElementCategoryOptionCombo( DataElementCategoryOptionCombo dataElementCategoryOptionCombo )
    {
        dataElementCategoryOptionComboStore.update( dataElementCategoryOptionCombo );
    }

    public void deleteDataElementCategoryOptionCombo( DataElementCategoryOptionCombo dataElementCategoryOptionCombo )
    {
        dataElementCategoryOptionComboStore.delete( dataElementCategoryOptionCombo );
    }

    public DataElementCategoryOptionCombo getDataElementCategoryOptionCombo( int id )
    {
        return dataElementCategoryOptionComboStore.get( id );
    }

    public Collection<DataElementCategoryOptionCombo> getDataElementCategoryOptionCombos(
        Collection<Integer> identifiers )
    {
        if ( identifiers == null )
        {
            return getAllDataElementCategoryOptionCombos();
        }

        Collection<DataElementCategoryOptionCombo> categoryOptionCombos = new ArrayList<DataElementCategoryOptionCombo>();

        for ( Integer id : identifiers )
        {
            categoryOptionCombos.add( getDataElementCategoryOptionCombo( id ) );
        }

        return categoryOptionCombos;
    }
    
    public DataElementCategoryOptionCombo getDataElementCategoryOptionCombo( Collection<DataElementCategoryOption> categoryOptions )
    {
        // TODO Re-implement with a Hibernate Criteria
        
        for ( DataElementCategoryOptionCombo categoryOptionCombo : getAllDataElementCategoryOptionCombos() )
        {
            if ( CollectionUtils.isEqualCollection( categoryOptions, categoryOptionCombo.getCategoryOptions() ) )
            {
                return categoryOptionCombo;
            }
        }
        
        return null;
    }
    
    public DataElementCategoryOptionCombo getDataElementCategoryOptionCombo( DataElementCategoryOptionCombo categoryOptionCombo )
    {
        for ( DataElementCategoryOptionCombo dcoc : getAllDataElementCategoryOptionCombos() )
        {
            // -----------------------------------------------------------------
            // Hibernate puts proxies on associations and makes the native
            // equals methods unusable
            // -----------------------------------------------------------------

            if ( dcoc.equalsOnName( categoryOptionCombo ) )
            {
                return dcoc;
            }
        }

        return null;
    }

    public Collection<DataElementCategoryOptionCombo> getAllDataElementCategoryOptionCombos()
    {
        return dataElementCategoryOptionComboStore.getAll();
    }
    
    public void generateDefaultDimension()
    {
        // ---------------------------------------------------------------------
        // Add default DataElementCategoryOption
        // ---------------------------------------------------------------------

        DataElementCategoryOption categoryOption = new DataElementCategoryOption(
            DataElementCategoryOption.DEFAULT_NAME );

        dataElementCategoryOptionService.addDataElementCategoryOption( categoryOption );

        // ---------------------------------------------------------------------
        // Add default DataElementCategory containing default
        // DataElementCategoryOption
        // ---------------------------------------------------------------------

        DataElementCategory category = new DataElementCategory( DataElementCategory.DEFAULT_NAME );

        List<DataElementCategoryOption> categoryOptions = new ArrayList<DataElementCategoryOption>();
        categoryOptions.add( categoryOption );
        category.setCategoryOptions( categoryOptions );

        dataElementCategoryService.addDataElementCategory( category );

        // ---------------------------------------------------------------------
        // Add default DataElementCategoryCombo made of the default
        // DataElementCategory
        // ---------------------------------------------------------------------

        DataElementCategoryCombo categoryCombo = new DataElementCategoryCombo(
            DataElementCategoryCombo.DEFAULT_CATEGORY_COMBO_NAME );

        List<DataElementCategory> categories = new ArrayList<DataElementCategory>();
        categories.add( category );
        categoryCombo.setCategories( categories );

        dataElementCategoryComboService.addDataElementCategoryCombo( categoryCombo );

        // ---------------------------------------------------------------------
        // Add default DataElementCategoryOptionCombo
        // ---------------------------------------------------------------------

        DataElementCategoryOptionCombo categoryOptionCombo = new DataElementCategoryOptionCombo();

        categoryOptionCombo.setCategoryCombo( categoryCombo );
        categoryOptionCombo.setCategoryOptions( new ArrayList<DataElementCategoryOption>( categoryOptions ) );

        addDataElementCategoryOptionCombo( categoryOptionCombo );

        Set<DataElementCategoryOptionCombo> categoryOptionCombos = new HashSet<DataElementCategoryOptionCombo>();
        categoryOptionCombos.add( categoryOptionCombo );
        categoryCombo.setOptionCombos( categoryOptionCombos );

        dataElementCategoryComboService.updateDataElementCategoryCombo( categoryCombo );
    }

    public DataElementCategoryOptionCombo getDefaultDataElementCategoryOptionCombo()
    {
        DataElementCategoryCombo categoryCombo = dataElementCategoryComboService
            .getDataElementCategoryComboByName( DataElementCategoryCombo.DEFAULT_CATEGORY_COMBO_NAME );

        return categoryCombo.getOptionCombos().iterator().next();
    }

    public Collection<Operand> getOperandsByIds( Collection<Integer> dataElementIdentifiers )
    {
        Collection<DataElement> dataElements = dataElementService.getDataElements( dataElementIdentifiers );

        return getOperands( dataElements );
    }

    public Collection<Operand> getOperands( Collection<DataElement> dataElements )
    {
        Collection<Operand> operands = new ArrayList<Operand>();

        for ( DataElement dataElement : dataElements )
        {
            Set<DataElementCategoryOptionCombo> categoryOptionCombos = dataElement.getCategoryCombo()
                .getOptionCombos();

            if ( categoryOptionCombos.size() > 1 && !(dataElement instanceof CalculatedDataElement) )
            {
                for ( DataElementCategoryOptionCombo optionCombo : categoryOptionCombos )
                {
                    Operand operand = new Operand( dataElement.getId(), optionCombo.getId(), dataElement.getName()
                        + optionCombo.getName(), new ArrayList<Integer>( dataElement.getAggregationLevels() ) );

                    operands.add( operand );
                }
            }
            else
            {
                Operand operand = new Operand( dataElement.getId(), categoryOptionCombos.iterator().next().getId(),
                    dataElement.getName(), new ArrayList<Integer>( dataElement.getAggregationLevels() ) );

                operands.add( operand );
            }
        }

        return operands;
    }

    public void generateOptionCombos( DataElementCategoryCombo categoryCombo )
    {

        int totalOptionCombos = 1;

        for ( DataElementCategory category : categoryCombo.getCategories() )
        {
            totalOptionCombos = totalOptionCombos * category.getCategoryOptions().size();
        }

        /*
         * Iterate through the collection of optionsMap every time picking one option
         * from each collection. Because we have put enough number of
         * options in each collection, better to remove the picked options
         * so that we don't get confused how many times to pick an option - pick
         * an option only once!
         */
        
        Map<Integer, Collection<DataElementCategoryOption>> optionsMap = prepareOptionsForCombination( categoryCombo ); 

        Set<DataElementCategoryOptionCombo> optionCombos = new HashSet<DataElementCategoryOptionCombo>(
            totalOptionCombos );

        for ( int i = 0; i < totalOptionCombos; i++ )
        {
            List<DataElementCategoryOption> options = new ArrayList<DataElementCategoryOption>( categoryCombo
                .getCategories().size() );

            /*
             * We are going to iterate the list of categories a number of times.
             * better to create a copy and iterate through the copy. we can stop
             * iterating when we have create the required option combinations.
             */
            Collection<DataElementCategory> copyOfCategories = categoryCombo.getCategories();

            Iterator<DataElementCategory> categoryIterator = copyOfCategories.iterator();

            while ( categoryIterator.hasNext() )
            {
                DataElementCategory cat = categoryIterator.next();

                /*
                 * From each category pick one option
                 */
                Iterator<DataElementCategoryOption> optionIterator = optionsMap.get( cat.getId() ).iterator();

                DataElementCategoryOption option = optionIterator.next();

                options.add( option );

                /*
                 * Once we used the option, better to remove it. because we have
                 * enough number of options
                 */

                optionIterator.remove();
            }

            DataElementCategoryOptionCombo optionCombo = new DataElementCategoryOptionCombo();

            optionCombo.setCategoryCombo( categoryCombo );

            optionCombo.setCategoryOptions( options );

            addDataElementCategoryOptionCombo( optionCombo );

            optionCombos.add( optionCombo );

        }

        categoryCombo.setOptionCombos( optionCombos );

        dataElementCategoryComboService.updateDataElementCategoryCombo( categoryCombo );

    }

    public Collection<DataElementCategoryOptionCombo> sortOptionCombos( DataElementCategoryCombo categoryCombo )
    {
        Collection<DataElementCategoryOptionCombo> optionCombos = new ArrayList<DataElementCategoryOptionCombo>(
            categoryCombo.getOptionCombos() );

        int totalColumns = optionCombos.size();        

        Map<Integer, Collection<DataElementCategoryOption>> orderedOptions = prepareOptionsForCombination( categoryCombo );

        Collection<DataElementCategoryOptionCombo> orderdCategoryOptionCombos = new ArrayList<DataElementCategoryOptionCombo>();

        for ( int i = 0; i < totalColumns; i++ )
        {
            Collection<DataElementCategoryOption> options = new ArrayList<DataElementCategoryOption>( categoryCombo
                .getCategories().size() );

            Collection<DataElementCategory> copyOforderedCategories = categoryCombo.getCategories();

            Iterator<DataElementCategory> categoryIterator = copyOforderedCategories.iterator();

            while ( categoryIterator.hasNext() )
            {
                DataElementCategory category = categoryIterator.next();
                Iterator<DataElementCategoryOption> optionIterator = orderedOptions.get( category.getId() ).iterator();
                DataElementCategoryOption option = optionIterator.next();
                options.add( option );
                optionIterator.remove();
            }

            for ( DataElementCategoryOptionCombo optionCombo : optionCombos )
            {
                if ( optionCombo.getCategoryOptions().containsAll( options ) )
                {
                    orderdCategoryOptionCombos.add( optionCombo );
                    break;
                }
            }
        }

        return orderdCategoryOptionCombos;
        
    }

    // -------------------------------------------------------------------------
    // DataElementCategoryOptionCombo
    // -------------------------------------------------------------------------

    private Map<Integer, Collection<DataElementCategoryOption>> prepareOptionsForCombination(
        DataElementCategoryCombo categoryCombo )
    {
        // Get categories for a given category
        
        List<DataElementCategory> categories = new ArrayList<DataElementCategory>( categoryCombo.getCategories() );

        /*
         * Get the total number of option combinations that will come into
         * existence when combining categories having their own options
         * 
         * Eg. Category SEX with Options MALE and FEMALE Category AGE with
         * Options <5years, 5-15years, >15years When combining these two
         * categories we are going to have a total of 6 option combinations
         * MALE_<5years,MALE_5-15years,MALE_>15years
         * FEMALE_<5years,FEMALE_5-15years,FEMALE_>15years
         * 
         * 6 OptionCombinations = 2(from SEX) * 3(from AGE)
         * 
         * generalizing this we can have total option combinations by
         * multiplying the number of options in each category
         */
        
        int totalOptionCombos = 1;

        for ( DataElementCategory category : categories )
        {
            totalOptionCombos = totalOptionCombos * category.getCategoryOptions().size();
        }       

        // ---------------------------------------------------------------------
        // Determine the number of times each category is going to repeat
        // ---------------------------------------------------------------------
        
        
        /* Example again Category IPD_OPD, Options I and D
         *               Category Sex, Options F and M
         *               Category Age, Options 0-5,5-10,11+
         *               
         * Category combination is IPD_OPD+Sex+Age
         * 
         * Option combinations ... with the following arrangement in Data Entry
         *  
         *   I |  I | I | I | I  |  I | O |  O | O | O | O  |  O    
         *   F |  F | F | F | F  |  F | M |  M | M | M | M  |  M     
         *  0-5|5-10|11+|0-5|5-10|11+1|0-5|5-10|11+|0-5|5-10|11+1
         *  
         *  If we rearrange our categories like IPD_OPD+Age+Sex - then we will 
         *  have the same option combinations, but with different arrangement. 
         *  
         *   I |  I | I  | I  | I  |  I | O |  O | O  | O  | O  |  O    
         *  0-5| 0-5|5-10|5-10| 11+| 11+|0-5| 0-5|5-10|5-10| 11+|11+     
         *  F  |  M | F  | M  |  F |  M |F  |  M | F  | M  |  F |  M 
         *  
         *  If we assume that we will draw a data entry table header, the top 
         *  a category is in the order list, then the more ColSpan its options 
         *  are going to have
         *   
         */
        

        int categoryColSpan = totalOptionCombos;

        Map<Integer, Integer> categoryRepeat = new HashMap<Integer, Integer>();

        for ( DataElementCategory category : categories )
        {
            categoryColSpan = categoryColSpan / category.getCategoryOptions().size();

            categoryRepeat.put( category.getId(), categoryColSpan );
        }

        /*
         * If we see the above example, any option from AGE appear only twice
         * while an option from SEX appears three times....generalizing this we
         * can say set the following formula let
         * 
         * appearance = appearance of any option from a given category
         * category_options = number of options from the category containing the
         * option and option_combinations = total number of option combinations
         * 
         * appearance = option_combinaitions/category_options
         * 
         * each option becoming part of the option combinations for 'appearance'
         * number of times, then totally a category will be represented in the
         * option combinations option_combinaitions number of times.
         * 
         * Then we can prepare list of categories containing collection of its
         * options where each option is repeated 'appearance' times. By doing
         * this, we can iterate through these categories every time removing an
         * option from the category but putting it in the option combinations.
         */

        Map<Integer, Collection<DataElementCategoryOption>> optionsMap = new HashMap<Integer, Collection<DataElementCategoryOption>>();

        /*
         * For each category create a collection of options by repeating each of
         * its options 'appearance' number of times. The size of the collection
         * should be equal to total number of options combinations.
         */
        for ( DataElementCategory cat : categories )
        {
            Collection<DataElementCategoryOption> requiredOptions = new ArrayList<DataElementCategoryOption>();
            Collection<DataElementCategoryOption> options = cat.getCategoryOptions();

            int count = 0;

            while ( count < totalOptionCombos )
            {
                for ( DataElementCategoryOption option : options )
                {
                    for ( int i = 0; i < categoryRepeat.get( cat.getId() ); i++ )
                    {
                        requiredOptions.add( option );

                        count++;
                    }
                }
            }

            optionsMap.put( cat.getId(), requiredOptions );
        }

        return optionsMap;
    }
}
