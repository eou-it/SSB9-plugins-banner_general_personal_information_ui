/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import groovy.sql.Sql
import net.hedtech.banner.general.system.SdaCrosswalkConversion
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class PersonalInformationConfigService {

    static final PI_CONFIG = 'piConfig'

    def sessionFactory

    def getParamFromWebTailor (webParam, defaultVal) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def val

        try {
            sql.call("{? = call twbkwbis.f_fetchwtparam (?)}", [Sql.VARCHAR, webParam]) { result -> val = result }
        } catch (e) {
            log.error("Error retrieving value for Web Tailor parameter \"" + webParam + "\". " +
                    "Will attempt to use default value of \"" + defaultVal + "\".", e)
        } finally {
            sql?.close()

            if (!val) {
                log.error("No value found for Web Tailor parameter key \"" + webParam + "\". " +
                        "This should be configured in Web Tailor. Using default value of \"" + defaultVal + "\".")
                val = defaultVal
            }

            return val;
        }
    }

    def setPersonalInfoConfigInSession(session, config) {
        session.setAttribute(PI_CONFIG, config)
    }

    def getPersonalInfoConfigFromSession(session) {
        session.getAttribute(PI_CONFIG)
    }

    def getAddressDisplayPriorities(session) {
        def piConfig = getPersonalInfoConfigFromSession(session)

        if (piConfig) {
            if (piConfig.addressDisplayPriorities) {
                return piConfig.addressDisplayPriorities
            }
        } else {
            piConfig = [:]
        }

        def priorities = [:]
        def addrList = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup( 'PINFOADDR', 'ADDRESS' )

        addrList.each {it ->
            priorities[it.external] = it.internalSequenceNumber
        }

        piConfig.addressDisplayPriorities = priorities
        setPersonalInfoConfigInSession(session, piConfig)

        priorities
    }

}