package org.hisp.dhis.organisationunit;

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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.hierarchy.HierarchyViolationException;

/**
 * Defines methods for working with OrganisationUnits.
 * 
 * @author Torgeir Lorange Ostby
 * @version $Id: OrganisationUnitService.java 5951 2008-10-16 17:41:34Z larshelg $
 */
public interface OrganisationUnitService
{
    String ID = OrganisationUnitService.class.getName();

    // -------------------------------------------------------------------------
    // OrganisationUnit
    // -------------------------------------------------------------------------

    /**
     * Adds an OrganisationUnit to the hierarchy.
     * 
     * @param organisationUnit the OrganisationUnit to add.
     * @return a generated unique id of the added OrganisationUnit.
     */
    public int addOrganisationUnit( OrganisationUnit organisationUnit );

    /**
     * Updates an OrganisationUnit.
     * 
     * @param organisationUnit the OrganisationUnit to update.
     */
    void updateOrganisationUnit( OrganisationUnit organisationUnit );

    /**
     * Updates an OrganisationUnit.
     * 
     * @param organisationUnit the organisationUnit to update.
     * @param updateHierarchy indicate whether the OrganisationUnit hierarchy
     *        has been updated.
     */
    void updateOrganisationUnit( OrganisationUnit organisationUnit, boolean updateHierarchy );
    
    /**
     * Deletes an OrganisationUnit. OrganisationUnits with children cannot be
     * deleted.
     * 
     * @param organisationUnit the OrganisationUnit to delete.
     * @throws HierarchyViolationException if the OrganisationUnit has children.
     */
    void deleteOrganisationUnit( OrganisationUnit organisationUnit )
        throws HierarchyViolationException;

    /**
     * Returns an OrganisationUnit.
     * 
     * @param id the id of the OrganisationUnit to return.
     * @return the OrganisationUnit with the given id, or null if no match.
     */
    OrganisationUnit getOrganisationUnit( int id );
    
    /**
     * Returns the OrganisationUnit with the given UUID.
     * 
     * @param uuid the UUID of the OrganisationUnit to return.
     * @return the OrganisationUnit with the given UUID, or null if no match.
     */
    OrganisationUnit getOrganisationUnit( String uuid );

    /**
     * Returns all OrganisationUnits.
     * 
     * @return a collection of all OrganisationUnits, or an empty collection if
     *         there are no OrganisationUnits.
     */
    Collection<OrganisationUnit> getAllOrganisationUnits();

    /**
     * Returns all OrganisationUnits with corresponding identifiers.
     * 
     * @param identifiers the collection of identifiers.
     * @return a collection of OrganisationUnits.
     */
    Collection<OrganisationUnit> getOrganisationUnits( Collection<Integer> identifiers );
    
    /**
     * Returns an OrganisationUnit with a given name.
     * 
     * @param name the name of the OrganisationUnit to return.
     * @return the OrganisationUnit with the given name, or null if not match.
     */
    OrganisationUnit getOrganisationUnitByName( String name );

    /**
     * Returns an OrganisationUnit with a given short name.
     * 
     * @param shortName the short name of the OrganisationUnit to return.
     * @return the OrganisationUnit with the given short name, or null if no
     *         match.
     */
    OrganisationUnit getOrganisationUnitByShortName( String shortName );

    /**
     * Returns all root OrganisationUnits. A root OrganisationUnit is an
     * OrganisationUnit with no parent/the parent set to null.
     * 
     * @return a collection containing all root OrganisationUnits, or an empty
     *         collection if there are no OrganisationUnits.
     */
    Collection<OrganisationUnit> getRootOrganisationUnits();

    /**
     * Returns an OrganisationUnit and all its children.
     * 
     * @param id the id of the parent OrganisationUnit in the subtree.
     * @return a collection containing the OrganisationUnit with the given id
     *         and all its children, or an empty collection if no
     *         OrganisationUnits match.
     */
    Collection<OrganisationUnit> getOrganisationUnitWithChildren( int id );

    /**
     * Returns the branch of OrganisationUnits from a root to a given
     * OrganisationUnit. Both root and target OrganisationUnits are included in
     * the returned collection.
     * 
     * @param id the id of the OrganisationUnit to trace upwards from.
     * @return the list of OrganisationUnits from a root to the given
     *         OrganisationUnit, or an empty list if the given OrganisationUnit
     *         doesn't exist.
     */
    List<OrganisationUnit> getOrganisationUnitBranch( int id );

