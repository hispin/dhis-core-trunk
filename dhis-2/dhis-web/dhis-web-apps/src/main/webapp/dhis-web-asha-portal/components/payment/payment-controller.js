/* global trackerCapture, angular */

trackerCapture.controller('PaymentController',
        function($scope,
                $filter,
                $modalInstance,
                $translate,
                payments,
                activityPrograms,
                activityProgramsById,
                stages,
                stagesById,
                orgUnitName,
                ashaDetails,
                ashaEvent,
                ashaPeriod,
                slipType) {

    $scope.payments = payments;
    $scope.activityPrograms = activityPrograms;
    $scope.activityProgramsById = activityProgramsById;
    $scope.stages = stages;
    $scope.stagesById = stagesById;
    $scope.ashaDetails = ashaDetails;
    $scope.ashaEvent = ashaEvent;
    
    $scope.paymentHeaders = [];
    $scope.paymentHeaders.push({name: $translate('org_unit'), value: orgUnitName});
    $scope.paymentHeaders.push({name: $translate('period'), value: ashaPeriod.name});
    
    $scope.paymentTableHeaders = [];

    $scope.paymentTableHeaders.push({id: 'activity', value: $translate('activity')});
    $scope.paymentTableHeaders.push({id: 'rate', value: $translate('rate')});
    $scope.paymentTableHeaders.push({id: 'claimed', value: $translate('claimed')});
    $scope.paymentTableHeaders.push({id: 'pending', value: $translate('pending')});
    $scope.paymentTableHeaders.push({id: 'rejected', value: $translate('rejected')});
    $scope.paymentTableHeaders.push({id: 'approved', value: $translate('approved')});
    $scope.paymentTableHeaders.push({id: 'sanctioned', value: $translate('sanctioned')});
   
    $scope.paymentReport = []; 
    if(slipType === 'ACTIVITY'){
        
        angular.forEach($scope.activityPrograms, function(program){
            $scope.paymentReport[program.id] = {hasData: false, rate: 'rate', claimed: 0, pending: 0, rejected: 0, approved: 0, sanctioned: 0};
            angular.forEach($filter('filter')($scope.payments, {program: program.id}), function(activity){
                var obj = $scope.paymentReport[activity.program];
                $scope.paymentReport[program.id] = {hasData: true, 
                                                        program: $scope.activityProgramsById[activity.program].name,
                                                        activity: $scope.activityProgramsById[activity.program].name,
                                                        rate: obj.rate, 
                                                        claimed: obj.claimed + 1, 
                                                        pending: (activity.currentApprovalStatus !== 'Approved' || activity.currentApprovalStatus !== 'Rejected') ? obj.pending + 1: obj.pending,
                                                        rejected: (activity.currentApprovalStatus === 'Rejected') ? obj.rejected + 1 : obj.rejected,
                                                        approved: (activity.currentApprovalStatus === 'Approved') ? obj.approved + 1 : obj.approved,
                                                        sanctioned: ''
                                                    };                
            });
        });
    }
    else if (slipType === 'SERVICE'){
        
    }    
    
    
    $scope.close = function () {
        $modalInstance.close($scope.gridColumns);
    };    
});