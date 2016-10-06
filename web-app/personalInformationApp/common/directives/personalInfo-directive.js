/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationAppDirectives.directive('setStartingTab', [function () {
    return {
        restrict: 'A',
        link: {
            pre: function(scope, ele, attr) {
                if(attr.setStartingTab === scope.startingTab){
                    attr.active = true;
                }
            }
        }
    };
}]);

personalInformationAppDirectives.directive('piMobileFooterButton', ['$filter', function ($filter) {
    return {
        restrict: 'E',
        scope: {
            clickFunction: '='
        },
        transclude: true,
        templateUrl: $filter('webAppResourcePath')('personalInformationApp/piMain/piMobileFooterButton.html')
    };
}]);

personalInformationAppDirectives.directive('enterKey', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.enterKey);
                });

                event.preventDefault();
            }
        });
    };
});
