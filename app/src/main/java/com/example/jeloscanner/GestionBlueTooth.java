package com.example.jeloscanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class GestionBlueTooth
{
    private Set<BluetoothDevice> appareilConnecte;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private BluetoothDevice mmDevice;

    public String erreur;
    public String donneePartielleRecue ="";

    GestionBlueTooth()
    {
    }

    //**************************************************
    // Initialisation du bluetooth et creation du flux de donnee
    // retourne true si il y a eu un probleme et false sinon
    //**************************************************
    @SuppressLint("MissingPermission")
    public Boolean InitialisationBluetooth()
    {
        Boolean pb = false;

        try {
            // Allume le bluetoothe si il est eteint
            if (bluetoothAdapter == null || bluetoothAdapter.isEnabled() == false) {
                bluetoothAdapter.enable();
            }
            // Cherche le scanner et verifie qu'il est connecté
            appareilConnecte = bluetoothAdapter.getBondedDevices();
            if (appareilConnecte.size() > 0) {
                for (BluetoothDevice device : appareilConnecte) {
                    if (device.getName().equals("GS R1521-B20FB5") == true) {
                        mmDevice = device;
                        pb = false;
                        break;
                    }
                }
            }
            if(pb == false) {
                // Creation du socket et du flux d'echange
                mmSocket = mmDevice.createRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                bluetoothAdapter.cancelDiscovery();
                mmSocket.connect();
                mmInStream = mmSocket.getInputStream();
            }
            else
                erreur = "impossible de trouver le scanner !";
        }
        catch (Exception e) {
            pb = true;
            erreur = "Erreur durant l'initialisation du bluetooth: "+ e.getMessage();
        }
        if(pb == true)
            MainActivity.gestionFichier.EcritLogJelo(erreur);
        return (pb);
    }
    //**************************************************

    //**************************************************
    // Lecture d'une donnee scannée
    // Retourne le string de caractere lu
    //**************************************************
    public Boolean ReceptionDonnee() {
        byte[] mmBuffer;
        int numBytes;
        Boolean pb = false;

        try {
            mmBuffer = new byte[300];
            numBytes = mmInStream.read(mmBuffer, 0, 300);
            donneePartielleRecue = new String(mmBuffer, 0, numBytes,StandardCharsets.UTF_8);
            pb = false;
        }
        catch (IOException e) {
            pb = true;
            erreur = "Stream entrant deconnecté: " + e.getMessage();
        }
        return (pb);
    }
    //**************************************************
}
