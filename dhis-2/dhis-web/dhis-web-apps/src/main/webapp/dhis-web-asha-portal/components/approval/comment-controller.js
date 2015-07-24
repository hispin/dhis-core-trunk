/* global trackerCapture, angular */

trackerCapture.controller('CommentController', 
    function($scope, 
            $modalInstance, 
            dhis2Event){
    
    $scope.dhis2Event = dhis2Event;
    
    $scope.close = function () {
        $modalInstance.close();
    };      
});