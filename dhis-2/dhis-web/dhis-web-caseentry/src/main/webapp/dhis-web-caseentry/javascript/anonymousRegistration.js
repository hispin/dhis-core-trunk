
function organisationUnitSelected( orgUnits )
{
	hideById('dataEntryFormDiv');
	disable( 'executionDate' );
	setFieldValue('executionDate', '');
	$('#executionDate').unbind('change');
	
	disable('createEventBtn');
	disable('deleteCurrentEventBtn');
	disable('showEventBtn');
	
	$.postJSON( 'loadAnonymousPrograms.action',{}
		, function( json ) 
		{
			clearListById( 'programId' );
			addOptionById( 'programId', '', i18n_please_select );
			
			var preSelectedProgramId = getFieldValue('selectedProgramId');
			for ( i in json.programInstances ) 
			{ 
				if( preSelectedProgramId == json.programInstances[i].id )
				{
					$('#programId').append('<option selected value=' + json.programInstances[i].id + ' singleevent="true" programInstanceId=' + json.programInstances[i].programInstanceId + '>' + json.programInstances[i].name + '</option>');
				}
				else
				{
					$('#programId').append('<option value=' + json.programInstances[i].id + ' singleevent="true" programInstanceId=' + json.programInstances[i].programInstanceId + '>' + json.programInstances[i].name + '</option>');
				}
			}

			if( byId('programId').selectedIndex > 0 )
			{
				loadEventForm();
			}
			else
			{
				if( json.programInstances.length > 0 )
				{
					enable('createEventBtn');
				}
			}
			
		} );
}

selection.setListenerFunction( organisationUnitSelected );


function loadEventForm()
{	
	hideById('dataEntryFormDiv');
	setFieldValue('executionDate', '');
	disable( 'executionDate' );
	
	var programId = getFieldValue('programId');
	if( programId == '' )
	{
		disable('showEventBtn');
		return;
	}
	
	showLoader();
	
	jQuery.postJSON( "loadProgramStages.action",
		{
			programId: programId
		}, 
		function( json ) 
		{    
			enable('showEventBtn');
			setFieldValue( 'programStageId', json.programStages[0].id );
			setFieldValue( 'selectedProgramId', programId );
			
			if( json.programStageInstances.length > 0 )
			{
				loadEventRegistrationForm();
			}
			else
			{
				enable( 'executionDate' );
				enable('createEventBtn');
				disable('deleteCurrentEventBtn');
				disable('completeBtn');
				hideById('loaderDiv');
			}
			
	});
}

function loadEventRegistrationForm()
{
	$( '#dataEntryFormDiv' ).load( "dataentryform.action", 
		{ 
			programStageId:getFieldValue('programStageId')
		},function( )
		{
			hideById('loaderDiv');
			showById('dataEntryFormDiv');
			
			var programStageInstanceId = getFieldValue('programStageInstanceId');
			if( programStageInstanceId == '' )
			{
				enable('createEventBtn');
				disable('deleteCurrentEventBtn');
				disable('completeBtn');
				enable( 'executionDate' );
				$('#executionDate').bind('change');
			}
			else
			{
				if( getFieldValue('completed') == 'true')
				{
					disable( 'executionDate' );
					enable('createEventBtn');
					enable('deleteCurrentEventBtn');
					disable('completeBtn');
				} 
				else
				{
					disable('createEventBtn');
					enable('deleteCurrentEventBtn');
					enable('completeBtn');
					enable( 'executionDate' );
					jQuery('#executionDate').bind('change');
				}
			}
			
		} );
}

function createNewEvent()
{
	jQuery.postJSON( "createAnonymousEncounter.action",
		{
			programInstanceId: jQuery('select[id=programId] option:selected').attr('programInstanceId'),
			executionDate: getFieldValue('executionDate')
		}, 
		function( json ) 
		{    
			selection.enable();
			
			if(json.response=='success')
			{
				disable('createEventBtn');
				enable('deleteCurrentEventBtn');
				setFieldValue('programStageInstanceId', json.message );
				
				selection.disable();
				
				loadEventRegistrationForm();
			}
			else
			{
				showWarmingMessage( json.message );
			}
			
		});
}

function deleteCurrentEvent()
{	
	var result = window.confirm( i18n_comfirm_delete_current_event );
    
    if ( result )
    {
		jQuery.postJSON( "removeCurrentEncounter.action",
			{
				programStageInstanceId: getFieldValue('programStageInstanceId')
			}, 
			function( json ) 
			{    
				var type = json.response;
				
				if( type == 'success' )
				{
					hideById('dataEntryFormDiv');
					byId('programId').selectedIndex = 0;
					
					disable('deleteCurrentEventBtn');
					enable('createEventBtn');
					
					setFieldValue('executionDate','');
					enable( 'executionDate' );
					$('#executionDate').unbind('change');
					
					selection.enable();
					
					showSuccessMessage( i18n_delete_current_event_success );
				}
				else if( type == 'input' )
				{
					showWarningMessage( json.message );
				}
			});
	}
}

isAjax = true;
function showHistoryEvents()
{
	contentDiv = 'dataEntryFormDiv';
	$( '#dataEntryFormDiv' ).load( "getEventsByProgram.action", 
		{ 
			programInstanceId: jQuery('select[id=programId] option:selected').attr('programInstanceId')
		},function( )
		{
		});
}


function viewRecords( programStageInstanceId ) 
{
	$('#viewEventDiv' )
		.load( 'viewAnonymousEvents.action?programStageInstanceId=' + programStageInstanceId )
		.dialog({
			title: i18n_event_information,
			maximize: true, 
			closable: true,
			modal:true,
			overlay:{background:'#000000', opacity:0.1},
			width: 800,
			height: 400
		});
}