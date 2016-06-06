/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

class PersonProfileController {
    static defaultAction = 'landingPage'

    def landingPage() {
        render model: [:], view: "personProfile"
    }

}