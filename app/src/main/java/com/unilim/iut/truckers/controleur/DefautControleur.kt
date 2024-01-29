package com.unilim.iut.truckers.controleur

import android.content.Context
import android.os.Environment
import com.unilim.iut.truckers.modele.NumeroTelephone
import java.io.File

private const val DEFAULT_CONFIG_FILE = "defaut.json"

class DefautControleur {

    private val controleurJson = ChargeurDeStockageExterne()

    /**
     * Cette fonction permet de vérifier si un fichier JSON existe.
     *
     * @return Cette fonction retourne un booléen.
     */
    fun verificationDefaultJson(): Boolean {
        val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fichier = File(downloadsPath, DEFAULT_CONFIG_FILE)

        return fichier.exists()
    }

    /**
     * Cette fonction permet de créer un fichier JSON contenant une liste de String mot-clés.
     *
     * @param contexte Ce paramètre est le contexte de l'application.
     * @return Cette fonction ne retourne rien.
     */
    fun chargementJson(contexte: Context?): String {
        return controleurJson.charger(contexte, DEFAULT_CONFIG_FILE).toString()
    }

    /**
     * Cette fonction permet de charger une liste de String mot-clés.
     *
     * @param contexte Ce paramètre est le contexte de l'application.
     * @return Cette fonction retourne une liste de String mot-clés.
     */
    fun chargementMotsClesDefaut(contexte: Context?): MutableList<String> {
        val objetJson = controleurJson.charger(contexte, DEFAULT_CONFIG_FILE)
        val liste = mutableListOf<String>()

        val tableauJSON = objetJson.getJSONArray("mots_cles")

        for (i in 0 until tableauJSON.length()) {
            val motCle = tableauJSON.getString(i)
            liste.add(motCle.toString())
        }

        return liste
    }

    /**
     * Cette fonction permet de charger une liste de String mot-clés.
     *
     * @param contexte Ce paramètre est le contexte de l'application.
     * @return Cette fonction retourne une liste de String mot-clés.
     */
    fun chargementListeBlancheDefaut(contexte: Context?, nomTableauJson: String): MutableList<NumeroTelephone> {
        val objetJson = controleurJson.charger(contexte, DEFAULT_CONFIG_FILE)
        val liste = mutableListOf<NumeroTelephone>()

        val tableauJson = objetJson.getJSONArray(nomTableauJson)

        for (i in 0 until tableauJson.length()) {
            val numero = tableauJson.getString(i)
            liste.add(NumeroTelephone(numero.toString()))
        }

        return liste
    }

}