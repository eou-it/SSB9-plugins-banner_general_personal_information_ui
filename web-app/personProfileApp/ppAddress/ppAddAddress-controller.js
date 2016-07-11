personProfileAppControllers.controller('ppAddAddressController',['$scope', '$modal', '$modalInstance', 'ppAddressService','$rootScope', '$state', '$stateParams',
    '$filter', '$q', '$timeout', 'notificationCenterService',
    function ($scope, $modal, $modalInstance, ppAddressService, $rootScope, $state, $stateParams, $filter, $q, $timeout, notificationCenterService){
        $scope.address = {
            county: {},
            state: {},
            nation: {},
            addressType:{},
            city: null,
            fromDate: null,
            toDate: null,
            houseNumber: null,
            streetLine1: null,
            streetLine2: null,
            streetLine3: null,
            streetLine4: null,
            zip: null
        };

        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.saveNewAddress = function() {
            if(ppAddressService.isValidAddress($scope.address)) {
                // TODO this can probably be removed when date picker implemented
                $scope.address.fromDate = new Date(Date.parse($scope.address.fromDate));
                $scope.address.toDate = new Date(Date.parse($scope.address.toDate));

                ppAddressService.saveNewAddress($scope.address).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, "error");
                    }
                    else {
                        $scope.cancelModal();
                    }
                });
            }
            else {
                ppAddressService.displayMessages();
            }
        };
    }
]);