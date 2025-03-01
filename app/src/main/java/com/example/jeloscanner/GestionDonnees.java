package com.example.jeloscanner;

import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class GestionDonnees
{
    //**************************************************
    // LISTES GLOBALES VENANT DU FICHIER GLOBAL
    //**************************************************
    // Dico machine key = id machine, value = nom de la machine
    public HashMap<String, String> listeMachine = new HashMap<>();
    // Dico liste produit key = id produit, value = nom produit
    public HashMap<String, String> listeProduit = new HashMap<>();
    // Dico liste produit dans machine key = id Machine, value = liste (idproduit, qty voulue, qty faite)
    public HashMap<String, ArrayList<ArrayList>> remplissageMachine = new HashMap<>();
    //**************************************************
    // LISTES ASSOCIEES AU CHARGEMENT D'UNE MACHINE
    //**************************************************
    // liste des produits et quantité a charger pour la machine courante A SAUVEGARDER
    // key = produit, value = Liste (Qty a chargée, qty deja faite)
    public HashMap<String, ArrayList<Integer>> donneeMachine = new HashMap<>();
    // Liste des text view de quantite et des bord de chaque produit
    public HashMap<String, ArrayList> listeViewProduit = new HashMap<>();
    // Quantité totale chargé pour cette machine A SAUVEGARDER
    public int quantiteCharge;
    // Quantité totale attendue pour cette machine A SAUVEGARDER
    public int quantiteVoulue;
    // Id de la machine en cours A SAUVEGARDER
    public String idMachineEnCours;
    //**************************************************
    // LISTES GLOBALES DE L ETAT D'AVANCEMENT DU CHARGEMENT
    //**************************************************
    // Liste des machines deja chargées et terminées  A SAUVEGARDER
    public ArrayList<String> listeMachineFinis= new ArrayList<>();
    // Liste des produits chargés pour evité les doublons key = numOrdreProduit value = idMachine ou il a ete chargé  A SAUVEGARDER
    public HashMap<String, String> listeProduitCharges= new HashMap<>();
    // Etat de chargement en cours ou non A SAUVEGARDER
    public Boolean chargementEnCours = false;
    //**************************************************
    // VARIABLES DE l'ENCOURS
    //**************************************************
    public String idMachine;
    public String idProduit;
    public String numDordre;
    public String donneeScannee;
    public String donneeBruteRecu;
    public char resultat = 'R';
    // bleu azur
    public int produitDebutCouleur = -15532289;
    // jaune poussin
    public int produitCourantCouleur = -131299;
    // Rouge foncé
    public int produitProblemeCouleur = -65022;
    // vert clair
    public int produitFinieCouleur = -9830634;

    GestionDonnees()
    {
    }

    //**************************************************
    // Remet les donnees a zero
    //**************************************************
    public void RemetZeroDonnees()
    {
        listeMachineFinis.clear();
        listeProduitCharges.clear();
        chargementEnCours = false;
    }
    //**************************************************

    //**************************************************
    // Analyse de la donnee recue par le scanner
    // Retourne:
    // R si la donnee n'est pas compatible
    // M si c'est un numero de machine (num machine dans idMachine)
    // P si c'est une produit (num d'ordre et id produit dans idProduit, numDordre
    // C si une reception est en cours
    //**************************************************
    public char AnalyseDonneeScannee(String donneeRecue)
    {
        String[] motsProduit;
        char finDonneeRecu;
        char debutDonneeRecu;
        String aTraiter;

        if(donneeRecue.length() > 0) {
            // id machine commence et finisse par une * c'est un id machine
            debutDonneeRecu = donneeRecue.charAt(0);
            finDonneeRecu = donneeRecue.charAt(donneeRecue.length() - 1);
            if (debutDonneeRecu == '*' && finDonneeRecu == '*') {
                donneeScannee = donneeRecue;
                resultat = 'M';
            }
            else {
                // Ca comment par # et ca fini par #, c'est un produit
                if (debutDonneeRecu == '#' && finDonneeRecu == '#') {
                    donneeScannee = donneeRecue;
                    resultat = 'P';
                }
                else {
                    // ca commence par # et ca termine pas par # et c'est pas en cours, on commence en cours
                    if (debutDonneeRecu == '#' && finDonneeRecu != '#' && resultat != 'C') {
                        resultat = 'C';
                        donneeScannee = donneeRecue;
                    }
                    else {
                        // C'est en cours et ca ne commence pas par # et ca ne fini par par #, concate et toujours en cours
                        if (debutDonneeRecu != '#' && finDonneeRecu != '#' && resultat == 'C') {
                            resultat = 'C';
                            donneeScannee = donneeScannee + donneeRecue;
                        }
                        else {
                            // C'est en cours et ca ne commence pas par # mais ca fini par #, concate et termine c'est un produit
                            if (debutDonneeRecu != '#' && finDonneeRecu == '#' && resultat == 'C') {
                                resultat = 'P';
                                donneeScannee = donneeScannee + donneeRecue;
                            }
                            else
                                resultat = 'R';
                        }
                    }
                }
            }
        }
        if(resultat == 'M') {
            idMachine = donneeScannee.substring(1,donneeScannee.length() - 1);
        }
        if(resultat == 'P') {
            aTraiter = donneeScannee.substring(1,donneeScannee.length() - 1);
            motsProduit = aTraiter.split("\\|");
            if(motsProduit.length == 5) {
                // ordre attentdu des donnees
                // 0 Identifiant produit
                // 1 Descriptif produit
                // 2 DLC
                // 3 Numéro de lot
                // 4 Numéro d’ordre
                // Nom du produit
                idProduit = motsProduit[0];
                // Num ordre du produit
                numDordre = motsProduit[4];
                resultat = 'P';
            }
        }
        if(resultat != 'C') {
            donneeBruteRecu = donneeScannee;
        }
        return(resultat);
    }
    //**************************************************

    //**************************************************
    // Analyse l'id machine qui a ete renvoyé par le scanner
    // Retourne:
    // R rien de special est signalé
    // E si un chargement est en cours
    // F la machine a deja ete chargée
    // P cette machine n'apparait pas dans le fichier global
    // V les donnees concernant cette machine sont vides
    // N un nouveau chargement a commencé
    //**************************************************
    public char AnalyseIdMachine(String IdMachineRecue)
    {
        char resultat = 'R';

        // aucun chargement en cours
        if(chargementEnCours == false) {
            // Cette machine n'a pas deja ete chargee
            if(listeMachineFinis.contains(IdMachineRecue) == false) {
                // Cette machine existe dans le fichier global
                if(remplissageMachine.containsKey(IdMachineRecue) == true) {
                    // Des produits sont associes a cette machine dans le fichier global
                    if(remplissageMachine.get(IdMachineRecue).isEmpty() == false) {
                        resultat = 'N';
                    }
                    else
                        resultat = 'V';
                }
                else
                    resultat = 'P';
            }
            else
                resultat = 'F';
        }
        else {
            if(idMachineEnCours.equals(IdMachineRecue) == false)
                resultat = 'E';
        }
        return(resultat);
    }
    //**************************************************

    //**************************************************
    // Analyse l'id produit qui a ete lu par le scanner
    // Retourne:
    // R rien de special est signalé
    // E aucun chargement est en cours
    // F le produit n'est pas dans la liste correspondant a cette machine
    // P ce produit a deja ete chargé
    // V quantité maximum atteinte pour ce produit dans cette machine
    // N un nouveau produit a ete rajouté mais ce produit n'est pas entierement fini
    // L un nouveau produit a ete rajouté et est entierement chargé
    // G le chargement de la machine est terminé
    //**************************************************
    public char AnalyseIdProduit(String idProduitRecu, String numOrdreProduitRecu)
    {
        char resultat = 'R';

        // un chargement est en cours
        if(chargementEnCours == true) {
            // Ce produit existe pour cette machine
            if(donneeMachine.containsKey(idProduitRecu) == true) {
                // Ce produit n'a pas deja ete charge
                if(listeProduitCharges.containsKey(numOrdreProduitRecu) == false) {
                    // Ce produit n'a pas atteint le max de chargement le concernant
                    int toto = donneeMachine.get(idProduitRecu).get(0);
                    int tata = donneeMachine.get(idProduitRecu).get(1);
                    if(donneeMachine.get(idProduitRecu).get(0) > donneeMachine.get(idProduitRecu).get(1)) {
                        // Ajoute une produit charge
                        donneeMachine.get(idProduitRecu).set(1, donneeMachine.get(idProduitRecu).get(1) + 1);
                        // Ajoute le produit chargé a la liste
                        listeProduitCharges.put(numOrdreProduitRecu, idMachineEnCours);
                        // increment la qty de produit charges
                        quantiteCharge ++;
                        // Test que le chargement est terminé
                        if(quantiteCharge == quantiteVoulue) {
                            // termine le chargement de la machine
                            resultat = 'G';
                        }
                        else
                            // test que ce produit est entierement chargé pour cette machine
                            if(donneeMachine.get(idProduitRecu).get(0) == donneeMachine.get(idProduitRecu).get(1))
                                resultat = 'L';
                            else
                                resultat = 'N';
                    }
                    else
                        resultat = 'V';
                }
                else
                    resultat = 'P';
            }
            else
                resultat = 'F';
        }
        else
            resultat = 'E';
        return(resultat);
    }
    //**************************************************

    //**************************************************
    // Demarrage de chargement d'une machine
    //**************************************************
    public void DemarrageChargementmachine(String machineIdPass)
    {
        ArrayList<Integer> quantite;
        String produ;
        int qtyProdu;

        donneeMachine.clear();
        quantiteCharge = 0;
        quantiteVoulue = 0;
        chargementEnCours = true;
        idMachineEnCours = machineIdPass;
        listeViewProduit.clear();
        for (ArrayList produitPresent : remplissageMachine.get(machineIdPass)) {
            // sort le nom et la quantité de produit
            produ = (String)produitPresent.get(0);
            qtyProdu = (int)produitPresent.get(1);
            // le produit est deja dans la liste on incrment
            if(donneeMachine.containsKey(produ) == true) {
                // Ajoute la qty de produit au produit existant
                donneeMachine.get(produ).set(0, donneeMachine.get(produ).get(0) + qtyProdu);
            }
            // sinon cree le produit dans la liste
            else {
                quantite = new ArrayList<>();
                quantite.add(qtyProdu);
                quantite.add(0);
                donneeMachine.put(produ, quantite);
            }
            quantiteVoulue = quantiteVoulue + qtyProdu;
        }
    }
    //**************************************************

    //**************************************************
    // Arret de chargement d'une machine
    //**************************************************
    public void ArretChargementmachine()
    {
        chargementEnCours = false;
        listeMachineFinis.add(idMachineEnCours);
    }
    //**************************************************

    //**************************************************
    // Ajoute Les views concernant ce produit pour utilisation ulterieur
    //**************************************************
    public void SauveViewProduit(String idProduitPass, GradientDrawable bordPass, TextView textViewPass, int couleurPass)
    {
        ArrayList vueAffi = new ArrayList();

        vueAffi.add(bordPass);
        vueAffi.add(textViewPass);
        vueAffi.add('A');
        listeViewProduit.put(idProduitPass,vueAffi);
    }
    //**************************************************
}
