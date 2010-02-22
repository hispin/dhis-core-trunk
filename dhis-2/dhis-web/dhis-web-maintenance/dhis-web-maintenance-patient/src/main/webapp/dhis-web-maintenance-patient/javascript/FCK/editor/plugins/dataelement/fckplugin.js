/*
 * Data element selector plugin for FCK editor.
 * Christian Mikalsen <chrismi@ifi.uio.no>
 */

// Register the command.
var associationIdField = window.parent.document.getElementById( 'associationIdField' );
var associationId = associationIdField.value;
var associationNameField = window.parent.document.getElementById( 'associationNameField' );
var associationName = associationNameField.value;
var urlLocation = window.parent.location.href;
var urlParts = new Array();
urlParts = urlLocation.split('viewDataEntryForm.action');
var urlPath = urlParts[0]+'selectDataElement.action?associationId='+associationId+'&associationName='+associationName;

FCKCommands.RegisterCommand( 'InsertDataElement', new FCKDialogCommand( 'Insert data element', 'Data element selector', urlPath, 700, 550 ) ) ;


// Create the "Insert Data element" toolbar button.
var oInsertDataElementItem = new FCKToolbarButton( 'InsertDataElement', FCKLang.PlaceholderBtn ) ;
oInsertDataElementItem.IconPath = FCKPlugins.Items['dataelement'].Path + 'dataElement.gif' ;
FCKToolbarItems.RegisterItem( 'InsertDataElement', oInsertDataElementItem ) ;

// The object used for all operations.
var FCKSelectElement = new Object() ;

// Called by the popup to insert the selected data element.
FCKSelectElement.Add = function( associationId, associationName, dataElementId, dataElementName, dataElementType, dispName, viewByValue, selectedOptionComboIds, selectedOptionComboNames)
{
    viewByValue = "@@"+viewByValue+"@@";
    var strPSDataEntryId   = "value["+ associationId +"].value:value["+ dataElementId +"].value";
    var comboPSDataEntryId = "value["+ associationId +"].combo:value["+ dataElementId +"].combo";
    var boolPSDataEntryId  = "value["+ associationId +"].boolean:value["+ dataElementId +"].boolean";
    var datePSDataEntryId  = "value["+ associationId +"].date:value["+ dataElementId +"].date";

    if(associationName == "programstage")
    {
        var selectString = "";
        if(dataElementType == "string" && (selectedOptionComboNames.indexOf("(default)")== -1 ))
        {
        	selectString = "<select name=\"entryselect\" id=\""+comboPSDataEntryId+"\" > <option value=\"\">i18n_select_value</option>";

            for(k=0; k<selectedOptionComboIds.length; k++)
            {
                //FCK.InsertHtml("<option value=\""+psOptionComboId+"\" >$encoder.htmlEncode(\""+psOptionComboName+"\")</option>");
                selectString += "<option value=\""+selectedOptionComboIds[k]+"\" id=\"combo["+selectedOptionComboIds[k]+"].combo\" >("+selectedOptionComboNames[k]+")</option>";
            }
            selectString += "</select>";

            FCK.InsertHtml(selectString);
        }
        else if (dataElementType == "bool")
        {
            selectString = "<select name=\"entryselect\" id=\""+boolPSDataEntryId+"\" > <option value=\"\">i18n_select_value</option>";
            selectString += "<option value=\"true\" >i18n_yes</option>";
            selectString += "<option value=\"false\" >i18n_no</option>";
            selectString += "</select>";

            FCK.InsertHtml(selectString);
        }
        else if (dataElementType == "date")
        {
            selectString = "<input type=\"text\" id=\""+datePSDataEntryId+"\" name=\"entryfield\" value=\"\" >";
            selectString += "<img src=\"../images/calendar_icon.gif\" width=\"10em\"  id=\"get_"+programStageId+"_"+dataElementId+"\" style=\"cursor: pointer;\" title=\"Select Date\" onmouseover=\"this.style.background=\'orange\';\" onmouseout=\"this.style.background=\'\'\"";
            selectString += "<script type=\"text/javascript\">";
            selectString += "Calendar.setup({";
            selectString += "inputField     :    \""+datePSDataEntryId+"\",";
            selectString += "ifFormat       :    \"yyyy-mm-dd\"," ;
            selectString += "button         :    \"get_"+programStageId+"_"+dataElementId+"\" ";
            selectString += "});";
            selectString += "</script>";

            FCK.InsertHtml(selectString);

        }
        else if ( dataElementType == "int" &&  selectedOptionComboIds.length > 0 )
        {
        	for(k=0; k<selectedOptionComboIds.length; k++)
        	{
        		 var optionComboId = selectedOptionComboIds[k];
                 var optionComboName = selectedOptionComboNames[k];

                 var titleValue = "-- "+dataElementId + ". "+ dataElementName+" "+optionComboId+". "+optionComboName+" ("+dataElementType+") --";
                 var displayName = dispName+" - "+optionComboName+" ]";
                 var dataEntryId = "value[" + associationId + "].value:value[" + dataElementId + "].value:value[" + optionComboId + "].value";
                 FCK.InsertHtml("<input title=\"" + titleValue + "\" view=\""+viewByValue+"\" value=\"" + displayName + "\" name=\"entryfield\" id=\"" + dataEntryId + "\" style=\"width:10em;text-align:center\"/><br/>");
        	}
        }else{
        	strPSDataEntryId  = strPSDataEntryId + ":value["+ selectedOptionComboIds[0] +"].value";
            FCK.InsertHtml("<input name=\"entryfield\" id=\""+strPSDataEntryId+"\" type=\"text\" value=\"\" onkeypress=\"return keyPress(event, this)\" >");
        }
    }
    else if(associationName == "dataset")
    {
        for(k=0; k<selectedOptionComboIds.length; k++)
        {
            var optionComboId = selectedOptionComboIds[k];
            var optionComboName = selectedOptionComboNames[k];

            var titleValue = "-- "+dataElementId + ". "+ dataElementName+" "+optionComboId+". "+optionComboName+" ("+dataElementType+") --";
            var displayName = dispName+" - "+optionComboName+" ]";
            var dataEntryId = "value[" + dataElementId + "].value:value[" + optionComboId + "].value";
            var boolDataEntryId = "value[" + dataElementId + "].value:value[" + optionComboId + "].value";
//            alert("dataElementType = "+dataElementType +" optionComboId = "+optionComboId +" optionComboName= "+optionComboName);
            if (dataElementType == "bool")
            {
                selectString = "<select name=\"entryselect\" id=\""+boolDataEntryId+"\" > <option value=\"\">Select Value</option>";
                selectString += "<option value=\"true\" >$i18n.getString( \"yes\" )</option>";
                selectString += "<option value=\"false\" >$i18n.getString( \"no\" )</option>";
                selectString += "</select>";

                FCK.InsertHtml(selectString);
            }
            else
            {
                //alert("<input title=\"" + titleValue + "\" view=\""+viewByValue+"\" value=\"" + displayName + "\" name=\"entryfield\" id=\"" + dataEntryId + "\" style=\"width:4em;text-align:center\"/>");
                FCK.InsertHtml("<input title=\"" + titleValue + "\" view=\""+viewByValue+"\" value=\"" + displayName + "\" name=\"entryfield\" id=\"" + dataEntryId + "\" style=\"width:4em;text-align:center\"/>");

            }
        }

    }
}
