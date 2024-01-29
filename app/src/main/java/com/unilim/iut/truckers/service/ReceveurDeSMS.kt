package com.unilim.iut.truckers.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.unilim.iut.truckers.controleur.MotCleControleur
import com.unilim.iut.truckers.controleur.MessageControleur
import com.unilim.iut.truckers.controleur.ListeBlancheControleur
import com.unilim.iut.truckers.controleur.LogcatControleur
import com.unilim.iut.truckers.modele.NumeroTelephone
import com.unilim.iut.truckers.modele.Message

class ReceveurDeSMS : BroadcastReceiver() {

    private val controleurListeBlanche = ListeBlancheControleur()
    private val controleurMessage = MessageControleur()
    private val controleurMotCle = MotCleControleur()
    private val controleurLogcat = LogcatControleur()

    override fun onReceive(contexte: Context?, intention: Intent?) {
        if (intention != null && intention.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intention)

            for (message in messages) {
                Log.d("TruckerService", "--------------------------------------")
                controleurLogcat.ecrireDansFichierLog("--------------------------------------")
                val numeroEmetteur: NumeroTelephone?
                try {
                    numeroEmetteur = message.originatingAddress?.let { NumeroTelephone(it) }
                    val corpsMessage = message.messageBody

                    val listeBlanche = controleurListeBlanche.chargementListeBlanche(contexte, false)
                    val numeroAdmin = controleurListeBlanche.chargementListeBlanche(contexte, true)

                    if (numeroEmetteur != null) {
                        when(numeroEmetteur.numeroTelephone) {
                            in listeBlanche.toString() -> {
                                Log.d("TruckerService", "Message de la liste blanche")
                                controleurLogcat.ecrireDansFichierLog("Message de la liste blanche")

                                if (controleurMotCle.verificationMotsCles(contexte, corpsMessage)) {
                                    controleurMessage.ajoutMessageDansJsonBonMessage(contexte, Message(numeroEmetteur, corpsMessage, message.timestampMillis.toString()))
                                } else {
                                    controleurMessage.ajoutMessageDansMauvaisJsonMessage(contexte, Message(numeroEmetteur, corpsMessage, message.timestampMillis.toString()))
                                }
                            }

                            in numeroAdmin.toString() -> {
                                Log.d("TruckerService", "Message de l'administrateur")
                                controleurLogcat.ecrireDansFichierLog("Message de l'administrateur")

                                if (controleurMotCle.verificationMotsClesAdmin(corpsMessage)) {
                                    controleurMessage.ajoutMessageDansJsonBonMessage(contexte, Message(numeroEmetteur, corpsMessage, message.timestampMillis.toString()))
                                    controleurMessage.actionMessage(contexte, Message(numeroEmetteur, corpsMessage, message.timestampMillis.toString()))
                                } else {
                                    controleurMessage.ajoutMessageDansMauvaisJsonMessage(contexte, Message(numeroEmetteur, corpsMessage, message.timestampMillis.toString()))
                                }
                            }

                            else -> {
                                Log.d("TruckerService", "Message Invalide")
                                controleurLogcat.ecrireDansFichierLog("Message Invalide")
                            }
                        }

                        controleurMessage.avoirMessagesDansBonJsonMessage(contexte)
                        controleurMessage.avoirMessagesDansMauvaisJsonMessage(contexte)
                    }
                } catch (e: Exception) {
                    Log.d("TruckerService", e.message.toString())
                    controleurLogcat.ecrireDansFichierLog(e.message.toString())
                }
            }
        }
    }
}
