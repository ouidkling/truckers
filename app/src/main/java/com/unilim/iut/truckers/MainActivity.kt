package com.unilim.iut.truckers
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.unilim.iut.truckers.commande.AjoutNumeroAdminCommande
import com.unilim.iut.truckers.commande.AjoutMotCleCommande
import com.unilim.iut.truckers.commande.AjoutNumeroListeBlancheCommande
import com.unilim.iut.truckers.commande.HistoriqueDeCommande
import com.unilim.iut.truckers.controleur.CommandeControleur
import com.unilim.iut.truckers.controleur.DefautControleur
import com.unilim.iut.truckers.controleur.MotCleControleur
import com.unilim.iut.truckers.controleur.MessageControleur
import com.unilim.iut.truckers.controleur.ListeBlancheControleur
import com.unilim.iut.truckers.service.ServiceDuReceveurDeSMS
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    companion object {
        val historique: HistoriqueDeCommande = HistoriqueDeCommande()
    }

    private val controlleurCommande = CommandeControleur()
    private val controlleurListeBlanche = ListeBlancheControleur()
    private val controlleurMessage = MessageControleur()
    private val controlleurMotCles = MotCleControleur()
    private val controlleurDefaut = DefautControleur()
    private val SMS_RECEIVE_PERMISSION_CODE = 123
    private val SMS_READ_PERMISSION_CODE = 124

    override fun onCreate(instanceEtatSauvegardee: Bundle?) {
        super.onCreate(instanceEtatSauvegardee)

        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                requestSMSPermissions()
            }
        } else {
            requestSMSPermissions()
        }
    }

    private fun requestSMSPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECEIVE_SMS), SMS_RECEIVE_PERMISSION_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS), SMS_READ_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_RECEIVE_PERMISSION_CODE || requestCode == SMS_READ_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "SMS permission accordée")
                if (areAllPermissionsGranted()) {
                    enqueueWorkManagerJob()
                }
            } else {
                Log.d("Permissions", "SMS permission refusée")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            if (areAllPermissionsGranted()) {
                enqueueWorkManagerJob()
            } else {
                requestSMSPermissions()
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && areAllPermissionsGranted()) {
            enqueueWorkManagerJob()
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun enqueueWorkManagerJob() {

        controlleurListeBlanche.creationListeBlanche(this)
        controlleurMessage.creationJsonMauvaisMessage(this)
        controlleurMessage.creationJsonBonMessage(this)
        controlleurMotCles.creationJSONMotCle(this)
        applicationContext.getSharedPreferences("sms_service_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_receiver_registered", false)
            .apply()

        if (controlleurListeBlanche.chargementListeBlanche(this, true).isNotEmpty() && controlleurMotCles.chargementMotsCles(this).isNotEmpty()) {
            Log.d("TruckerService", "Liste blanche et mots clés déjà présents")
            val managerDeTravail = WorkManager.getInstance(this)
            val smsWorkerRequest = PeriodicWorkRequest.Builder(ServiceDuReceveurDeSMS::class.java, 15, TimeUnit.MINUTES)
                .build()

            managerDeTravail.enqueue(smsWorkerRequest)
        } else if (controlleurDefaut.verificationDefaultJson()) {
            Log.d("TruckerService", "Liste blanche et mots clés par défaut")
            val listeMotsCles = controlleurDefaut.chargementMotsClesDefaut(this)
            val listeBlanche = controlleurDefaut.chargementListeBlancheDefaut(this, "liste_blanche")
            val listeNumeroAdmin = controlleurDefaut.chargementListeBlancheDefaut(this, "numero_admin")
            for (numero in listeBlanche) {
                controlleurCommande.executerCommande(AjoutNumeroListeBlancheCommande(this, numero))
            }
            for (numeroAdmin in listeNumeroAdmin) {
                controlleurCommande.executerCommande(AjoutNumeroAdminCommande(this, numeroAdmin))
            }
            for (motcle in listeMotsCles) {
                controlleurCommande.executerCommande(AjoutMotCleCommande(this, motcle))
            }
            val managerDeTravail = WorkManager.getInstance(this)
            val requeteDuTravailleurDeSms = PeriodicWorkRequest.Builder(ServiceDuReceveurDeSMS::class.java, 15, TimeUnit.MINUTES)
                .build()
            managerDeTravail.enqueue(requeteDuTravailleurDeSms)
        } else {
            Log.d("TruckerService", "Le fichier JSON par défaut n'existe pas")
        }

        finish()
    }
}