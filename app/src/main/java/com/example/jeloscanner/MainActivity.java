package com.example.jeloscanner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity
{
    // Variable globales
    public static GestionFichier gestionFichier;
    public static GestionDonnees gestionDonnees;
    public static GestionAffichage gestionAffichage;
    public static GestionBlueTooth gestionBlueTooth;
    public static View fenetrePrincipale;
    public static TextView alerte;
    public static TextView nomMachine;
    // Variable locales
    private AlertDialog.Builder apparenceDialogue;
    private Intent intentPerso;

    //############################################
    // FONCTION ANDROID
    //############################################
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fenetrePrincipale =  findViewById(R.id.listeProduit);
        alerte =  findViewById(R.id.Alerte);
        nomMachine = findViewById(R.id.machineChargement);

        // declaration de toutes les classe
        gestionFichier = new GestionFichier();
        gestionDonnees = new GestionDonnees();
        gestionAffichage = new GestionAffichage(this);
        gestionBlueTooth = new GestionBlueTooth();
        // Modif pour etre compatible avec l API de la montre
        //TestAutorisation();
        DemarrageBluetooth();
    }

    protected void onPause(Bundle savedInstanceState)
    {
        stopService(intentPerso);
    }

    protected void onResume(Bundle savedInstanceState)
    {
        TestAutorisation();
    }

    public void FermeLeProgramme()
    {
        stopService(intentPerso);
        finish();
    }
    //############################################

    //############################################
    // FONCTION PERSO
    //############################################
    //**************************************************
    // Test les autorisation et ferme le programme si manquant
    //**************************************************
    private void TestAutorisation()
    {
        Boolean autorisationBluetooth = false;

        MainActivity.gestionFichier.EcritLogJelo("demmarage du programme");
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                DialogueErreurCritique("Probleme de permission", "Veuillez autoriser l'utilisation du bluetooth !");
                MainActivity.gestionFichier.EcritLogJelo("Probleme de permission de bluetooth");
            }
            else
                autorisationBluetooth = true;
        }
        catch (Exception e) {
            DialogueErreurCritique("Probleme de permission", "Le systeme de permission est en defaut ! " + e.getMessage());
            MainActivity.gestionFichier.EcritLogJelo("Le systeme de permission est en defaut " + e.getMessage());
        }
        if(autorisationBluetooth == true)
            DemarrageBluetooth();
    }
    //**************************************************

    //**************************************************
    // Demarre le bluetooth
    //**************************************************
    private void DemarrageBluetooth()
    {
        if(gestionBlueTooth.InitialisationBluetooth() == false) {
            VerificationChargementFichierGlobal();
        }
        else {
            MainActivity.gestionFichier.EcritLogJelo("Demarrage scanner eteint: " + gestionBlueTooth.erreur);
            DialogueErreurCritique("Probleme d'initialisation du bluetooth", "Allumez le scanner et connectez le a la smart watch !");
        }
    }
    //**************************************************

    //**************************************************
    // Verifie que le fichier global a ete charge aujourd'hui
    // Sinon, demande l'autorisation de la charger
    //**************************************************
    private void VerificationChargementFichierGlobal()
    {
        if(gestionFichier.VerifieGlobalExiste() == false) {
            TelechargementFichierGlobal();
        }
        else {
            // Si le fichier global avec la date d'aujourd'hui n'existe pas, demander si on veut le charger
            if(gestionFichier.VerifieGlobalChargeAujour() == false) {
                MainActivity.gestionFichier.EcritLogJelo("Aucun telechargement du fichier global aujourd'hui");
                Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                apparenceDialogue = new AlertDialog.Builder(this);
                apparenceDialogue.setCancelable(false);
                apparenceDialogue.setTitle("Chargement fichier global");
                apparenceDialogue.setMessage("Le fichier global n'est pas a jour\nVoulez vous charger le nouveau fichier ?");
                apparenceDialogue.setPositiveButton("Oui", dialogueReponse);
                apparenceDialogue.setNegativeButton("Non", dialogueReponse);
                apparenceDialogue.show();
            }
            else {
                LectureFichierGlobal();
            }
        }
    }
    DialogInterface.OnClickListener dialogueReponse = (dialog, which) -> {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                TelechargementFichierGlobal();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                LectureFichierGlobal();
                break;
        }
    };
    //**************************************************

    //**************************************************
    // Telechargement du fichier global du serveur vers document
    //**************************************************
    private void TelechargementFichierGlobal()
    {
        Boolean pb = false;

        // Telecharge le fichier ocerall.csv du serveur jelo et met a
        // CHARGE LE FICHIER GLOBAL DU RESEAU A FAIRE
        MainActivity.gestionFichier.EcritLogJelo("Telechargement du fichier global");
        pb = gestionFichier.EcritDateFichier();
        if(pb == true) {
            DialogueErreurCritique("Probleme de fichier", gestionFichier.erreur);
            MainActivity.gestionFichier.EcritLogJelo("Probleme de telechargement du fichier global " + gestionFichier.erreur);
        }
        else {
            LectureFichierGlobal();
        }
    }
    //**************************************************

    //**************************************************
    // Verifie que le fichier global a ete charge aujourd'hui
    // Sinon, demande l'autorisation de la charger
    //**************************************************
    private void LectureFichierGlobal()
    {
        Boolean pb = false;

        MainActivity.gestionFichier.EcritLogJelo("Lecture du fichier global");
        pb = gestionFichier.LitFichierGlobal("Ocerall.csv");
        if(pb == true) {
            MainActivity.gestionFichier.EcritLogJelo("Probleme de lecture du fichier global " + gestionFichier.erreur);
            DialogueErreurCritique("Probleme de fichier", gestionFichier.erreur);
        }
        else {
            ChargeAfficheEnCours();
        }
    }
    //**************************************************

    //**************************************************
    // Telechargement et chargement des en cours
    //**************************************************
    private void ChargeAfficheEnCours()
    {
        // CHARGEMENT D'UN ENCOURS SI EXISTE
        MainActivity.gestionFichier.EcritLogJelo("Demarrage du chargement");
        intentPerso = new Intent(this, ServicePrincipal.class);
        startService(intentPerso);
    }
    //**************************************************

    //**************************************************
    // Affichage du message erreur critique
    //**************************************************
    private void DialogueErreurCritique(String titre, String message)
    {
        Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        apparenceDialogue = new AlertDialog.Builder(this);
        apparenceDialogue.setTitle(titre);
        apparenceDialogue.setMessage(message);
        apparenceDialogue.setCancelable(false);
        apparenceDialogue.setNegativeButton("OK", (dialog, which) -> {
            dialog.cancel();
            FermeLeProgramme();
        });
        apparenceDialogue.show();
    }
    //############################################
}