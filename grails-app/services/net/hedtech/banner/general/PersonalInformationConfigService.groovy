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

    // TODO: example values primarily for testing, may need to be changed based on BXEGS-297
    static final def ADDRESS_VIEW = 'bwgkogad.P_SelectAtypView'
    static final def ADDRESS_UPDATE = 'bwgkogad.P_SelectAtypUpdate'

    def hasAccessToWebTailorMenu(menuName, roles, pidm) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        String rolesString = '\''+ roles.join('\',\'') +'\''
        menuName = "'%${menuName}%'"
        def sqlQuery = "select DISTINCT twgrmenu_url_text "+
          "from twgrmenu a "+
         "where twgrmenu_enabled = 'Y' "+
           "and (twgrmenu_name in "+
                    "(select twgrwmrl_name from twgrwmrl, twgrrole where twgrrole_pidm = ${pidm} "+
                        "and twgrrole_role = twgrwmrl_role and twgrwmrl_name = a.twgrmenu_name) "+
                "or twgrmenu_name in "+
                    "(select twgrwmrl_name from twgrwmrl, govrole where govrole_pidm = ${pidm} "+
                        "and twgrwmrl_role in  (${rolesString}))) "+
           "and twgrmenu_name like ${menuName}"
        def result

        try {
            // A non-empty result means the user can view the menu
            result = sql.firstRow(sqlQuery) as boolean
        } catch (e) {
            log.error("Error retrieving value for Web Tailor menu \"" + menuName + "\".", e)
        } finally {
            sql?.close()

            return result
        }
    }

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

    def getPersonalInfoSectionConfig(roles, pidm) {
        def model = [:]

        model.address = [
                isVisible: hasAccessToWebTailorMenu(ADDRESS_VIEW, roles, pidm),
                isUpdateable: hasAccessToWebTailorMenu(ADDRESS_UPDATE, roles, pidm)
        ]

        return model
    }
}