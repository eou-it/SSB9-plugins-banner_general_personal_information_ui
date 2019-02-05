/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
var personalInformationAppControllers = angular.module('personalInformationAppControllers', []);
var personalInformationAppDirectives = angular.module('personalInformationAppDirectives', []);


var personalInformationApp = angular.module('personalInformationApp', ['ngResource','ui.router','personalInformationAppControllers',
    'personalInformationAppDirectives','ui.bootstrap','I18n','datePickerApp', 'xe-ui-components', 'ui.select'])
    .run(
    ['$rootScope', '$state', '$stateParams', '$filter', 'breadcrumbService', 'notificationCenterService',
        function ($rootScope, $state, $stateParams, $filter, breadcrumbService, notificationCenterService) {
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;
            $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                $state.previous = fromState;
                $state.previousParams = fromParams;
                breadcrumbService.setBreadcrumbs(toState.data.breadcrumbs);
                breadcrumbService.refreshBreadcrumbs();
            });
            $rootScope.$on('$viewContentLoaded', function () {
            });
            $(document.body).removeAttr("role"); // TODO: is this needed?
            $("html").attr("dir", $filter('i18n')('default.language.direction') === 'ltr' ? "ltr" : "rtl");
            $rootScope.notificationErrorType = "error";
            $rootScope.notificationSuccessType = "success";
            $rootScope.notificationWarningType = "warning";
            $rootScope.flashNotification = true;
            //IE fix
            if (!window.location.origin) {
                window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
            }

            $rootScope.isMobileView = function() { return isMobile(); };
            $rootScope.isRTL = $('meta[name=dir]').attr("content") === 'rtl';

            // Above, we use the isDesktop function implemented in the banner_ui_ss plugin, which thus far has
            // proven to be satisfactory.  Below we modify the implementation of isTablet from banner_ui_ss to
            // be consistent with the definition of "is tablet" elsewhere in this app.
            var isTablet = window.matchMedia("only screen and (min-width: 769px) and (max-width:1024px)");
            $rootScope.isTabletView = isTablet.matches;

            $rootScope.playAudibleMessage = null;

            $rootScope.applicationContextRoot = $('meta[name=applicationContextRoot]').attr("content");
        }
    ]
);

personalInformationApp.constant('webAppResourcePathString', '../plugins/banner-general-personal-information-ui-0.1');

personalInformationApp.config(['$stateProvider', '$urlRouterProvider', 'webAppResourcePathString',
    function($stateProvider, $urlRouterProvider, webAppResourcePathString){
        // For any unmatched url, send to /personalInformationMain
        var url = url ? url : 'personalInformationMain';

        $urlRouterProvider.otherwise(url) ;

        /***********************************************************
         * Defining all the different states of the personalInformationApp
         * Landing pages
         ***********************************************************/
        $stateProvider
            .state('personalInformationMain', {
                url: "/personalInformationMain",
                templateUrl: webAppResourcePathString + '/personalInformationApp/piMain/personalInformationMain.html',
                controller: 'piMainController',
                resolve: {
                    piConfigResolve: function (piCrudService) {
                        return piCrudService.get('PiConfig').$promise;
                    }
                },
                data: {
                    breadcrumbs: []
                },
                params: {
                    onLoadNotifications: [],
                    startingTab: ''
                }
            })
            .state('piFullViewMobile', {
                url: "/personalInformationMobileFullProfile",
                templateUrl: webAppResourcePathString + '/personalInformationApp/piMain/piMobileFullView.html',
                controller: 'piMainController',
                resolve: {
                    piConfigResolve: function (piCrudService) {
                        return piCrudService.get('PiConfig').$promise;
                    }
                },
                data: {
                    breadcrumbs: []
                },
                params: {
                    onLoadNotifications: [],
                    startingTab: ''
                }
            });
    }
]);

personalInformationApp.config(['$locationProvider',
    function ($locationProvider) {
        $locationProvider.html5Mode(false);
    }
]);

personalInformationApp.config(['$httpProvider',
    function ($httpProvider) {
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }
        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
        $httpProvider.defaults.cache = false;
        $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';
        $httpProvider.interceptors.push(function ($q, $window,$rootScope) {
            $rootScope.ActiveAjaxConectionsWithouthNotifications = 0;
            var checker = function(parameters,status){
                //YOU CAN USE parameters.url TO IGNORE SOME URL
                if(status === "request"){
                    $rootScope.ActiveAjaxConectionsWithouthNotifications+=1;
                    $('.body-overlay').addClass('loading');
                    $("#content").attr('aria-busy', true);
                }
                if(status === "response"){
                    $rootScope.ActiveAjaxConectionsWithouthNotifications-=1;

                }
                if($rootScope.ActiveAjaxConectionsWithouthNotifications<=0){
                    $rootScope.ActiveAjaxConectionsWithouthNotifications=0;
                    $('.body-overlay').removeClass('loading');
                    $("#content").attr('aria-busy', false);
                }
            };
            return {
                'request': function(config) {
                    checker(config,"request");
                    return config;
                },
                'requestError': function(rejection) {
                    checker(rejection.config,"request");
                    return $q.reject(rejection);
                },
                'response': function(response) {
                    checker(response.config,"response");
                    return response;
                },
                'responseError': function(rejection) {
                    checker(rejection.config,"response");
                    if (rejection.status === 403) {
                        window.location.href = '/login/denied';
                    }
                    return $q.reject(rejection);
                }
            };
        });
    }
]);

/*
 * Disables AngularJS debug data for performance reasons.
 * To re-enable, enter this in browser debug console:
 *
 *     angular.reloadWithDebugInfo();
 *
 * For more info, see https://docs.angularjs.org/guide/production
 */
personalInformationApp.config(['$compileProvider', function ($compileProvider) {
    $compileProvider.debugInfoEnabled(false);
}]);
