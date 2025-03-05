package com.example.jeloscanner;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        String[] mots;

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
                    mots = ligne.split(";");
                    if(mots.length == 11) {
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
            writer.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
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
            writer.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
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
    public void TelechargeFichierGlobal() {
        File fichier;
        FileOutputStream writer;
        File chemin;
        ArrayList<String> ligneExterneLue = new ArrayList<>();

        Thread telechargement = new Thread(new Runnable() {
            public void run() {
                try {
                    Log.e("lesmiens", "debut chargement");
                    // Chargement des donnees distantes
                    URL url = new URL("http://192.168.1.44/Ocerall.csv");
                    InputStreamReader inputStreamReader = new InputStreamReader(url.openStream(), StandardCharsets.ISO_8859_1);
                    BufferedReader fluxEntrant = new BufferedReader(inputStreamReader);
                    String ligneCourante;

                    while((ligneCourante = fluxEntrant.readLine()) != null) {
                        ligneExterneLue.add(ligneCourante);
                        Log.e("lesmiens", "ligne : " + ligneCourante);
                    }
                    fluxEntrant.close();
                    inputStreamReader.close();
                } catch (Exception e) {
                    Log.e("lesmiens", "pb lecture");
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

    //**************************************************
    // Ecrit le fichier reprise.jelo
    //**************************************************
    public boolean EcritRepriseJelo(char status) {
        boolean pb = false;
        File fichier;
        FileOutputStream writer;
        String LigneCr;
        File chemin;

        try {
            chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            fichier = new File(chemin, "reprise.jelo");
            writer = new FileOutputStream(fichier, false);
            if(status == 'R') {
                LigneCr = "PARAGRAPHE;FINI" + "\n";
                writer.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            else {
                pb = SauveDonneeEnCours(writer);
                if(MainActivity.gestionDonnees.chargementEnCours == true)
                    pb = SauveMachineEnCours(writer);
                LigneCr = "PARAGRAPHE;FIN" + "\n";
                writer.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            writer.close();
        }
        catch (IOException e) {
            MainActivity.gestionFichier.EcritLogJelo("pb d'ecriture du fichier de reprise: " + e.getMessage());
            erreur = "pb d'ecriture du fichier de reprise: " + e.getMessage();
            pb = true;
        }
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Sauvegarde les donnees concernant la machine en cours de chargement
    //**************************************************
    public Boolean SauveMachineEnCours(FileOutputStream ecrivain) {
        String LigneCr;
        Boolean pb = false;

        try {
            // Sauvegarde HashMap<String, ArrayList<Integer>> donneeMachine = new HashMap<>();
            LigneCr = "PARAGRAPHE;donneeMachine" + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            for(Map.Entry<String, ArrayList<Integer>> chaqueProduit : MainActivity.gestionDonnees.donneeMachine.entrySet()) {
                LigneCr = "SOUSPARAGRAPHE;quantite;" + chaqueProduit.getValue().get(0) + ";" +  chaqueProduit.getValue().get(1) + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
                LigneCr = "SOUSPARAGRAPHE;produit;" + chaqueProduit.getKey() + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            // Sauvegarde public int quantiteCharge;
            LigneCr = "SOUSPARAGRAPHE;quantiteCharge;" + MainActivity.gestionDonnees.quantiteCharge + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            // Sauvegarde public int quantiteVoulue;
            LigneCr = "SOUSPARAGRAPHE;quantiteVoulue;" + MainActivity.gestionDonnees.quantiteVoulue + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            // Sauvegarde public String idMachineEnCours;
            LigneCr = "SOUSPARAGRAPHE;idMachineEnCours;" + MainActivity.gestionDonnees.idMachineEnCours + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
        }
        catch (IOException e) {
            MainActivity.gestionFichier.EcritLogJelo("pb d'ecriture du fichier de reprise: " + e.getMessage());
            erreur = "pb d'ecriture du fichier de reprise: " + e.getMessage();
            pb = true;
        }
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Sauvegarde les donnees le fichier en cours de traitement
    //**************************************************
    public Boolean SauveDonneeEnCours(FileOutputStream ecrivain)
    {
        String LigneCr;
        Boolean pb = false;

        try {
            // sauvegarde de public HashMap<String, String> listeMachine = new HashMap<>();
            LigneCr = "PARAGRAPHE;listeMachine" + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            for(Map.Entry<String, String> chaqueLigne : MainActivity.gestionDonnees.listeMachine.entrySet()) {
                LigneCr = chaqueLigne.getKey() + ";" + chaqueLigne.getValue() + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            //Sauvegarde denHashMap<String, String> listeProduit = new HashMap<>();
            LigneCr = "PARAGRAPHE;listeProduit" + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            for(Map.Entry<String, String> chaqueLigne : MainActivity.gestionDonnees.listeProduit.entrySet()) {
                LigneCr = chaqueLigne.getKey() + ";" + chaqueLigne.getValue() + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            //Sauvegarde ArrayList<String> listeMachineFinis= new ArrayList<>();
            LigneCr = "PARAGRAPHE;listeMachineFinis" + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            for(String chaqueLigne : MainActivity.gestionDonnees.listeMachineFinis) {
                LigneCr = chaqueLigne + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            //Sauvegarde HashMap<String, String> listeProduitCharges= new HashMap<>();
            LigneCr = "PARAGRAPHE;listeProduitCharges" + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            for(Map.Entry<String, String> chaqueLigne : MainActivity.gestionDonnees.listeProduitCharges.entrySet()) {
                LigneCr = chaqueLigne.getKey() + ";" + chaqueLigne.getValue() + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
            // Dico liste produit dans machine key = id Machine, value = liste (idproduit, qty voulue, qty faite)
            //Sauvegarde HashMap<String, ArrayList<ArrayList>> remplissageMachine = new HashMap<>();
            LigneCr = "PARAGRAPHE;remplissageMachine" + "\n";
            ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            for(Map.Entry<String, ArrayList<ArrayList>> chaqueMachine : MainActivity.gestionDonnees.remplissageMachine.entrySet()) {
                LigneCr = "SOUSPARAGRAPHE;machineDebut;" + chaqueMachine.getKey() + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
                for (ArrayList chaqueProduit : chaqueMachine.getValue()) {
                    LigneCr = "SOUSPARAGRAPHE;produit;" + chaqueProduit.get(0) + ";" + chaqueProduit.get(1) + "\n";
                    ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
                }
                LigneCr = "SOUSPARAGRAPHE;machineFin;" + chaqueMachine.getKey() + "\n";
                ecrivain.write(LigneCr.getBytes(StandardCharsets.ISO_8859_1));
            }
        }
        catch (IOException e) {
            MainActivity.gestionFichier.EcritLogJelo("pb d'ecriture du fichier de reprise: " + e.getMessage());
            erreur = "pb d'ecriture du fichier de reprise: " + e.getMessage();
            pb = true;
        }
        return(pb);
    }
    //**************************************************

    //**************************************************
    // Lit le fichier de reprise pour la sauveagrde des donnees en cours
    // retourne :
    // true = des donnees ont ete chargees, il faut les afficher
    // false = Aucune donnee on repart a zero
    //**************************************************
    public Boolean LitRepriseJelo()
    {
        File fichier;
        boolean pb = false;
        FileInputStream fileInputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String ligne;
        File chemin;
        String sousPartie = "inconnue";
        String[] mots;
        ArrayList donneeProduitTampon = new ArrayList<>();
        ArrayList<ArrayList> donneeMachineTampon = new ArrayList<>();

        chemin = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        fichier = new File(chemin, "reprise.jelo");
        if (fichier.exists() == true) {
            //Charge la date courante
            try {
                //Creation du stream du fichier
                fileInputStream = new FileInputStream(fichier);
                inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.ISO_8859_1);
                bufferedReader = new BufferedReader(inputStreamReader);
                ligne = bufferedReader.readLine();
                if(ligne.equals("PARAGRAPHE;FINI") == false) {
                    MainActivity.gestionDonnees.listeMachineFinis.clear();
                    MainActivity.gestionDonnees.listeProduitCharges.clear();
                    MainActivity.gestionDonnees.listeMachine.clear();
                    MainActivity.gestionDonnees.listeProduit.clear();
                    MainActivity.gestionDonnees.remplissageMachine.clear();
                    MainActivity.gestionDonnees.chargementEnCours = false;
                    pb = true;
                    while (ligne != null) {
                        if (ligne.equals("PARAGRAPHE;donneeMachine") == true) {
                            MainActivity.gestionDonnees.chargementEnCours = true;
                            MainActivity.gestionDonnees.quantiteVoulue = 0;
                            MainActivity.gestionDonnees.quantiteCharge = 0;
                            MainActivity.gestionDonnees.idMachineEnCours = "";
                            MainActivity.gestionDonnees.donneeMachine.clear();
                            sousPartie = "donneeMachine";
                        }
                        else if (ligne.equals("PARAGRAPHE;FIN" ) == true)
                            break;
                        // Lit de public HashMap<String, String> listeMachine = new HashMap<>();
                        else if (ligne.equals("PARAGRAPHE;listeMachine") == true) {
                            sousPartie = "listeMachine";
                        }
                            //Sauvegarde denHashMap<String, String> listeProduit = new HashMap<>();
                        else if (ligne.equals("PARAGRAPHE;listeProduit") == true) {
                            sousPartie = "listeProduit";
                        }
                            //Sauvegarde ArrayList<String> listeMachineFinis= new ArrayList<>();
                        else if (ligne.equals("PARAGRAPHE;listeMachineFinis") == true)
                            sousPartie = "listeMachineFinis";
                            //Sauvegarde HashMap<String, String> listeProduitCharges= new HashMap<>();
                        else if (ligne.equals("PARAGRAPHE;listeProduitCharges") == true)
                            sousPartie = "listeProduitCharges";
                            // Dico liste produit dans machine key = id Machine, value = liste (idproduit, qty voulue, qty faite)
                            //Sauvegarde HashMap<String, ArrayList<ArrayList>> remplissageMachine = new HashMap<>();
                        else if (ligne.equals("PARAGRAPHE;remplissageMachine") == true) {
                            sousPartie = "remplissageMachine";
                        }
                        else {
                            switch (sousPartie) {
                                case "donneeMachine":
                                    mots = ligne.split(";");
                                    if (mots.length > 2) {
                                        if(mots[0].equals("SOUSPARAGRAPHE") == true) {
                                            // Sauvegarde public int quantiteCharge;
                                            if(mots[1].equals("quantiteCharge") == true)
                                                MainActivity.gestionDonnees.quantiteCharge = Integer.parseInt(mots[2]);
                                            if(mots[1].equals("quantiteVoulue") == true)
                                                MainActivity.gestionDonnees.quantiteVoulue = Integer.parseInt(mots[2]);
                                            if(mots[1].equals("idMachineEnCours") == true)
                                                MainActivity.gestionDonnees.idMachineEnCours = mots[2];
                                            if(mots[1].equals("quantite") == true && mots.length > 3) {
                                                donneeProduitTampon = new ArrayList<>();
                                                donneeProduitTampon.add(Integer.parseInt(mots[2]));
                                                donneeProduitTampon.add(Integer.parseInt(mots[3]));
                                            }
                                            if(mots[1].equals("produit") == true)
                                                MainActivity.gestionDonnees.donneeMachine.put(mots[2],donneeProduitTampon);
                                        }
                                    }
                                    break;
                                case "listeMachine":
                                    // Lit de public HashMap<String, String> listeMachine = new HashMap<>();
                                    mots = ligne.split(";");
                                    if (mots.length == 2)
                                        MainActivity.gestionDonnees.listeMachine.put(mots[0], mots[1]);
                                    break;
                                case "listeProduit":
                                    //Sauvegarde denHashMap<String, String> listeProduit = new HashMap<>();
                                    mots = ligne.split(";");
                                    if (mots.length == 2)
                                        MainActivity.gestionDonnees.listeProduit.put(mots[0], mots[1]);
                                    break;
                                case "listeMachineFinis":
                                    //Sauvegarde ArrayList<String> listeMachineFinis= new ArrayList<>();
                                    MainActivity.gestionDonnees.listeMachineFinis.add(ligne);
                                    break;
                                case "listeProduitCharges":
                                    //Sauvegarde HashMap<String, String> listeProduitCharges= new HashMap<>();
                                    mots = ligne.split(";");
                                    if (mots.length == 2)
                                        MainActivity.gestionDonnees.listeProduitCharges.put(mots[0], mots[1]);
                                    break;
                                case "remplissageMachine":
                                    // Dico liste produit dans machine key = id Machine, value = liste (idproduit, qty voulue, qty faite)
                                    //Sauvegarde HashMap<String, ArrayList<ArrayList>> remplissageMachine = new HashMap<>();
                                    mots = ligne.split(";");
                                    if (mots.length > 2) {
                                          if(mots[0].equals("SOUSPARAGRAPHE") == true) {
                                              if(mots[1].equals("machineDebut") == true)
                                                  donneeMachineTampon = new ArrayList<>();
                                              if(mots[1].equals("produit") == true && mots.length == 4) {
                                                  donneeProduitTampon = new ArrayList<>();
                                                  donneeProduitTampon.add(mots[2]);
                                                  donneeProduitTampon.add(Integer.parseInt(mots[3]));
                                                  donneeMachineTampon.add(donneeProduitTampon);
                                              }
                                              if(mots[1].equals("machineFin") == true)
                                                  MainActivity.gestionDonnees.remplissageMachine.put(mots[2],donneeMachineTampon);
                                          }
                                    }
                                    break;
                            }
                        }
                        ligne = bufferedReader.readLine();
                    }
                }
                fileInputStream.close();
                inputStreamReader.close();
                bufferedReader.close();
            }
            catch (IOException e) {
                MainActivity.gestionFichier.EcritLogJelo("pb de lecture du fichier de reprise: " + e.getMessage());
                erreur = "pb de lecture du fichier de reprise: " + e.getMessage();
                pb = false;
            }
        }
        return(pb);
    }
    //**************************************************
}
