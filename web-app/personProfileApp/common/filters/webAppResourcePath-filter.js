/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
personProfileApp.filter('webAppResourcePath', ['webAppResourcePathString', function (webAppResourcePathString) {
    return function(input){
        var separator = input.startsWith('/') ? '' : '/';
        return webAppResourcePathString + separator + input;
    };
}]);