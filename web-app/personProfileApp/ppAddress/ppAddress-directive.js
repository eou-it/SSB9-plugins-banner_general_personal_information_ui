/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personProfileAppDirectives.directive('personAddress', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personProfileApp/ppAddress/ppViewAddress.html')
    };
}]);

personProfileAppDirectives.directive('personAddressList', ['$filter', function ($filter) {
    return{
        restrict: 'E',
        templateUrl: $filter('webAppResourcePath')('personProfileApp/ppAddress/ppAddressList.html')
    };
}]);
