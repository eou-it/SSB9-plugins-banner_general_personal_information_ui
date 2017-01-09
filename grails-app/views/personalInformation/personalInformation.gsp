%{--*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************--}%
<!DOCTYPE html>
<!--[if IE 9 ]>    <html xmlns:ng="http://angularjs.org" ng-app="personalInformationApp" id="ng-app" class="ie9"> <![endif]-->
<html xmlns:ng="http://angularjs.org" ng-app="personalInformationApp" id="ng-app">
<head>
    <script type="text/javascript">
        var superUser=${session['SUPER_USER_INDICATOR'] ?: 'undefined'};
        var proxyUser=${session['PROXY_USER_INDICATOR'] ?: 'undefined'};
        var proxyUserName = '${session.PROXY_USER_NAME ?: 'undefined'}';
        var adminFlag=${session['adminFlag'] ?: 'undefined'};
        var employeeFlag=${session['EmployeeFlag'] ?: 'undefined'};
        var proxyFlag=${session['proxyFlag'] ?: 'undefined'};
        var originatorFlag =${session['originatorFlag'] ?: 'undefined'};
        var approverFlag =${session['approverFlag'] ?: 'undefined'};
        var url = '${url}'

    </script>
    <g:applyLayout name="bannerSelfServicePage">
        <meta name="locale" content="${request.locale.toLanguageTag()}" >
        <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu"/>
        <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
        <meta charset="${message(code: 'default.character.encoding')}">

        <g:if test="${message(code: 'default.language.direction')  == 'rtl'}">
            <r:require modules="personalInformationAppRTL"/>
        </g:if>
        <g:else>
            <r:require modules="personalInformationAppLTR"/>
        </g:else>

    </g:applyLayout>

    <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0, user-scalable=no, user-scalable=0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10" />

    <script type="text/javascript">
        <g:i18n_setup/>
    </script>
</head>

<body>

<div class="body-overlay"></div>
<svg>
    <defs>
        <clipPath id="profilePicClip">
            <circle cx="66" cy="80" r="66"/>
        </clipPath>
    </defs>
</svg>
<div id="content" class="container-fluid" aria-relevant="additions" role="main">
    <div ui-view></div>
</div>
<script  type="text/javascript">
    function tellAngular() {
        var domElt = document.getElementsByClassName('page-header');
        scope = angular.element(domElt).scope();
        scope.$apply(function() {
            if(window.innerWidth > 768)
            {
                scope.searchView = false;
            }
        });
    }
    window.onresize = tellAngular;
</script>
</body>
</html>
