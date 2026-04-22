package client; // IMPORTANTE

import common.DomoticaInterfaz; // IMPORTAMOS LA INTERFAZ
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class MainClienteRMI {
    public static void main(String[] args) {
        String host = (args.length < 1) ? "localhost" : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            DomoticaInterfaz central = (DomoticaInterfaz) registry.lookup("CentralDomotica");

            System.out.println("Conectado a: " + central.getNombreSistema());
            central.conmutarDispositivo("Luz_Salon", true);
            System.out.println("Luz del salon encendida remotamente.");
            System.out.println("Estado actual: " + central.obtenerEstadoGeneral());

        } catch (RemoteException e) {
            System.err.println("Error de comunicacion (Red/RMI): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Excepcion en el cliente: " + e.toString());
        }
    }
}