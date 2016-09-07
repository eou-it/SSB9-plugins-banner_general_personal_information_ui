/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

modules = {
    /* Override UI Bootstrap 0.10.0 to use version 0.13.3
     * Override AngularUI Router 0.2.10 to use version 0.2.15 */
    overrides {
        'angularApp' {
            resource id:[plugin: 'banner-ui-ss', file: 'js/angular/ui-bootstrap-tpls-0.10.0.min.js'], url: [plugin: 'banner-general-personal-information-ui', file: 'js/angular/ui-bootstrap-tpls-0.13.3.min.js']
            resource id:[plugin: 'banner-ui-ss', file: 'js/angular/angular-ui-router.min.js'], url: [plugin: 'banner-general-personal-information-ui', file: 'js/angular/angular-ui-router.min.js']
        }
    }

    'angular' {
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'js/angular/angular-route.min.js']
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

    'commonComponents' {
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'js/xe-components/xe-ui-components.js']
    }
    'commonComponentsLTR' {
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'css/xe-components/xe-ui-components.css']
    }
    'commonComponentsRTL' {
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'css/xe-components/xe-ui-components-rtl.css']
    }

    'personalInformationApp' {
        dependsOn "angular,glyphicons,commonComponents"

        defaultBundle environment == "development" ? false : "personalInformationApp"

        //Main configuration file
        resource url: [plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/app.js']

        // Services
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/services/breadcrumb-service.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/services/notificationcenter-service.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/services/piCrud-service.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piPhone/piPhone-service.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piAddress/piAddress-service.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piEmail/piEmail-service.js']

        // Controllers
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piMain/piMain-controller.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piAddress/piEditAddress-controller.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piEmail/piEditEmail-controller.js']

        // Filters
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/filters/i18n-filter.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/filters/webAppResourcePath-filter.js']

        // Directives
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/directives/selectBox-directive.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piPhone/piPhone-directive.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/piAddress/piAddress-directive.js']
        resource url:[plugin: 'banner-general-personal-information-ui', file: 'personalInformationApp/common/directives/personalInfo-directive.js']

    }

    if (System.properties['BANNERXE_APP_NAME'].equals("PersonalInformation") || System.properties['BANNERXE_APP_NAME'] == null) {
        'personalInformationAppLTR' {
            dependsOn "bannerWebLTR, personalInformationApp, bootstrapLTR, commonComponentsLTR"

            // CSS
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/main.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/responsive.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/banner-icon-font.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/select2-box.css'], attrs: [media: 'screen, projection']
        }

        'personalInformationAppRTL' {
            dependsOn "bannerWebRTL, personalInformationApp, bootstrapRTL. commonComponentsRTL"

            // CSS
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/main-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/responsive-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/banner-icon-font-rtl.css'], attrs: [media: 'screen, projection']
            resource url: [plugin: 'banner-general-personal-information-ui', file: 'css/select2-box-rtl.css'], attrs: [media: 'screen, projection']
        }
    }

}
