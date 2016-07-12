/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

modules = {
    /* Override UI Bootstrap 0.10.0 to use version 0.13.3
     * Override AngularUI Router 0.2.10 to use version 0.2.15 */
    overrides {
        'angularApp' {
            resource id:[plugin: 'banner-ui-ss', file: 'js/angular/ui-bootstrap-tpls-0.10.0.min.js'], url: [plugin: 'banner-general-person-profile-ui', file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
            resource id:[plugin: 'banner-ui-ss', file: 'js/angular/angular-ui-router.min.js'], url: [plugin: 'banner-general-person-profile-ui', file: 'js/angular/angular-ui-router.min.js']
        }
    }

    'angular' {
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'js/angular/angular-route.min.js']
    }

    'bootstrapLTR' {
        dependsOn "jquery"
        defaultBundle environment == "development" ? false : "bootstrap"

        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/css/bootstrap.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'css/bootstrap-fixes.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/js/bootstrap.js']
    }

    'bootstrapRTL' {
        dependsOn "jquery"
        defaultBundle environment == "development" ? false : "bootstrap"

        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/css/bootstrap-rtl.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'css/bootstrap-fixes-rtl.css'], attrs: [media: 'screen, projection']
        resource url:[plugin: 'banner-ui-ss', file: 'bootstrap/js/bootstrap.js']
    }

    'personProfileApp' {
        dependsOn "angular,glyphicons"

        defaultBundle environment == "development" ? false : "personProfileApp"

        //Main configuration file
        resource url: [plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/app.js']

        // Services
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/common/services/breadcrumb-service.js']
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/common/services/notificationcenter-service.js']
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/ppAddress/ppAddress-service.js']

        // Controllers
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/ppMain/ppMain-controller.js']
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/ppAddress/ppAddAddress-controller.js']

        // Filters
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/common/filters/i18n-filter.js']
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/common/filters/webAppResourcePath-filter.js']

        // Directives
        resource url:[plugin: 'banner-general-person-profile-ui', file: 'personProfileApp/common/directives/selectBox-directive.js']

    }

    if (System.properties['BANNERXE_APP_NAME'].equals("PersonProfile") || System.properties['BANNERXE_APP_NAME'] == null) {
        'personProfileAppLTR' {
            dependsOn "bannerWebLTR, personProfileApp, bootstrapLTR"

            // CSS
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/main.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/responsive.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/banner-icon-font.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/select2-box.css'], attrs: [media: 'screen, projection']
        }

        'personProfileAppRTL' {
            dependsOn "bannerWebRTL, personProfileApp, bootstrapRTL"

            // CSS
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/main-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/responsive-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/banner-icon-font-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-person-profile-ui', file: 'css/select2-box-rtl.css'], attrs: [media: 'screen, projection']
        }
    }

}
