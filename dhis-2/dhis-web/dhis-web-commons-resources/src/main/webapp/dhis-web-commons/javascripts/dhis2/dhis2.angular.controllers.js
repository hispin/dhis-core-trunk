'use strict';

/* Controllers */
var d2Controllers = angular.module('d2Controllers', [])

//Controller for column show/hide
.controller('ColumnDisplayController', 
    function($scope, 
            $modalInstance, 
            hiddenGridColumns,
            gridColumns){
    
    $scope.gridColumns = gridColumns;
    $scope.hiddenGridColumns = hiddenGridColumns;
    
    $scope.close = function () {
      $modalInstance.close($scope.gridColumns);
    };
    
    $scope.showHideColumns = function(gridColumn){
       
        if(gridColumn.show){                
            $scope.hiddenGridColumns--;            
        }
        else{
            $scope.hiddenGridColumns++;            
        }      
    };    
})

//controller for dealing with google map
.controller('MapController',
        function($scope, 
                $modalInstance,
                CurrentSelection,
                DHIS2URL,
                DialogService,
                location) {
    
    $scope.home = function(){        
        window.location = DHIS2URL;
    };
    
    $scope.location = location;
    
    $scope.close = function () {
        $modalInstance.close();
    };
    
    $scope.captureCoordinate = function(){
    	if( $scope.location && $scope.location.lng && $scope.location.lat ){
    		$scope.location = CurrentSelection.getLocation();
            $modalInstance.close($scope.location);
    	}
    	else{
    		//notify user
            var dialogOptions = {
                headerText: 'error',
                bodyText: 'nothing_captured'
            };
            DialogService.showDialog({}, dialogOptions);
            return;
    	}
    };
})

//Controller for audit history
.controller('AuditHistoryController', function ($scope, $modalInstance, $modal, AuditHistoryDataService, DateUtils, eventId, dataType, nameIdMap) {

    $scope.itemList = [];

    $scope.model = {type: dataType};

    $scope.close = function () {
        $modalInstance.close();
    };
    
    $scope.auditColumns = ['name', 'auditType', 'value', 'modifiedBy', 'created'];    

    AuditHistoryDataService.getAuditHistoryData(eventId, dataType).then(function (data) {

        $scope.itemList = [];
        $scope.uniqueRows = [];

        var reponseData = data.trackedEntityDataValueAudits ? data.trackedEntityDataValueAudits :
            data.trackedEntityAttributeValueAudits ? data.trackedEntityAttributeValueAudits : null;

        if (reponseData) {
            for (var index = 0; index < reponseData.length; index++) {
                var dataValue = reponseData[index];
                /*The true/false values are displayed as Yes/No*/
                if (dataValue.value === "true") {
                    dataValue.value = "Yes";
                } else if (dataValue.value === "false") {
                    dataValue.value = "No";
                }
                
                var obj = {};                
                obj.auditType = dataValue.auditType;                
                obj.value = dataValue.value;
                obj.modifiedBy = dataValue.modifiedBy;
                obj.created = DateUtils.formatToHrsMinsSecs(dataValue.created);
                
                if (dataType === "attribute") {
                    if (nameIdMap[dataValue.trackedEntityAttribute.id] && nameIdMap[dataValue.trackedEntityAttribute.id].displayFormName) {                        
                        obj.name = nameIdMap[dataValue.trackedEntityAttribute.id].displayFormName;
                    }
                } else if (dataType === "dataElement") {
                    if (nameIdMap[dataValue.dataElement.id] && nameIdMap[dataValue.dataElement.id].dataElement) {                        
                        obj.name = nameIdMap[dataValue.dataElement.id].dataElement.displayFormName;
                    }
                }                
                $scope.itemList.push(obj);
                
                if( $scope.uniqueRows.indexOf(obj.name) === -1){
                    $scope.uniqueRows.push(obj.name);
                }
            }
            
            if($scope.uniqueRows.length > 0){
                $scope.uniqueRows = $scope.uniqueRows.sort();
            }
        }
    });
})
.controller('ExportController', function($scope, $modalInstance) {

    $scope.export = function (format) {
        $modalInstance.close(format);
    };

    $scope.close = function() {
        $modalInstance.close();
    }
});