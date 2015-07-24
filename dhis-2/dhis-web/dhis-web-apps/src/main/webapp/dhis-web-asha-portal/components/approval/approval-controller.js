/* global trackerCapture, angular */

trackerCapture.controller('ApprovalController',
        function ($scope,
                $modalInstance,
                DHIS2EventFactory,
                AshaPortalUtils,
                DateUtils,
                SessionStorageService,
                optionSets,
                dataElementForCurrentApprovalLevelId,
                dataElementForCurrentApprovalStatusId,
                stage,
                event) {
                    
    $scope.today = DateUtils.getToday();        
    var userProfile = SessionStorageService.get('USER_PROFILE');
    var storedBy = userProfile && userProfile.username ? userProfile.username : '';                
    $scope.form = {};
    $scope.event = event;
    
    $scope.save = function () {
        //check for form validity 
        $scope.form.approvalForm.submitted = true;        
        if( $scope.form.approvalForm.$invalid ){
            return false;
        }
            
        var obj = AshaPortalUtils.saveApproval( event, 
                                      stage, 
                                      optionSets, 
                                      dataElementForCurrentApprovalLevelId, 
                                      dataElementForCurrentApprovalStatusId);        
        if($scope.event.comment){
            obj.model.notes = [{value: $scope.event.comment, storedDate: $scope.today, storedBy: storedBy}];
        }
        
        DHIS2EventFactory.update( obj.model ).then(function(){
            event.currentApprovalLevel = event[dataElementForCurrentApprovalLevelId] = obj.display[dataElementForCurrentApprovalLevelId];
            event[dataElementForCurrentApprovalStatusId] = event.latestApprovalStatus;   
            event.currentApprovalStatus = event.latestApprovalStatus;
            event.notes.splice(0,0,{value: $scope.event.comment, storedDate: $scope.today, storedBy: storedBy});
            $modalInstance.close(event);
        }, function(){
            event.latestApprovalStatus = null;                
            $modalInstance.close(event);
        });
        
        $scope.event = {};
    };

    $scope.cancel = function () {
        event.latestApprovalStatus = null;
        $modalInstance.close(event);
    };
});