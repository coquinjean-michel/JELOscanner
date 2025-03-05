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
        MainActivity.gestionFichier.EcritLogJelo("*** DEMARRAGE PROGRAMME ***");
        DemarrageBluetooth();
    }

    protected void onPause(Bundle savedInstanceState) {
        if(MainActivity.gestionDonnees.listeMachine.size() > MainActivity.gestionDonnees.listeMachineFinis.size())
            MainActivity.gestionFichier.EcritRepriseJelo('C');
        else
            MainActivity.gestionFichier.EcritRepriseJelo('F');
        stopService(intentPerso);
    }

    public void FermeLeProgramme() {
        if(MainActivity.gestionDonnees.listeMachine.size() > MainActivity.gestionDonnees.listeMachineFinis.size())
            MainActivity.gestionFichier.EcritRepriseJelo('C');
        else
            MainActivity.gestionFichier.EcritRepriseJelo('F');
        MainActivity.gestionFichier.EcritLogJelo("*** FERMETURE DU PROGRAMME ***");
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
            apparenceDialogue.setMessage("Voulez vous quitter le programme ?");
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
                    TelechargementFichierGlobal(false);
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
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                DialogueErreurCritique("Probleme de permission", "Veuillez autoriser l'utilisation du bluetooth !");
                MainActivity.gestionFichier.EcritLogJelo("Le bluetooth n'est pas autorisé!");
            }
            else
                DemarrageBluetooth();
        }
        catch (Exception e) {
            DialogueErreurCritique("Probleme de permission", "Le systeme de permission est en defaut ! " + e.getMessage());
            MainActivity.gestionFichier.EcritLogJelo("Le systeme de permission est en defaut " + e.getMessage());
        }
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
            TelechargementFichierGlobal(true);
        }
        else {
            // Si le fichier global avec la date d'aujourd'hui n'existe pas, demander si on veut le charger
            if(gestionFichier.VerifieGlobalChargeAujour() == false) {
                MainActivity.gestionFichier.EcritLogJelo("Aucun telechargement du fichier global aujourd'hui");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibreur.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                else {
                    vibreur.vibrate(500);
                }
                apparenceDialogue = new AlertDialog.Builder(this);
                apparenceDialogue.setTitle("Chargement fichier global");
                apparenceDialogue.setMessage("Voulez vous charger le nouveau fichier ?");
                apparenceDialogue.setCancelable(false);
                apparenceDialogue.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TelechargementFichierGlobal(true);
                        dialog.cancel();
                    }
                });
                apparenceDialogue.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LectureFichierGlobal(true);
                        dialog.cancel();
                    }
                });
                apparenceDialogue.show();
            }
            else {
                LectureFichierGlobal(true);
            }
        }
    }
    //**************************************************

    //**************************************************
    // Telechargement du fichier global du serveur vers document
    //**************************************************
    private void TelechargementFichierGlobal(Boolean demarreService)
    {
        // Telecharge le fichier ocerall.csv du serveur jelo et met a
        MainActivity.gestionFichier.EcritLogJelo("Telechargement du fichier global");
        MainActivity.gestionFichier.TelechargeFichierGlobal();
        if(MainActivity.gestionFichier.erreurChargementFichier == true)
            DialogueErreurCritique("Impossible de telecharger le fichier global: ", gestionFichier.erreur);
        else {
            if(gestionFichier.EcritDateFichier() == true) {
                DialogueErreurCritique("Probleme de fichier", gestionFichier.erreur);
            }
            else {
                MainActivity.gestionAffichage.RemetEcranZero();
                MainActivity.gestionDonnees.RemetZeroDonnees();
                MainActivity.gestionFichier.EcritRepriseJelo('F');
                LectureFichierGlobal(demarreService);
            }
        }
    }
    //**************************************************

    //**************************************************
    // Verifie que le fichier global a ete charge aujourd'hui
    // Sinon, demande l'autorisation de la charger
    //**************************************************
    private void LectureFichierGlobal(Boolean demarreService)
    {
        Boolean pb = false;

        MainActivity.gestionFichier.EcritLogJelo("Lecture du fichier global");
        pb = gestionFichier.LitFichierGlobal("Ocerall.csv");
        if(pb == true) {
            DialogueErreurCritique("Probleme de fichier", gestionFichier.erreur);
        }
        else {
            if(demarreService == true)
                RechargementReprise();
        }
    }
    //**************************************************

    //**************************************************
    // Rechargement des donnees de reprise
    //**************************************************
    private void RechargementReprise()
    {
        if(MainActivity.gestionFichier.LitRepriseJelo() == true) {
            if(MainActivity.gestionDonnees.chargementEnCours == true) {
                MainActivity.gestionAffichage.AfficheLesProduits();
                MainActivity.gestionAffichage.RemetVertLigneFinis();
            }
        }
        DemarrageService();
    }
    //**************************************************

    //**************************************************
    // Telechargement et chargement des en cours
    //**************************************************
    private void DemarrageService()
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
        else {
            vibreur.vibrate(500);
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