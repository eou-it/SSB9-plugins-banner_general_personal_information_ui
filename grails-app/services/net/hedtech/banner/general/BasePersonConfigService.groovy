/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import grails.gorm.transactions.Transactional
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.person.PersonUtility
import org.springframework.transaction.annotation.Propagation
//import org.springframework.transaction.annotation.Transactional

//TODO: previously used Spring Transactional (see import above). Does it cause problem switching to Grails 3 Transactional?  JDC 4/19
@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
abstract class BasePersonConfigService {
    
    protected abstract String getCacheName();

    protected abstract String getProcessCode();

    protected abstract List getExcludedProperties();

    def getParamFromSession(param, defaultVal) {
        def personalInfoConfig = getPersonConfigFromSession(getCacheName(), getProcessCode(), getExcludedProperties())

        def paramVal = personalInfoConfig[param]

        if (!paramVal) {
            log.error("No value found for integration configuration setting \"" + param + "\". " +
                    "This should be configured in GORICCR. Using default value of \"" + defaultVal + "\".")

            paramVal = defaultVal
        }

        paramVal
    }

    private static getPersonConfigFromSession(cacheName, processCode, excludedProperties) {
        def personConfigInSession = PersonUtility.getPersonConfigFromSession()

        if (personConfigInSession) {
            if (!personConfigInSession.containsKey(cacheName)) {
                createConfig(personConfigInSession, cacheName, processCode, excludedProperties)
            }
        } else {
            createConfig(personConfigInSession, cacheName, processCode, excludedProperties)
            PersonUtility.setPersonConfigInSession(personConfigInSession)
        }

        personConfigInSession[cacheName]
    }

    private static createConfig(personConfigInSession, cacheName, processCode, excludedProperties) {
        def configFromGoriccr = IntegrationConfiguration.fetchAllByProcessCode(processCode)
        def config = [:]

        configFromGoriccr.each {it ->
            if (!excludedProperties.contains(it.settingName)) {
                config[it.settingName] = it.value
            }
        }

        personConfigInSession[cacheName] = config

        personConfigInSession
    }
}