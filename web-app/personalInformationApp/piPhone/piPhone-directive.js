/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('personPhone', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piPhone/piPhone.html')
    };
}]);

personalInformationAppDirectives.directive('personPhoneList', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piPhone/piPhoneList.html')
    };
}]);
