package net.hedtech.banner.general

import net.hedtech.banner.exceptions.ApplicationException
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.security.core.context.SecurityContextHolder

class ControllerUtility {

    static def log = Logger.getLogger('net.hedtech.banner.general.ControllerUtility')

    def static getPrincipalPidm() {
        try {
            return SecurityContextHolder?.context?.authentication?.principal?.pidm
        } catch (MissingPropertyException it) {
            log.error("principal lacks a pidm - may be unauthenticated or session expired. Principal: ${SecurityContextHolder?.context?.authentication?.principal}")
            log.error(it)
            throw it
        }
    }

    public static  returnFailureMessage(ApplicationException e) {
        def model = [:]

        model.failure = true
        log.error(e)

        try {
            def extractError = e.returnMap({ mapToLocalize -> new ValidationTagLib().message(mapToLocalize) })
            model.message = extractError.message + (extractError.errors ? " " + extractError.errors : "")

            if(e.type == 'SQLException'){
                // don't expose the oracle error numbers in SQL exceptions
                model.message = model.message.replaceAll("(ORA)-[0-9]+: ","")
            }

            return model
        }
        catch (ApplicationException ex) {
            log.error(ex)
            model.message = e.message
            return model
        }
    }

}
