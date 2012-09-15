
function organisationUnitSelected( orgUnits, orgUnitNames )
{	
	showById('selectDiv');
	showById('searchDiv');
	hideById('listPatientDiv');
	hideById('editPatientDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('migrationPatientDiv');
	hideById('patientDashboard');
	enable('listPatientBtn');
	enable('addPatientBtn');
	enable('advancedSearchBtn');
	setFieldValue("orgunitName", orgUnitNames[0]);
}

selection.setListenerFunction( organisationUnitSelected );

// -----------------------------------------------------------------------------
// Remove patient
// -----------------------------------------------------------------------------

function removePatient( patientId, fullName )
{
	removeItem( patientId, fullName, i18n_confirm_delete, 'removePatient.action' );
}

function sortPatients()
{
	hideById( 'listPatientDiv' );
	
	contentDiv = 'listPatientDiv';
	jQuery( "#loaderDiv" ).show();
	jQuery('#listPatientDiv').load("searchRegistrationPatient.action", 
		{
			sortPatientAttributeId: getFieldValue('sortPatientAttributeId')
		}, function(){
			showById('listPatientDiv');
			jQuery( "#loaderDiv" ).hide();
		});
}

// -----------------------------------------------------------------------------
// Add Patient
// -----------------------------------------------------------------------------

function validateAddPatient()
{	
	$("#patientForm :input").attr("disabled", true);
	$.ajax({
		type: "POST",
		url: 'validatePatient.action',
		data: getParamsForDiv('patientForm'),
		success:addValidationCompleted
    });	
}

function addValidationCompleted( data )
{
    var type = jQuery(data).find('message').attr('type');
	var message = jQuery(data).find('message').text();
	
	if ( type == 'success' )
	{
		removeDisabledIdentifier( );
		addPatient();
	}
	else
	{
		if ( type == 'error' )
		{
			showErrorMessage( i18n_adding_patient_failed + ':' + '\n' + message );
		}
		else if ( type == 'input' )
		{
			showWarningMessage( message );
		}
		else if( type == 'duplicate' )
		{
			showListPatientDuplicate(data, false);
		}
			
		$("#patientForm :input").attr("disabled", false);
	}
}


// -----------------------------------------------------------------------------
// Update Patient
// -----------------------------------------------------------------------------

function validateUpdatePatient()
{
	$("#editPatientDiv :input").attr("disabled", true);
	$.ajax({
		type: "POST",
		url: 'validatePatient.action',
		data: getParamsForDiv('editPatientDiv'),
		success:updateValidationCompleted
     });
}

function updateValidationCompleted( messageElement )
{
    var type = jQuery(messageElement).find('message').attr('type');
	var message = jQuery(messageElement).find('message').text();
    
    if ( type == 'success' )
    {
    	removeDisabledIdentifier();
    	updatePatient();
    }
	else
	{
		$("#editPatientDiv :input").attr("disabled", true);
		if ( type == 'error' )
		{
			showErrorMessage( i18n_saving_patient_failed + ':' + '\n' + message );
		}
		else if ( type == 'input' )
		{
			showWarningMessage( message );
		}
		else if( type == 'duplicate' )
		{
			showListPatientDuplicate(messageElement, true);
		}
		$("#editPatientDiv :input").attr("disabled", false);
	}
}
// get and build a param String of all the identifierType id and its value
// excluding inherited identifiers
function getIdParams()
{
	var params = "";
	jQuery("input.idfield").each(function(){
		if( jQuery(this).val() && !jQuery(this).is(":disabled") )
			params += "&" + jQuery(this).attr("name") +"="+ jQuery(this).val();
	});
	return params;
}

/**
 * Show list patient duplicate  by jQuery thickbox plugin
 * @param rootElement : root element of the response xml
 * @param validate  :  is TRUE if this method is called from validation method  
 */
function showListPatientDuplicate( rootElement, validate )
{
	var message = jQuery(rootElement).find('message').text();
	var patients = jQuery(rootElement).find('patient');
	
	var sPatient = "";
	jQuery( patients ).each( function( i, patient )
        {
			sPatient += "<hr style='margin:5px 0px;'><table>";
			sPatient += "<tr><td class='bold'>" + i18n_patient_system_id + "</td><td>" + jQuery(patient).find('systemIdentifier').text() + "</td></tr>" ;
			sPatient += "<tr><td class='bold'>" + i18n_patient_full_name + "</td><td>" + jQuery(patient).find('fullName').text() + "</td></tr>" ;
			sPatient += "<tr><td class='bold'>" + i18n_patient_gender + "</td><td>" + jQuery(patient).find('gender').text() + "</td></tr>" ;
			sPatient += "<tr><td class='bold'>" + i18n_patient_date_of_birth + "</td><td>" + jQuery(patient).find('dateOfBirth').text() + "</td></tr>" ;
			sPatient += "<tr><td class='bold'>" + i18n_patient_age + "</td><td>" + jQuery(patient).find('age').text() + "</td></tr>" ;
			sPatient += "<tr><td class='bold'>" + i18n_patient_phone_number + "</td><td>" + jQuery(patient).find('phoneNumber').text() + "</td></tr>";
        	
			var identifiers = jQuery(patient).find('identifier');
        	if( identifiers.length > 0 )
        	{
        		sPatient += "<tr><td colspan='2' class='bold'>" + i18n_patient_identifiers + "</td></tr>";

        		jQuery( identifiers ).each( function( i, identifier )
				{
        			sPatient +="<tr class='identifierRow'>"
        				+"<td class='bold'>" + jQuery(identifier).find('name').text() + "</td>"
        				+"<td>" + jQuery(identifier).find('value').text() + "</td>	"	
        				+"</tr>";
        		});
        	}
			
        	var attributes = jQuery(patient).find('attribute');
        	if( attributes.length > 0 )
        	{
        		sPatient += "<tr><td colspan='2' class='bold'>" + i18n_patient_attributes + "</td></tr>";

        		jQuery( attributes ).each( function( i, attribute )
				{
        			sPatient +="<tr class='attributeRow'>"
        				+"<td class='bold'>" + jQuery(attribute).find('name').text() + "</td>"
        				+"<td>" + jQuery(attribute).find('value').text() + "</td>	"	
        				+"</tr>";
        		});
        	}
        	sPatient += "<tr><td colspan='2'><input type='button' id='"+ jQuery(patient).find('id').first().text() + "' value='" + i18n_edit_this_patient + "' onclick='showUpdatePatientForm(this.id)'/></td></tr>";
        	sPatient += "</table>";
		});
		
		var result = i18n_duplicate_warning;
		if( !validate )
		{
			result += "<input type='button' value='" + i18n_create_new_patient + "' onClick='removeDisabledIdentifier( );addPatient();'/>";
			result += "<br><hr style='margin:5px 0px;'>";
		}
		
		result += "<br>" + sPatient;
		jQuery('#resultSearchDiv' ).html( result );
		jQuery('#resultSearchDiv' ).dialog({
			title: i18n_duplicated_patient_list,
			maximize: true, 
			closable: true,
			modal:true,
			overlay:{background:'#000000', opacity:0.1},
			width: 800,
			height: 400
		});
}

// -----------------------------------------------------------------------------
// Show representative form
// -----------------------------------------------------------------------------

function toggleUnderAge(this_)
{
	if( jQuery(this_).is(":checked"))
	{
		jQuery('#representativeDiv').dialog('destroy').remove();
		jQuery('<div id="representativeDiv">' ).load( 'showAddRepresentative.action',{},
			function(){
				$('#patientForm [id=birthDate]').attr('id','birthDate_id');
				$('#patientForm [id=birthDate_id]').attr('name','birthDate_id');
			}).dialog({
			title: i18n_child_representative,
			maximize: true, 
			closable: true,
			modal:true,
			overlay:{background:'#000000', opacity:0.1},
			width: 800,
			height: 450,
			close:function()
			{
				$('#patientForm [id=birthDate_id]').attr('id','birthDate');
				$('#patientForm [id=birthDate]').attr('name','birthDate');
			}
		});
	}else
	{
		jQuery("#representativeDiv :input.idfield").each(function(){
			if( jQuery(this).is(":disabled"))
			{
				jQuery(this).removeAttr("disabled").val("");
			}
		});
		jQuery("#representativeId").val("");
		jQuery("#relationshipTypeId").val("");
	}
}

// ----------------------------------------------------------------
// Add Patient
// ----------------------------------------------------------------

function showAddPatientForm()
{
	hideById('listPatientDiv');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('migrationPatientDiv');
	
	jQuery('#loaderDiv').show();
	jQuery('#editPatientDiv').load('showAddPatientForm.action'
		, function()
		{
			showById('editPatientDiv');
			jQuery('#loaderDiv').hide();
		});
	
}

function addPatient()
{
	$.ajax({
      type: "POST",
      url: 'addPatient.action',
      data: getParamsForDiv('patientForm'),
      success: function(json) {
		var patientId = json.message.split('_')[0];
		var systemIdentifierId = json.message.split('_')[1];
		jQuery('#advSearchBox0 [id="searchText"]').val( systemIdentifierId );
		statusSearching = 1;
		
		showPatientDashboardForm( patientId );
		jQuery('#resultSearchDiv').dialog('close');
      }
     });
    return false;
}

// ----------------------------------------------------------------
// Update Patient
// ----------------------------------------------------------------

function showUpdatePatientForm( patientId )
{
	hideById('listPatientDiv');
	setInnerHTML('editPatientDiv', '');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('migrationPatientDiv');
	setInnerHTML('patientDashboard','');
	
	jQuery('#loaderDiv').show();
	jQuery('#editPatientDiv').load('showUpdatePatientForm.action',
		{
			id:patientId
		}, function()
		{
			jQuery('#searchPatientsDiv').dialog('close');
			jQuery('#loaderDiv').hide();
			showById('editPatientDiv');
		});
		
	jQuery('#resultSearchDiv').dialog('close');
}

function updatePatient()
{
	$.ajax({
      type: "POST",
      url: 'updatePatient.action',
      data: getParamsForDiv('editPatientDiv'),
      success: function( json ) {
		showPatientDashboardForm( getFieldValue('id') );
      }
     });
}

// ----------------------------------------------------------------
// Enrollment program
// ----------------------------------------------------------------

function showProgramEnrollmentForm( patientId )
{
	jQuery('#enrollmentDiv').load('showProgramEnrollmentForm.action',
		{
			id:patientId
		}).dialog({
			title: i18n_enroll_program,
			maximize: true, 
			closable: true,
			modal:true,
			overlay:{background:'#000000', opacity:0.1},
			width: 500,
			height: 200
		});
}

function programOnchange( programId )
{
	if( programId==0){
		hideById('enrollmentDateTR');
		hideById('dateOfIncidentTR');
	}
	else{
		var type = jQuery('#enrollmentDiv [name=programId] option:selected').attr('programType')
		if(type=='2'){
			hideById('enrollmentDateTR');
			hideById('dateOfIncidentTR');
			disable('enrollmentDate');
			disable('dateOfIncident');
		}
		else{
			showById( 'enrollmentDateTR');
			enable('enrollmentDate');
			
			var dateOfEnrollmentDescription = jQuery('#enrollmentDiv [name=programId] option:selected').attr('dateOfEnrollmentDescription');
			var dateOfIncidentDescription = jQuery('#enrollmentDiv [name=programId] option:selected').attr('dateOfIncidentDescription');
			setInnerHTML('enrollmentDateDescription', dateOfEnrollmentDescription);
			setInnerHTML('dateOfIncidentDescription', dateOfIncidentDescription);
			
			var displayIncidentDate = jQuery('#enrollmentDiv [name=programId] option:selected').attr('displayIncidentDate');
			if( displayIncidentDate=='true'){
				showById( 'dateOfIncidentTR');
				enable('dateOfIncident');
			}
			else{
				hideById( 'dateOfIncidentTR');
				disable('dateOfIncident');
			}
		}
	}
}

function saveEnrollment( patientId, programId )
{
	var programId = jQuery('#enrollmentDiv [id=programId] option:selected').val();
	var programName = jQuery('#enrollmentDiv [id=programId] option:selected').text();
	var dateOfIncident = jQuery('#enrollmentDiv [id=dateOfIncidentField]').val();
	var enrollmentDate = jQuery('#enrollmentDiv [id=enrollmentDateField]').val();
	
	jQuery.postJSON( "saveProgramEnrollment.action",
		{
			patientId: jQuery('#enrollmentDiv [id=patientId]').val(),
			programId: programId,
			dateOfIncident: dateOfIncident,
			enrollmentDate: enrollmentDate
		}, 
		function( json ) 
		{    
			var programInstanceId = json.programInstanceId;
			var programStageInstanceId = json.activeProgramStageInstanceId;
			var programStageName = json.activeProgramStageName;
			var dueDate = json.dueDate;
			var type = jQuery('#enrollmentDiv [id=programId] option:selected').attr('programType');
			
			var activedRow = "<tr id='tr1_" + programInstanceId 
							+ "' type='" + type +"'"
							+ " programStageInstanceId='" + programStageInstanceId + "'>"
							+ " <td id='td_" + programInstanceId + "'>"
							+ " <a href='javascript:loadActiveProgramStageRecords(" + programInstanceId + "," + programStageInstanceId + "')'>"
							+ programName + "(" + enrollmentDate + ")</a></td>"
							+ "</tr>";
			
			activedRow += "<tr id='tr2_" + programInstanceId +"'"+
						+ " onclick='javascript:loadActiveProgramStageRecords(" + programInstanceId + "," + programStageInstanceId + ")' style='cursor:pointer;'>"
						+ "<td colspan='2'><a>&#8226; " + programStageName + "(" + dueDate + ")</a></td></tr>";

			jQuery('#activeTB' ).prepend(activedRow);
			jQuery('#enrollmentDiv').dialog("close");
			
			loadProgramInstance( programInstanceId, false );
			showSuccessMessage(i18n_enrol_success);
		});
}

function loadProgramInstance( programInstanceId, completed )
{				
	jQuery('#loaderDiv').show();
	jQuery('#programEnrollmentDiv').load('enrollmentform.action',
		{
			programInstanceId:programInstanceId
		}, function()
		{
			showById('programEnrollmentDiv');
			var type = jQuery('#tr_'+programInstanceId).attr('programType');
			if(type=='2'){
				hideById('programInstanceDiv');
				var programStageInstanceId = jQuery('#tr_'+programInstanceId).attr('programStageInstanceId');
				loadDataEntry( programStageInstanceId );
			}
			else{
				showById('programInstanceDiv');
			}
			activeProgramInstanceDiv( programInstanceId );
			if( completed ){
				hideById('newEncounterBtn_' + programInstanceId);
			}
			jQuery('#loaderDiv').hide();
			resize();
		});
}

function validateProgramEnrollment()
{	
	jQuery('#loaderDiv').show();
	$.ajax({
		type: "GET",
		url: 'validatePatientProgramEnrollment.action',
		data: getParamsForDiv('programEnrollmentSelectDiv'),
		success: function(json) {
			hideById('message');
			var type = json.response;
			if ( type == 'success' ){
				saveProgramEnrollment();
			}
			else if ( type == 'error' ){
				setMessage( i18n_program_enrollment_failed + ':' + '\n' + message );
			}
			else if ( type == 'input' ){
				setMessage( json.message );
			}
			jQuery('#loaderDiv').hide();
      }
    });
}

function saveProgramEnrollment()
{
	$.ajax({
		type: "POST",
		url: 'saveProgramEnrollment.action',
		data: getParamsForDiv('programEnrollmentSelectDiv'),
		success: function( html ) {
				setInnerHTML('programEnrollmentDiv', html );
				jQuery('#enrollBtn').attr('value',i18n_update);
				showSuccessMessage( i18n_enrol_success );
			}
		});
    return false;
}

// ----------------------------------------------------------------
// Un-Enrollment program
// ----------------------------------------------------------------

function showUnenrollmentSelectForm( patientId )
{
	hideById('listPatientDiv');
	hideById('editPatientDiv');
	hideById('selectDiv');
	hideById('searchDiv');
	hideById('migrationPatientDiv');
				
	jQuery('#loaderDiv').show();
	jQuery('#enrollmentDiv').load('showProgramUnEnrollmentForm.action',
		{
			patientId:patientId
		}, function()
		{
			showById('enrollmentDiv');
			jQuery('#loaderDiv').hide();
		});
}

function showUnenrollmentForm( programInstanceId )
{				
	if( programInstanceId == 0 )
	{
		hideById( 'unenrollmentFormDiv' );
		return;
	}
	
	jQuery('#loaderDiv').show();
	jQuery.getJSON( "getProgramInstance.action",
		{
			programInstanceId:programInstanceId
		}, 
		function( json ) 
		{   
			showById( 'unenrollmentFormDiv' );
			jQuery( "#loaderDiv" ).hide();
		});
}

function unenrollmentForm( programInstanceId )
{	
	$.ajax({
		type: "POST",
		url: 'removeEnrollment.action',
		data: "programInstanceId=" + programInstanceId,
		success: function( json ) 
		{
			var completedRow = jQuery('#td_' + programInstanceId).html();
			jQuery('#completedTB' ).prepend("<tr><td>" + completedRow + "</td></tr>");
			hideById('tr1_' + programInstanceId );
			hideById('tr2_' + programInstanceId );
			hideById('programEnrollmentDiv');
			showSuccessMessage( i18n_unenrol_success );
		}
    });
	
	
}

//----------------------------------------------------
// Show relationship with new patient
//----------------------------------------------------

function showRelationshipList( patientId )
{
	hideById('addRelationshipDiv');
	setInnerHTML('patientDashboard','');
	
	if ( getFieldValue('isShowPatientList') == 'false' )
	{
		hideById('selectDiv');
		hideById('searchDiv');
		hideById('listPatientDiv');

		jQuery('#loaderDiv').show();
		jQuery('#listRelationshipDiv').load('showRelationshipList.action',
			{
				id:patientId
			}, function()
			{
				showById('listRelationshipDiv');
				jQuery('#loaderDiv').hide();
			});
	}
	else
	{
		loadPatientList();
	}
}

// ----------------------------------------------------------------
// Click Back to Search button
// ----------------------------------------------------------------

function onClickBackBtn()
{
	showById('selectDiv');
	showById('searchDiv');
	showById('listPatientDiv');
	
	hideById('editPatientDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('migrationPatientDiv');
	setInnerHTML('patientDashboard','');
}

function loadPatientList()
{
	hideById('editPatientDiv');
	hideById('enrollmentDiv');
	hideById('listRelationshipDiv');
	hideById('addRelationshipDiv');
	hideById('dataRecordingSelectDiv');
	hideById('dataEntryFormDiv');
	hideById('migrationPatientDiv');
	
	showById('selectDiv');
	showById('searchDiv');
	
	if( statusSearching == 0)
	{
		listAllPatient();
	}
	else if( statusSearching == 1 )
	{
		validateAdvancedSearch();
	}
}

// -----------------------------------------------------------------------------
// Load all patients
// -----------------------------------------------------------------------------

function listAllPatient()
{
	hideById('listPatientDiv');
	hideById('editPatientDiv');
	hideById('migrationPatientDiv');
	
	jQuery('#loaderDiv').show();
	contentDiv = 'listPatientDiv';
	jQuery('#listPatientDiv').load('searchRegistrationPatient.action',{
			listAll:true
		},
		function(){
			statusSearching = 0;
			showById('listPatientDiv');
			jQuery('#loaderDiv').hide();
		});
	hideLoader();
}

// -----------------------------------------------------------------------------
// remove value of all the disabled identifier fields
// an identifier field is disabled when its value is inherited from another person ( underAge is true ) 
// we don't save inherited identifiers. Only save the representative id.
// -----------------------------------------------------------------------------

function removeDisabledIdentifier()
{
	jQuery("input.idfield").each(function(){
		if( jQuery(this).is(":disabled"))
			jQuery(this).val("");
	});
}

function addEventForPatientForm( divname )
{
	jQuery("#" + divname + " [id=checkDuplicateBtn]").click(function() {
		checkDuplicate( divname );
	});
	
	jQuery("#" + divname + " [id=dobType]").change(function() {
		dobTypeOnChange( divname );
	});
}

function showRepresentativeInfo( patientId)
{
	jQuery('#representativeInfo' ).dialog({
			title: i18n_representative_info,
			maximize: true, 
			closable: true,
			modal: false,
			overlay: {background:'#000000', opacity:0.1},
			width: 400,
			height: 300
		});
}

function hideEnrolmentField()
{
	setFieldValue( 'enrollmentDate', '' );
	setFieldValue( 'dateOfIncident', '' );
	hideById('enrollmentDateTR');
	hideById('dateOfIncidentTR');
	//hideById('enrollBtn');
	hideById('unenrollBtn');
}
  
function showEnrolmentField()
{
	showById('enrollmentDateTR');
	showById('dateOfIncidentTR');
	enable('dateOfIncident');
	//showById('enrollBtn');
}

function hideIncidentDateField()
{
	setFieldValue( 'dateOfIncident', '' );
	disable('dateOfIncident');
	hideById('dateOfIncidentTR');
}
  
function showIncidentDateField()
{
	showById('dateOfIncidentTR');
}

function saveIdentifierAndAttribute()
{
	$.ajax({
			type: "POST",
			url: 'savePatientIdentifierAndAttribute.action',
			data: getParamsForDiv('programEnrollmentInforForm'),
			success: function(json) 
			{
				showSuccessMessage( i18n_save_success );
			}
		});
}

//--------------------------------------------------------------------------------------------
// Show selected data-recording
//--------------------------------------------------------------------------------------------

function showSelectedDataRecoding( patientId )
{
	showLoader();
	hideById('searchDiv');
	hideById('dataEntryFormDiv');
	hideById('migrationPatientDiv');
	hideById('dataRecordingSelectDiv');
	
	jQuery('#dataRecordingSelectDiv').load( 'selectDataRecording.action', 
		{
			patientId: patientId
		},
		function()
		{
			jQuery('#dataRecordingSelectDiv [id=backBtnFromEntry]').hide();
			showById('dataRecordingSelectDiv');
			
			var programId = jQuery('#programEnrollmentSelectDiv [id=programId] option:selected').val();
			$('#dataRecordingSelectDiv [id=programId]').val( programId );
			$('#dataRecordingSelectDiv [id=inputCriteria]').hide();
			showById('dataRecordingSelectDiv');
			hideLoader();
			hideById('contentDiv');
		});
}

function advancedSearch( params )
{
	$.ajax({
		url: 'searchRegistrationPatient.action',
		type:"POST",
		data: params,
		success: function( html ){
				statusSearching = 1;
				setInnerHTML( 'listPatientDiv', html );
				showById('listPatientDiv');
				setFieldValue('listAll',false);
				jQuery( "#loaderDiv" ).hide();
			}
		});
}

//--------------------------------------------------------------------------------------------
// Migration patient
//--------------------------------------------------------------------------------------------

function getPatientLocation( patientId )
{
	hideById('listPatientDiv');
	hideById('selectDiv');
	hideById('searchDiv');
	setInnerHTML('patientDashboard','');
				
	jQuery('#loaderDiv').show();
	
	jQuery('#migrationPatientDiv').load("getPatientLocation.action", 
		{
			patientId: patientId
		}
		, function(){
			showById( 'migrationPatientDiv' );
			jQuery( "#loaderDiv" ).hide();
		});
}

function registerPatientLocation( patientId )
{
	$.getJSON( 'registerPatientLocation.action',{ patientId:patientId }
		, function( json ) 
		{
			showSuccessMessage( i18n_save_success );
		} );
}
