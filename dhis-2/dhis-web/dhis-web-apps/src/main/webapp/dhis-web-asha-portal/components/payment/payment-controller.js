/* global trackerCapture, angular, dhis2 */

trackerCapture.controller('PaymentController',
        function($scope,
                $modalInstance,
                payments,
                paymentRate,
                programs,
                programsById,
                stages,
                stagesById,
                orgUnitName,
                ashaDetails,
                ashaEvent,
                ashaPeriod,
                slipType,
                AshaPortalUtils,
                SessionStorageService) {

    var role = SessionStorageService.get('USER_ROLES');    
    $scope.approvingGroup = role['ApprovalGroup'];
    
    $scope.payments = payments;
    $scope.paymentRate = paymentRate;
    $scope.programs = programs;
    $scope.programsById = programsById;
    $scope.stages = stages;
    $scope.stagesById = stagesById;
    $scope.ashaDetails = ashaDetails;
    $scope.ashaEvent = ashaEvent;
    
    var paymentSlip = AshaPortalUtils.getPaymentSlip('VERIFICATION', slipType, payments, paymentRate, programs, programsById, stages, stagesById, orgUnitName, ashaPeriod.name);
    $scope.paymentHeaders = paymentSlip.paymentHeaders;
    $scope.paymentTableHeaders = paymentSlip.paymentTableHeaders;
    $scope.paymentReport = paymentSlip.paymentReport;
    $scope.totalPaymentAmount = paymentSlip.totalPaymentAmount;
    
    $scope.close = function () {
        $modalInstance.close($scope.gridColumns);
    };
    
});