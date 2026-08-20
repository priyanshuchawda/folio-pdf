package com.pulse.pdf

import com.pulse.pdf.pdf.PdfDocumentSession

/**
 * Process-wide holder so Activities can share / trim the active PDF session.
 */
object PdfSession {
    @Volatile
    var active: PdfDocumentSession? = null
        private set

    fun attach(session: PdfDocumentSession) {
        active?.close()
        active = session
    }

    fun detach(session: PdfDocumentSession) {
        if (active === session) {
            active = null
        }
        session.close()
    }

    fun trimCaches() {
        active?.trimMemory()
    }
}
