package server;

import common.DomoticaInterfaz;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MainServidor {

    public static void main(String[] args) {

        try {

            DomoticaInterfaz central = new DomoticaImplement();

            Registry registry = LocateRegistry.createRegistry(1099);

            registry.rebind("CentralDomotica", central);

            System.out.println("Servidor RMI de domotica listo en puerto 1099.");

        } catch (Exception e) {

            System.err.println("Error en el servidor: " + e.getMessage());

            e.printStackTrace();
        }
    }
}