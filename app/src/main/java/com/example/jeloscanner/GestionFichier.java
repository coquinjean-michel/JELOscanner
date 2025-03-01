package com.example.jeloscanner;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GestionFichier
{
    // Contient le message d'erreur a transmettre
    public String erreur;
    public Boolean erreurChargementFichier = false;
    private SimpleDateFormat dateCouranteFormat;
    private SimpleDateFormat heureCouranteFormat;

    GestionFichier()
    {
    }

    //**************************************************
    // Retourne la date en cours au format dd-MM-YYYY
    //**************************************************
    private String RetourneDateCourante()
    {
        dateCouranteFormat = new SimpleDateFormat("dd-MM-yyyy");
        return(dateCouranteFormat.format(new Date()));
    }
    //**************************************************

    //**************************************************
    // Retourne l'heure en cours au format HH:mm:ss
    //**************************************************
    private String RetourneHeureCourante()
    {
        heureCouranteFormat = new SimpleDateFormat("HH:mm:ss");
        return(heureCouranteFormat.format(new Date()));
    }
    //**************************************************

    //**************************************************
    //Lecture et chargement du fichier Global
    // retourne :
    // true = si il y a un probleme
    // false = Si aucun probleme
    //**************************************************
    public boolean LitFichierGlobal(String nomFichierGlobal)
    {
        File fichier;
        boolean pb = false;
        FileInputStream fileInputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String ligne;
        File chemin;
        String machineNom;
        String machineId;
        String produitNom;
        String produitId;
        int quantiteVoulue;
        ArrayList infoProduit;
        ArrayList<ArrayList> listeProduitMachine;
        int valeurCourante;
        Boolean produitTrouv;

        // Declaration du fichier
        chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        MainActivity.gestionDonnees.listeMachine.clear();
        MainActivity.gestionDonnees.listeProduit.clear();
        MainActivity.gestionDonnees.remplissageMachine.clear();
        fichier = new File(chemin, nomFichierGlobal);
        if (fichier.exists() == true) {
            //Si le fichier existe
            try {
                //Creation du stream du fichier
                fileInputStream = new FileInputStream(fichier);
                inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.ISO_8859_1);
                bufferedReader = new BufferedReader(inputStreamReader);
                //Lecture
                ligne = bufferedReader.readLine();
                ligne = bufferedReader.readLine();
                while(ligne != null) {
                    String[] mots = ligne.split(";");
                    if(mots.length != 0) {
                        machineNom = mots[2];
                        machineId = mots[3];
                        produitNom = mots[5];
                        produitId = mots[4];
                        if(mots[10].equals("") == false)
                            quantiteVoulue = Integer.parseInt(mots[10]);
                        else
                            quantiteVoulue = 0;
                        if(machineId.equals("") == false)
                        {
                            if(MainActivity.gestionDonnees.listeMachine.containsKey(machineId) == false) {
                                //La machine n'est pas dans le dictionnaire de machine, on le cree
                                MainActivity.gestionDonnees.listeMachine.put(machineId, machineNom);
                            }
                            if(produitId != "")
                            {
                                if(MainActivity.gestionDonnees.listeProduit.containsKey(produitId) == false) {
                                    // Le produit n'est pas dans la liste produit, on le cree
                                    MainActivity.gestionDonnees.listeProduit.put(produitId, produitNom);
                                }
                                // Creation de la liste produit
                                infoProduit = new ArrayList<>();
                                infoProduit.add(produitId);
                                infoProduit.add(quantiteVoulue);
                                listeProduitMachine = new ArrayList<>();
                                listeProduitMachine.add(infoProduit);
                                produitTrouv = false;
                                if(MainActivity.gestionDonnees.remplissageMachine.containsKey(machineId) == false) {
                                    // La machine n'est pas dans le dictionnaire de remplissage on le cree tout
                                    MainActivity.gestionDonnees.remplissageMachine.put(machineId, listeProduitMachine);
                                }
                                else {
                                    // Sinon on verifie si le produit existe deja pour cette machine
                                    for (ArrayList produitPresent : MainActivity.gestionDonnees.remplissageMachine.get(machineId)) {
                                        if(produitPresent.get(0).equals(produitId) == true){
                                            // Le produit lue est celui qu'on cherche on charge la valeur et on addition
                                            valeurCourante = (int)produitPresent.get(1);
                                            valeurCourante = valeurCourante + quantiteVoulue;
                                            produitPresent.set(1, valeurCourante);
                                            produitTrouv = true;
                                            break;
                                        }
                                    }
                                    if(produitTrouv == false) {
                                        MainActivity.gestionDonnees.remplissageMachine.get(machineId).add(infoProduit);
                                    }
                                }
                            }
                        }
                    }
                    ligne = bufferedReader.readLine();
                }
                //Fermeture des Stream
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            }
            catch (IOException e) {
                erreur = "pb de lecture du fichier global: " + e.getMessage();
                pb = true;
            }
        }
        else {
            //Le fichier n'existe pas
            erreur = "pb le fichier global n'existe pas";
            pb = true;
        }
        if(pb == true)
            MainActivity.gestionFichier.EcritLogJelo(erreur);
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Verifie que le fichier global existe
    // retourne :
    // true = si existe
    // false = si n'existe pas
    //**************************************************
    public Boolean VerifieGlobalExiste()
    {
        File chemin;
        File fichier;

        chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        fichier = new File(chemin, "Ocerall.csv");
        return(fichier.exists());
    }
    //**************************************************

    //**************************************************
    // Lit le fichier etat contenant le date du dernier chargement du fichier global
    // retourne :
    // true = charge aujourd'hui
    // false = charge un autre jour
    //**************************************************
    public Boolean VerifieGlobalChargeAujour()
    {
        File fichier;
        boolean pb = false;
        FileInputStream fileInputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String dateChargement;
        File chemin;

        chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        fichier = new File(chemin, "datefichier.jelo");
        if (fichier.exists() == true) {
            //Charge la date courante
            try {
                //Creation du stream du fichier
                fileInputStream = new FileInputStream(fichier);
                inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.ISO_8859_1);
                bufferedReader = new BufferedReader(inputStreamReader);
                dateChargement = bufferedReader.readLine();
                pb = RetourneDateCourante().equals(dateChargement);
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            }
            catch (IOException e) {
                MainActivity.gestionFichier.EcritLogJelo("pb de lecture du fichier datefichier: " + e.getMessage());
                erreur = "pb de lecture du fichier datefichier: " + e.getMessage();
                pb = false;
            }
        }
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Cree ou met a jour le fichier datefichier.jelo
    //**************************************************
    public boolean EcritDateFichier() {
        boolean pb = false;
        File fichier;
        FileOutputStream writer;
        String LigneCr;
        File chemin;

        try {
            chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            fichier = new File(chemin, "datefichier.jelo");
            writer = new FileOutputStream(fichier);
            LigneCr = RetourneDateCourante() + "\n";
            writer.write(LigneCr.getBytes());
            writer.close();
        }
        catch (IOException e) {
            MainActivity.gestionFichier.EcritLogJelo("pb d'ecriture du fichier datefichier: " + e.getMessage());
            erreur = "pb d'ecriture du fichier datefichier: " + e.getMessage();
            pb = true;
        }
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Cree ou met a jour le fichier log_jelo.txt
    //**************************************************
    public boolean EcritLogJelo(String lignePass) {
        boolean pb = false;
        File fichier;
        FileOutputStream writer;
        String LigneCr;
        File chemin;

        try {
            chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            fichier = new File(chemin, RetourneDateCourante() + "log_jelo.txt");
            writer = new FileOutputStream(fichier, true);
            LigneCr = RetourneHeureCourante() + ": " + lignePass + "\n";
            writer.write(LigneCr.getBytes());
            writer.close();
        }
        catch (IOException e) {
            MainActivity.gestionFichier.EcritLogJelo("pb d'ecriture du fichier log: " + e.getMessage());
            erreur = "pb d'ecriture du fichier log: " + e.getMessage();
            pb = true;
        }
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Telecharge le fichier Ocerall.csv du reseau
    //**************************************************
    public void TelechargeFichierGlobal(){
        File fichier;
        FileOutputStream writer;
        File chemin;
        ArrayList<String> ligneExterneLue = new ArrayList<>();

        Thread telechargement = new Thread(new Runnable() {
            public void run() {
                try {
                    // Chargement des donnees distantes
                    URL url = new URL("http://192.168.1.44/Ocerall.csv");
                    InputStreamReader inputStreamReader = new InputStreamReader(url.openStream(), StandardCharsets.ISO_8859_1);
                    BufferedReader fluxEntrant = new BufferedReader(inputStreamReader);
                    String ligneCourante;

                    while((ligneCourante = fluxEntrant.readLine()) != null) {
                        ligneExterneLue.add(ligneCourante);
                    }
                } catch (Exception e) {
                    erreur = "Erreur de telechargement de fichier global: " + e.getMessage();
                    erreurChargementFichier = true;
                    MainActivity.gestionFichier.EcritLogJelo("Erreur de telechargement de fichier global: " + e.getMessage());
                }
            }
        });
        telechargement.start();
        try {
            telechargement.join();
            chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            fichier = new File(chemin, "Ocerall.csv");
            writer = new FileOutputStream(fichier, false);
            for (String ligne: ligneExterneLue) {
                writer.write((ligne + "\n").getBytes(StandardCharsets.ISO_8859_1));
            }
            writer.close();
        }
        catch (Exception e) {
            erreur = "Erreur de telechargement de fichier global: " + e.getMessage();
            erreurChargementFichier = true;
            MainActivity.gestionFichier.EcritLogJelo("Erreur de telechargement de fichier global: " + e.getMessage());
        }
    }
    //**************************************************
}
