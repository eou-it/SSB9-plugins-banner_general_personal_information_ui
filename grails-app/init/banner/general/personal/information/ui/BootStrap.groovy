/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.general.personal.information.ui

import net.hedtech.banner.converters.json.JSONBeanMarshaller
import net.hedtech.banner.converters.json.JSONDomainMarshaller
import net.hedtech.banner.i18n.LocalizeUtil
import grails.converters.JSON

class BootStrap {
    def dateConverterService

    def init = { servletContext ->
        registerJSONMarshallers()
    }
    def destroy = {
    }

    //Used by integration tests for parsing dates when rendering as JSON.
    private def registerJSONMarshallers() {
        JSON.registerObjectMarshaller(Date) {
           dateConverterService.parseGregorianToDefaultCalendar(LocalizeUtil.formatDate(it))
        }

        def localizeMap = [
                'attendanceHour': LocalizeUtil.formatNumber,
        ]

        JSON.registerObjectMarshaller(new JSONBeanMarshaller( localizeMap ), 1) // for decorators and maps
        JSON.registerObjectMarshaller(new JSONDomainMarshaller( localizeMap, true), 2) // for domain objects
    }
}
