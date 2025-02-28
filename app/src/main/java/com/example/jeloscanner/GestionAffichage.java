package com.example.jeloscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Map;

public class GestionAffichage
{
    private Handler notifHandler;
    private Context context;
    private Vibrator vibration;
    // Bleu clair

    public GestionAffichage(Context contextPass){
        context = contextPass;
        notifHandler = new Handler(Looper.getMainLooper());
        vibration = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
    }

    //**************************************************
    // Affiche l'ensemble des produits pour une machine donnee
    //**************************************************
    public void AfficheLesProduits()
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                int largeurNomProduit = 1500;
                int largeurNombreProduit =100;
                LinearLayout.LayoutParams parametreAffichage;
                GradientDrawable bord;
                try {
                    //Affiche le nom de la machine
                    MainActivity.nomMachine.setText(MainActivity.gestionDonnees.listeMachine.get(MainActivity.gestionDonnees.idMachineEnCours));
                    NomMachineChangeCouleur('D');
                    ((LinearLayout) MainActivity.fenetrePrincipale).removeAllViews();
                    // Pour chaque produit present
                    for (Map.Entry<String, ArrayList<Integer>> chaqueProduit : MainActivity.gestionDonnees.donneeMachine.entrySet()) {
                        //Creation d'une ligne
                        LinearLayout conteneurProduit = new LinearLayout(context);
                        parametreAffichage = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        parametreAffichage.setMargins(0, 3, 0, 0);
                        conteneurProduit.setLayoutParams(parametreAffichage);
                        conteneurProduit.setOrientation(LinearLayout.HORIZONTAL);
                        // Definition du bord noir interieur en blanc
                        bord = new GradientDrawable();
                        bord.setColor(Color.WHITE/*MainActivity.gestionDonnees.produitDebutCouleur*/);
                        bord.setStroke(2, Color.BLACK);
                        conteneurProduit.setBackground(bord);
                        // Rajout de la ligne a la vue
                        ((LinearLayout) MainActivity.fenetrePrincipale).addView(conteneurProduit);
                        // creer la view du nom du produit
                        TextView affiNomProduit = new TextView(context);
                        parametreAffichage = new LinearLayout.LayoutParams(largeurNomProduit, LinearLayout.LayoutParams.WRAP_CONTENT);
                        //parametreAffichage.setMargins(0, 1, 2, 0);
                        affiNomProduit.setLayoutParams(parametreAffichage);
                        affiNomProduit.setText((String) MainActivity.gestionDonnees.listeProduit.get(chaqueProduit.getKey()));
                        conteneurProduit.addView(affiNomProduit);
                        // Creer la view de la quantite voulue
                        TextView affiQtyVoulue = new TextView(context);
                        parametreAffichage = new LinearLayout.LayoutParams(largeurNombreProduit, LinearLayout.LayoutParams.WRAP_CONTENT);
                        //parametreAffichage.setMargins(0, 1, 2, 0);
                        affiQtyVoulue.setLayoutParams(parametreAffichage);
                        affiQtyVoulue.setText(String.valueOf(chaqueProduit.getValue().get(0)));
                        affiQtyVoulue.setTextSize(15);
                        conteneurProduit.addView(affiQtyVoulue);
                        // Cree la view de la quantite faite
                        TextView affiQtyFaite = new TextView(context);
                        parametreAffichage = new LinearLayout.LayoutParams(largeurNombreProduit, LinearLayout.LayoutParams.WRAP_CONTENT);
                        //parametreAffichage.setMargins(0, 1, 2, 0);
                        affiQtyFaite.setLayoutParams(parametreAffichage);
                        affiQtyFaite.setText(String.valueOf(chaqueProduit.getValue().get(1)));
                        affiQtyFaite.setTextSize(15);
                        conteneurProduit.addView(affiQtyFaite);
                        // Sauvegarde les view pour ce produit
                        MainActivity.gestionDonnees.SauveViewProduit(chaqueProduit.getKey(), bord, affiQtyFaite, MainActivity.gestionDonnees.produitDebutCouleur);
                    }
                }
                catch (Exception e) {
                    MainActivity.gestionFichier.EcritLogJelo("Erreur durant l'affichage de la liste produit: " + e.getMessage());
                }
            }
        });
    }
    //**************************************************

    //**************************************************
    // Reset les couleurs des lignes
    //**************************************************
    public void RemetCouleurLignePardefaut()
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                GradientDrawable bordInter;

                for(Map.Entry<String, ArrayList> chaqueLigne : MainActivity.gestionDonnees.listeViewProduit.entrySet()) {
                    try{
                        bordInter = (GradientDrawable)chaqueLigne.getValue().get(0);
                        if((char)chaqueLigne.getValue().get(2) != 'F') {
                            bordInter.setColor(MainActivity.gestionDonnees.produitDebutCouleur);
                        }
                        else {
                            bordInter.setColor(MainActivity.gestionDonnees.produitFinieCouleur);
                        }
                    }
                    catch (Exception e) {
                        MainActivity.gestionFichier.EcritLogJelo("Erreur durant le reset des couleurs des lignes: " + e.getMessage());
                    }
                }
            }
        });
    }
    //**************************************************

    //**************************************************
    // Change la couleur d'un produit
    //**************************************************
    public void ChangeLigneCouleur(final String idProduitPass, final int couleur)
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                GradientDrawable bordInter;

                try{
                    bordInter = (GradientDrawable)MainActivity.gestionDonnees.listeViewProduit.get(idProduitPass).get(0);
                    bordInter.setColor(couleur);
                    if(couleur == MainActivity.gestionDonnees.produitFinieCouleur)
                        MainActivity.gestionDonnees.listeViewProduit.get(idProduitPass).set(2,'F');
                }
                catch (Exception e) {
                    MainActivity.gestionFichier.EcritLogJelo("Erreur durant le changement de couleur du produit " + idProduitPass + ": " + e.getMessage());
                }
            }
        });
    }
    //**************************************************

    //**************************************************
    // Change la couleur d'un produit
    //**************************************************
    public void NomMachineChangeCouleur(char couleur)
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                GradientDrawable bordInter;

                try{
                    if(couleur == 'D') {
                        MainActivity.nomMachine.setBackground(context.getDrawable(R.drawable.ic_bordure_nom_machine_debut));
                    }
                    else {
                        MainActivity.nomMachine.setBackground(context.getDrawable(R.drawable.ic_bordure_nom_machine_finie));
                    }
                }
                catch (Exception e) {
                    MainActivity.gestionFichier.EcritLogJelo("Erreur durant le changement de couleur du nom de la machine: " + e.getMessage());
                }
            }
        });
    }
    //**************************************************

    //**************************************************
    // Change l'affichage de la quantite de barquette chargées pour un produit
    //**************************************************
    public void ChangeQuantiteProduit(final String idProduitPass)
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView texteInter;

                try {
                    texteInter = (TextView)MainActivity.gestionDonnees.listeViewProduit.get(idProduitPass).get(1);
                    texteInter.setText(String.valueOf(MainActivity.gestionDonnees.donneeMachine.get(idProduitPass).get(1)));
                }
                catch (Exception e) {
                    MainActivity.gestionFichier.EcritLogJelo("Erreur durant l'ecriture de la quantite chargée du produit " + idProduitPass + ": " + e.getMessage());
                }
            }
        });
    }

    //**************************************************
    // Affiche alerte en cas de probleme
    //**************************************************
    public void AfficheAlerte(final String messageAffi)
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibration.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)); }
                    MainActivity.alerte.setText(messageAffi);
                    MainActivity.alerte.setVisibility(TextView.VISIBLE);
                }
                catch (Exception e) {
                    MainActivity.gestionFichier.EcritLogJelo("Erreur durant l'affichage d'une l'alerte: " + e.getMessage());
                }
            }
        });
    }
    //**************************************************

    //**************************************************
    // Efface le message d'alerte
    //**************************************************
    public void EffaceAlerte()
    {
        notifHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    MainActivity.alerte.setVisibility(TextView.INVISIBLE);
                }
                catch (Exception e) {
                    MainActivity.gestionFichier.EcritLogJelo("Erreur durant l'effacement d'une alerte': " + e.getMessage());
                }
            }
        });
    }

    //**************************************************
    // Propose le choix d annulation de chargement
    //**************************************************
    public void DialogAnnulationChargement(final String nouvelleIdMachine) {
        notifHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder questionFinChargement;
                questionFinChargement = new AlertDialog.Builder(context);
                questionFinChargement.setTitle("Erreur de nom machine");
                questionFinChargement.setMessage("Le chargement de la machine courante n'est pas terminé\nCe chargement est il terminé ?");
                questionFinChargement.setCancelable(false);
                questionFinChargement.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.gestionFichier.EcritLogJelo("Le chargement de la machine id = " + MainActivity.gestionDonnees.idMachineEnCours + " n'a pas ete terminé");
                        MainActivity.gestionDonnees.ArretChargementmachine();
                        MainActivity.gestionDonnees.DemarrageChargementmachine(nouvelleIdMachine);
                        AfficheLesProduits();
                        dialog.cancel();
                    }
                });
                questionFinChargement.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                questionFinChargement.show();
            }
        });
    }
}
