/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import groovy.sql.Sql
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class PersonalInformationConfigService {

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

}