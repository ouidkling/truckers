package com.unilim.iut.truckers.exception

class WriteWhiteListException : Exception() {
    override val message: String
        get() = "Erreur lors de l'écriture du fichier JSON WhiteList : "
}