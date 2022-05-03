package com.bm2cy.dainty2.internal

import com.bm2cy.dainty2.component.overview.views.Overview

@FunctionalInterface
interface RecentListInterface {
    fun execute(layout: Overview)
}