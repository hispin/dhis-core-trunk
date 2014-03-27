package org.hisp.dhis.caseentry.action.trackedentity;

/*
 * Copyright (c) 2004-2014, University of Oslo
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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramIndicatorService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAudit;
import org.hisp.dhis.trackedentity.TrackedEntityAuditService;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.user.CurrentUserService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * 
 * @version TrackedEntityInstanceDashboardAction.java 1:30:29 PM Aug 10, 2012 $
 */
public class TrackedEntityInstanceDashboardAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityInstanceService entityInstanceService;

    private RelationshipService relationshipService;

    private TrackedEntityAuditService auditService;

    private CurrentUserService currentUserService;

    private ProgramService programService;

    private ProgramIndicatorService programIndicatorService;

    private TrackedEntityAttributeValueService attributeValueService;

    // -------------------------------------------------------------------------
    // Input && Output
    // -------------------------------------------------------------------------

    private String entityInstanceId;

    private TrackedEntityInstance entityInstance;

    private Collection<TrackedEntityAttributeValue> attributeValues;

    private Collection<ProgramInstance> activeProgramInstances;

    private Collection<ProgramInstance> completedProgramInstances;

    private Collection<TrackedEntityAudit> entityInstanceAudits;

    private Map<TrackedEntityAttribute, String> attributeMap = new HashMap<TrackedEntityAttribute, String>();

    private Collection<Relationship> relationships = new HashSet<Relationship>();

    private Map<String, String> programIndicatorsMap = new HashMap<String, String>();

    private I18nFormat format;

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public Map<String, String> getProgramIndicatorsMap()
    {
        return programIndicatorsMap;
    }

    public void setAttributeValueService( TrackedEntityAttributeValueService attributeValueService )
    {
        this.attributeValueService = attributeValueService;
    }

    public void setAuditService( TrackedEntityAuditService auditService )
    {
        this.auditService = auditService;
    }

    public void setProgramIndicatorService( ProgramIndicatorService programIndicatorService )
    {
        this.programIndicatorService = programIndicatorService;
    }

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    public Map<TrackedEntityAttribute, String> getAttributeMap()
    {
        return attributeMap;
    }

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    public Collection<ProgramInstance> getActiveProgramInstances()
    {
        return activeProgramInstances;
    }

    public Collection<TrackedEntityAudit> getEntityInstanceAudits()
    {
        return entityInstanceAudits;
    }

    public Collection<ProgramInstance> getCompletedProgramInstances()
    {
        return completedProgramInstances;
    }

    public void setRelationshipService( RelationshipService relationshipService )
    {
        this.relationshipService = relationshipService;
    }

    public Collection<Relationship> getRelationships()
    {
        return relationships;
    }

    public TrackedEntityInstance getEntityInstance()
    {
        return entityInstance;
    }

    public Collection<TrackedEntityAttributeValue> getAttributeValues()
    {
        return attributeValues;
    }

    public void setEntityInstanceService( TrackedEntityInstanceService entityInstanceService )
    {
        this.entityInstanceService = entityInstanceService;
    }

    public void setEntityInstanceId( String entityInstanceId )
    {
        this.entityInstanceId = entityInstanceId;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        entityInstance = entityInstanceService.getTrackedEntityInstance( entityInstanceId );

        Collection<Program> programs = programService
            .getProgramsByCurrentUser( Program.MULTIPLE_EVENTS_WITH_REGISTRATION );
        programs.addAll( programService.getProgramsByCurrentUser( Program.SINGLE_EVENT_WITH_REGISTRATION ) );

        // ---------------------------------------------------------------------
        // Get relationship
        // ---------------------------------------------------------------------

        relationships = relationshipService.getRelationshipsForTrackedEntityInstance( entityInstance );

        Collection<ProgramInstance> programInstances = entityInstance.getProgramInstances();

        // ---------------------------------------------------------------------
        // Get entityInstance-attribute-values
        // ---------------------------------------------------------------------

        Collection<TrackedEntityAttributeValue> _attributeValues = attributeValueService
            .getTrackedEntityAttributeValues( entityInstance );
        attributeValues = new HashSet<TrackedEntityAttributeValue>();

        for ( Program program : programs )
        {
            Collection<TrackedEntityAttribute> atttributes = program.getTrackedEntityAttributes();
            for ( TrackedEntityAttributeValue attributeValue : _attributeValues )
            {
                if ( atttributes.contains( attributeValue.getAttribute() ) )
                {
                    String value = attributeValue.getValue();
                    if ( attributeValue.getAttribute().getValueType().equals( TrackedEntityAttribute.TYPE_AGE ) )
                    {
                        value = format.formatDate( TrackedEntityAttribute.getDateFromAge( Integer.parseInt( value ) ) );
                    }

                    attributeValue.setValue( value );
                    attributeValues.add( attributeValue );
                }
            }
        }

        // ---------------------------------------------------------------------
        // Get program enrollment
        // ---------------------------------------------------------------------

        activeProgramInstances = new HashSet<ProgramInstance>();

        completedProgramInstances = new HashSet<ProgramInstance>();

        for ( ProgramInstance programInstance : programInstances )
        {
            if ( programs.contains( programInstance.getProgram() ) )
            {
                if ( programInstance.getStatus() == ProgramInstance.STATUS_ACTIVE )
                {
                    activeProgramInstances.add( programInstance );

                    programIndicatorsMap.putAll( programIndicatorService.getProgramIndicatorValues( programInstance ) );
                }
                else
                {
                    completedProgramInstances.add( programInstance );
                }
            }
        }

        // ---------------------------------------------------------------------
        // Tracked Entity Instance Audit
        // ---------------------------------------------------------------------

        entityInstanceAudits = auditService.getTrackedEntityAudits( entityInstance );

        Calendar today = Calendar.getInstance();
        PeriodType.clearTimeOfDay( today );
        Date date = today.getTime();
        String visitor = currentUserService.getCurrentUsername();
        TrackedEntityAudit entityInstanceAudit = auditService.getTrackedEntityAudit( entityInstance.getId(), visitor,
            date, TrackedEntityAudit.MODULE_ENTITY_INSTANCE_DASHBOARD );
        if ( entityInstanceAudit == null )
        {
            entityInstanceAudit = new TrackedEntityAudit( entityInstance, visitor, date,
                TrackedEntityAudit.MODULE_ENTITY_INSTANCE_DASHBOARD );
            auditService.saveTrackedEntityAudit( entityInstanceAudit );
        }

        return SUCCESS;
    }
}
