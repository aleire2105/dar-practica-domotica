package client; // Asegúrate de que coincida con el nombre de tu carpeta

import common.Dispositivo;
import common.DomoticaException;
import common.DomoticaInterfaz;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.List;

public class MainClienteRMI {
    public static void main(String[] args) {
        // Lee la IP por argumento (o usa localhost por defecto)
        String host = (args.length < 1) ? "localhost" : args[0];
        
        try {
            // 1. Conectar al registro RMI
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            DomoticaInterfaz central = (DomoticaInterfaz) registry.lookup("CentralDomotica");
            System.out.println("Conectado exitosamente al servidor RMI en: " + host);

            // 2. HACER LOGIN (Obligatorio, o el servidor nos dará error 401)
            // Usamos el hash que tienes en tu servidor
            String hashCorrecto = "1eeac4d4b3d1c8f8b9b2f5cb06c5d2f8d9d0a51d1f7a8f4d2b7d8e4c9c2d6f4b";
            central.login("admin", hashCorrecto);
            System.out.println("Login aceptado por el servidor.\n");

            // 3. PROBAR LOS MÉTODOS DE LA INTERFAZ
            
            // A) Cambiar el estado de la luz
            System.out.println("--- Cambiando estado de Luz (LUC1) ---");
            Dispositivo luz = central.cambiarEstado("LUC1", "ON");
            System.out.println("Éxito. " + luz.toString());

            // B) Cambiar la temperatura de la climatización
            System.out.println("\n--- Cambiando Climatización (CLI1) ---");
            Dispositivo clima = central.cambiarEstado("CLI1", "24");
            System.out.println("Éxito. " + clima.toString());

            // C) Listar todos los dispositivos
            System.out.println("\n--- Estado general del sistema ---");
            List<Dispositivo> lista = central.listarDispositivos();
            for (Dispositivo d : lista) {
                System.out.println(" -> " + d.toString());
            }

            // 4. CERRAR SESIÓN
            central.logout();
            System.out.println("\nSesión cerrada correctamente.");

        // Capturamos tu excepción personalizada de lógica (ej. error 404, 401)
        } catch (DomoticaException e) {
            System.err.println("Error de la aplicación domótica: " + e.getMessage());
            
        // Capturamos los errores de red propios de RMI
        } catch (RemoteException e) {
            System.err.println("Error de comunicación (Red/RMI): " + e.getMessage());
            
        // Capturamos fallos inesperados
        } catch (Exception e) {
            System.err.println("Excepción inesperada en el cliente: " + e.toString());
            e.printStackTrace();
        }
    }
}