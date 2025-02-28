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
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
    private Button boutonFermer;
    private Button boutonFichier;
    private Vibrator vibreur;

    //############################################
    // FONCTION ANDROID
    //############################################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fenetrePrincipale = findViewById(R.id.listeProduit);
        alerte = findViewById(R.id.Alerte);
        nomMachine = findViewById(R.id.machineChargement);
        boutonFermer = findViewById(R.id.boutonFerme);
        boutonFermer.setOnClickListener(this);
        boutonFichier = findViewById(R.id.boutonFichier);
        boutonFichier.setOnClickListener(this);
        vibreur = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        // declaration de toutes les classe
        gestionFichier = new GestionFichier();
        gestionDonnees = new GestionDonnees();
        gestionAffichage = new GestionAffichage(this);
        gestionBlueTooth = new GestionBlueTooth();
        // Modif pour etre compatible avec l API de la montre
        //TestAutorisation();
        DemarrageBluetooth();
    }

    protected void onPause(Bundle savedInstanceState) {
        stopService(intentPerso);
    }

    protected void onResume(Bundle savedInstanceState) {
        TestAutorisation();
    }

    public void FermeLeProgramme() {
        MainActivity.gestionFichier.EcritLogJelo("Fermeture du programme");
        stopService(intentPerso);
        finish();
    }

    @Override
    public void onClick(View v) {
        int but;

        but = v.getId();
        // Confirme la fermeture du programme
        if(but == R.id.boutonFerme) {
            apparenceDialogue = new AlertDialog.Builder(this);
            apparenceDialogue.setTitle("FERMETURE PROGRAMME");
            apparenceDialogue.setMessage("Voulez quitter le programme ?");
            apparenceDialogue.setCancelable(false);
            apparenceDialogue.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    FermeLeProgramme();
                    dialog.cancel();
                }
            });
            apparenceDialogue.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            apparenceDialogue.show();
        }
        if(but == R.id.boutonFichier) {
            apparenceDialogue = new AlertDialog.Builder(this);
            apparenceDialogue.setTitle("CHARGEMENT FICHIER");
            apparenceDialogue.setMessage("Voulez ecraser le fichier existant ?");
            apparenceDialogue.setCancelable(false);
            apparenceDialogue.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    TelechargementFichierGlobal();
                    dialog.cancel();
                }
            });
            apparenceDialogue.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            apparenceDialogue.show();
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibreur.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                apparenceDialogue = new AlertDialog.Builder(this);
                apparenceDialogue.setTitle("Chargement fichier global");
                apparenceDialogue.setMessage("Voulez vous charger le nouveau fichier ?");
                apparenceDialogue.setCancelable(false);
                apparenceDialogue.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TelechargementFichierGlobal();
                        dialog.cancel();
                    }
                });
                apparenceDialogue.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LectureFichierGlobal();
                        dialog.cancel();
                    }
                });
            }
            else {
                LectureFichierGlobal();
            }
        }
    }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibreur.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
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