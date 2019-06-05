/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class PersonalInformationInterceptorSpec extends Specification implements InterceptorUnitTest<PersonalInformationInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test personalInformation interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"personalInformation")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
