/* global trackerCapture, angular */

trackerCapture.controller('ApprovalController',
        function ($scope,
                $modalInstance,
                DHIS2EventFactory,
                AshaPortalUtils,
                optionSets,
                dataElementForCurrentApprovalLevelId,
                dataElementForCurrentApprovalStatusId,
                stage,
                event) {    

    console.log('the event:  ', event);
    $scope.approvalForm = {};
    $scope.event = event;
    
    $scope.save = function () {
        //check for form validity 
        if( event.latestApprovalStatus !== 'Approved' && !$scope.event.comment ){
            return false;
        }
            
        var obj = AshaPortalUtils.saveApproval( event, 
                                      stage, 
                                      optionSets, 
                                      dataElementForCurrentApprovalLevelId, 
                                      dataElementForCurrentApprovalStatusId);                
        DHIS2EventFactory.update( obj.model ).then(function(){
            event.currentApprovalLevel = event[dataElementForCurrentApprovalLevelId] = obj.display[dataElementForCurrentApprovalLevelId];
            event[dataElementForCurrentApprovalStatusId] = event.latestApprovalStatus;   
            event.currentApprovalStatus = event.latestApprovalStatus;
            $modalInstance.close(event);
        }, function(){
            event.latestApprovalStatus = null;                
            $modalInstance.close(event);
        });        
    };

    $scope.cancel = function () {
        event.latestApprovalStatus = null;
        $modalInstance.close(event);
    };
});