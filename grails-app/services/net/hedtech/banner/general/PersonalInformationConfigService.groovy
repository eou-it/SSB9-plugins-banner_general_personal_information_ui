/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.SdaCrosswalkConversion
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class PersonalInformationConfigService {

    static final String PERSONAL_INFO_CONFIG_CACHE_NAME = 'generalPersonalInfoConfig'

    static final String EMAIL_MODE = 'EMAIL_MODE'
    static final String PHONE_MODE = 'PHONE_MODE'
    static final String ADDR_MODE = 'ADDR_MODE'

    static final Integer SECTION_HIDDEN = '0'
    static final Integer SECTION_READONLY = '1'
    static final Integer SECTION_UPDATEABLE = '2'

    def sessionFactory

    def getParamFromSession(param, defaultVal) {
        def personalInfoConfig = getPersonalInfoConfigFromSession()

        def paramVal = personalInfoConfig[param]

        if (!paramVal) {
            log.error("No value found for external code \"" + param + "\". " +
                    "This should be configured in GTVSDAX. Using default value of \"" + defaultVal + "\".")

            paramVal = defaultVal
        }

        paramVal
    }

    def getPersonalInfoConfigFromSession() {
        def personConfigInSession = PersonUtility.getPersonConfigFromSession()
        def personalInfoConfig

        if (personConfigInSession) {
            if (!personConfigInSession.containsKey(PERSONAL_INFO_CONFIG_CACHE_NAME)) {
                createPersonalInfoConfig(personConfigInSession)
            }
        } else {
            personConfigInSession = [:]
            createPersonalInfoConfig(personConfigInSession)
            PersonUtility.setPersonConfigInSession(personConfigInSession)
        }

        personConfigInSession[PERSONAL_INFO_CONFIG_CACHE_NAME]
    }

    def createPersonalInfoConfig(personConfigInSession) {
        def configFromGtvsdax = SdaCrosswalkConversion.fetchAllByInternalGroup('PERSONAL_INFORMATION')
        def config = [:]

        configFromGtvsdax.each {it ->
            config[it.internal] = it.external
        }

        personConfigInSession[PERSONAL_INFO_CONFIG_CACHE_NAME] = config

        personConfigInSession
    }

}