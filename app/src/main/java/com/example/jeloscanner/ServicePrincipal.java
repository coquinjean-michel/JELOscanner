package com.example.jeloscanner;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

public class ServicePrincipal extends IntentService
{
    private Boolean execution = true;

    public ServicePrincipal() {
        super("ServicePrincipal");
    }

    //############################################
    // FONCTION SERVICE
    //############################################
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Execution();
    }

    @Override
    public void onDestroy()
    {
        execution = false;
        super.onDestroy();
    }
    //############################################

    //############################################
    // FONCTION PERSO
    //############################################
    private void Execution()
    {
        char resultatAnalyseDonneeScannee;
        char resultatAnalyseIdMachine;
        char resultatAnalyseIdProduit;
        Boolean pbScanner = false;

        //Log.e("lesmiens", "salut salut");
        while (execution == true) {
            if(MainActivity.gestionBlueTooth.ReceptionDonnee() == true) {
                if(pbScanner == false) {
                    MainActivity.gestionAffichage.AfficheAlerte("Le scanner doit etre eteint !");
                    pbScanner = true;
                }
                else {
                    MainActivity.gestionBlueTooth.InitialisationBluetooth();
                }
            }
            else {
                pbScanner = false;
                resultatAnalyseDonneeScannee = MainActivity.gestionDonnees.AnalyseDonneeScannee(MainActivity.gestionBlueTooth.donneePartielleRecue);
                // R si la donnee n'est pas compatible
                // M si c'est un numero de machine (num machine dans idMachine)
                // P si c'est une produit (num d'ordre et id produit dans idProduit, numDordre
                // C si une reception est en cours
                switch(resultatAnalyseDonneeScannee) {
                    case 'R':
                        // affichage "scan incompatible + vibrateur"
                        MainActivity.gestionFichier.EcritLogJelo("Format de donnée scannée incompatible: " + MainActivity.gestionDonnees.donneeBruteRecu);
                        MainActivity.gestionAffichage.AfficheAlerte("Format de donnée scannée incompatible !");
                        break;
                    case 'M':
                        MainActivity.gestionAffichage.EffaceAlerte();
                        resultatAnalyseIdMachine = MainActivity.gestionDonnees.AnalyseIdMachine(MainActivity.gestionDonnees.idMachine);
                        // R rien de special est signalé
                        // E si un chargement est en cours
                        // F la machine a deja ete chargée
                        // P cette machine n'apparait pas dans le fichier global
                        // V les donnees concernant cette machine sont vides
                        // N un nouveau chargement a commencé
                        switch (resultatAnalyseIdMachine) {
                            case 'E':
                                MainActivity.gestionFichier.EcritLogJelo("Machine id = " + MainActivity.gestionDonnees.idMachine + " scannée durant un chargement en cours");
                                MainActivity.gestionAffichage.DialogAnnulationChargement(MainActivity.gestionDonnees.idMachine);
                                break;
                            case 'F':
                                MainActivity.gestionFichier.EcritLogJelo("Machine id = " + MainActivity.gestionDonnees.idMachine + " a déjà ete chargée");
                                MainActivity.gestionAffichage.AfficheAlerte("Cette machine a déjà ete chargée !");
                                break;
                            case 'P':
                                MainActivity.gestionFichier.EcritLogJelo("Machine id = " + MainActivity.gestionDonnees.idMachine + " n'apparait pas dans le fichier global");
                                MainActivity.gestionAffichage.AfficheAlerte("Cette machine n'apparait pas dans le fichier global !");
                                break;
                            case 'V':
                                MainActivity.gestionFichier.EcritLogJelo("Machine id = " + MainActivity.gestionDonnees.idMachine + " ne contient aucun produit");
                                MainActivity.gestionAffichage.AfficheAlerte("Cette machine ne contient aucun produit !");
                                break;
                            case 'N':
                                MainActivity.gestionFichier.EcritLogJelo("Demarrage du chargement de la machine id = " + MainActivity.gestionDonnees.idMachine);
                                MainActivity.gestionDonnees.DemarrageChargementmachine(MainActivity.gestionDonnees.idMachine);
                                MainActivity.gestionAffichage.AfficheLesProduits();
                                break;
                        }
                        break;
                    case 'P':
                        MainActivity.gestionAffichage.EffaceAlerte();
                        MainActivity.gestionAffichage.RemetCouleurLignePardefaut();
                        resultatAnalyseIdProduit = MainActivity.gestionDonnees.AnalyseIdProduit(MainActivity.gestionDonnees.idProduit, MainActivity.gestionDonnees.numDordre);
                        // R rien de special est signalé
                        // E aucun chargement est en cours
                        // F le produit n'est pas dans la liste correspondant a cette machine
                        // P ce produit a deja ete chargé
                        // V quantité maximum atteinte pour ce produit dans cette machine
                        // N un nouveau produit a ete rajouté mais ce produit n'est pas entierement fini
                        // L un nouveau produit a ete rajouté et est entierement chargé
                        // G le chargement de la machine est terminé
                        switch (resultatAnalyseIdProduit) {
                            case 'E':
                                MainActivity.gestionFichier.EcritLogJelo("Aucun chargement en cours pendant le scan du produit: " + MainActivity.gestionDonnees.idProduit);
                                MainActivity.gestionAffichage.AfficheAlerte("Aucun chargement en cours \nScannez un identifiant machine pour commencer !");
                                break;
                            case 'F':
                                MainActivity.gestionFichier.EcritLogJelo("Le produit: " + MainActivity.gestionDonnees.idProduit + " n'apparait pas dans la liste des produits pour cette machine");
                                MainActivity.gestionAffichage.AfficheAlerte("Ce produit n'apparait pas dans la \nliste de chargement de cette machine !");
                                break;
                            case 'P':
                                MainActivity.gestionFichier.EcritLogJelo("Le produit: " + MainActivity.gestionDonnees.idProduit + " numero: " + MainActivity.gestionDonnees.numDordre + " a déjà été chargé");
                                MainActivity.gestionAffichage.ChangeLigneCouleur(MainActivity.gestionDonnees.idProduit, MainActivity.gestionDonnees.produitProblemeCouleur);
                                MainActivity.gestionAffichage.AfficheAlerte("Ce produit a déjà été chargé !");
                                break;
                            case 'V':
                                MainActivity.gestionFichier.EcritLogJelo("La quantité maximum du produit: " + MainActivity.gestionDonnees.idProduit + "pour cette machine est déjà atteint");
                                MainActivity.gestionAffichage.ChangeLigneCouleur(MainActivity.gestionDonnees.idProduit, MainActivity.gestionDonnees.produitProblemeCouleur);
                                MainActivity.gestionAffichage.AfficheAlerte("La quantité maximum de ce produit pour cette machine est déjà atteint !");
                                break;
                            case 'N':
                                MainActivity.gestionFichier.EcritLogJelo("Le produit: " + MainActivity.gestionDonnees.idProduit + "a ete chargé");
                                MainActivity.gestionAffichage.ChangeQuantiteProduit(MainActivity.gestionDonnees.idProduit);
                                MainActivity.gestionAffichage.ChangeLigneCouleur(MainActivity.gestionDonnees.idProduit, MainActivity.gestionDonnees.produitCourantCouleur);
                                break;
                            case 'L':
                                MainActivity.gestionFichier.EcritLogJelo("Le produit: " + MainActivity.gestionDonnees.idProduit + "a ete chargé et, est complet pour cette machine");
                                MainActivity.gestionAffichage.ChangeQuantiteProduit(MainActivity.gestionDonnees.idProduit);
                                MainActivity.gestionAffichage.ChangeLigneCouleur(MainActivity.gestionDonnees.idProduit, MainActivity.gestionDonnees.produitFinieCouleur);
                                break;
                            case 'G':
                                MainActivity.gestionFichier.EcritLogJelo("Le produit: " + MainActivity.gestionDonnees.idProduit + "a ete chargé et le chargement de la machine: " + MainActivity.gestionDonnees.idMachineEnCours + "est fini");
                                MainActivity.gestionDonnees.ArretChargementmachine();
                                MainActivity.gestionAffichage.ChangeQuantiteProduit(MainActivity.gestionDonnees.idProduit);
                                MainActivity.gestionAffichage.ChangeLigneCouleur(MainActivity.gestionDonnees.idProduit, MainActivity.gestionDonnees.produitFinieCouleur);
                                MainActivity.gestionAffichage.NomMachineChangeCouleur('F');
                                break;
                        }
                        break;
                }
            }
        }
    }
}