    /**
     * Returns all OrganisationUnits at a given hierarchical level. The root
     * OrganisationUnits are at level 1.
     * 
     * @param level the hierarchical level.
     * @return a collection of all OrganisationUnits at a given hierarchical
     *         level, or an empty collection if the level is empty.
     * @throws IllegalArgumentException if the level is zero or negative.
     */
    Collection<OrganisationUnit> getOrganisationUnitsAtLevel( int level );
    
    /**
     * Returns all OrganisationUnits which are children of the given unit and are
     * at the given hierarchical level. The root OrganisationUnits are at level 1.
     * 
     * @param level the hierarchical level.
     * @param parent the parent unit.
     * @return all OrganisationUnits which are children of the given unit and are
     *         at the given hierarchical level.
     * @throws IllegalArgumentException if the level is illegal.
     */
    Collection<OrganisationUnit> getOrganisationUnitsAtLevel( int level, OrganisationUnit parent );

    /**
     * Returns the hierarchical level in which the given OrganisationUnit
     * resides.
     * 
     * @param id the identifier of the OrganisationUnit.
     * @return the hierarchical level of the given OrganisationUnit.
     */
    int getLevelOfOrganisationUnit( int id );
    
    /**
     * Returns the hierarchical level in which the given OrganisationUnit
     * resides.
     * 
     * @param organisationUnit the OrganisationUnit.
     * @return the hierarchical level of the given OrganisationUnit.
     */
    int getLevelOfOrganisationUnit( OrganisationUnit organisationUnit );

    /**
     * Returns the number of levels in the OrganisationUnit hierarchy.
     * 
     * @return the number of hierarchical levels.
     */
    int getNumberOfOrganisationalLevels();

    /**
     * Set parent of OrganisationUnit.
     * 
     * @param organisationUnitId the identifier of the child OrganisationUnit.
     * @param parentId the identifier of the parent OrganisationUnit.
     */
    void updateOrganisationUnitParent( int organisationUnitId, int parentId );

    /**
     * Returns all OrganisationUnits which are not a member of any OrganisationUnitGroups.
     * 
     * @return all OrganisationUnits which are not a member of any OrganisationUnitGroups.
     */
    Collection<OrganisationUnit> getOrganisationUnitsWithoutGroups();
    
    // -------------------------------------------------------------------------
    // OrganisationUnitHierarchy
    // -------------------------------------------------------------------------

    /**
     * This method is not intended to be used by other modules!
     */
    int addOrganisationUnitHierarchy( Date date );

    /**
     * This method is not intended to be used by other modules!
     */
    OrganisationUnitHierarchy getOrganisationUnitHierarchy( int id );
    
    /**
     * This method is not intended to be used by other modules!
     */
    OrganisationUnitHierarchy getLatestOrganisationUnitHierarchy();

    /**
     * This method is not intended to be used by other modules!
     */
    void removeOrganisationUnitHierarchies( Date date );
    
    /**
     * This method is not intended to be used by other modules!
     */
    Collection<OrganisationUnitHierarchy> getOrganisationUnitHierarchies( Date startDate, Date endDate );

    /**
     * This method is not intended to be used by other modules!
     */
    void clearOrganisationUnitHierarchyHistory();
    
    /**
     * This method is not intended to be used by other modules!
     */
    Collection<Integer> getChildren( int organisationUnitHierarchyId, int parentId );

    // -------------------------------------------------------------------------
    // OrganisationUnitLevel
    // -------------------------------------------------------------------------

    int addOrganisationUnitLevel( OrganisationUnitLevel level );
    
    void updateOrganisationUnitLevel( OrganisationUnitLevel level );
    
    void addOrUpdateOrganisationUnitLevel( OrganisationUnitLevel level );
    
    void pruneOrganisationUnitLevels( Set<Integer> currentLevels );
    
    OrganisationUnitLevel getOrganisationUnitLevel( int id );
    
    Collection<OrganisationUnitLevel> getOrganisationUnitLevels( Collection<Integer> identifiers );
    
    void deleteOrganisationUnitLevel( OrganisationUnitLevel level );

    void deleteOrganisationUnitLevels();
    
    List<OrganisationUnitLevel> getOrganisationUnitLevels();
    
    OrganisationUnitLevel getOrganisationUnitLevelByLevel( int level );
    
    OrganisationUnitLevel getOrganisationUnitLevelByName( String name );
    
    List<OrganisationUnitLevel> getFilledOrganisationUnitLevels();
    
    int getNumberOfOrganisationUnits();
}
