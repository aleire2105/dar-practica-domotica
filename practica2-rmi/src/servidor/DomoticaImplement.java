package servidor; 

import common.DomoticaInterfaz; 
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class DomoticaImplement extends UnicastRemoteObject implements DomoticaInterfaz {
    private Map<String, Boolean> dispositivos;

    public DomoticaImplement() throws RemoteException {
        super();
        dispositivos = new HashMap<>();
        dispositivos.put("Luz_Salon", false);
        dispositivos.put("Persiana_Cocina", false);
        dispositivos.put("Aire_Acondicionado", false);
    }

    @Override
    public synchronized void conmutarDispositivo(String id, boolean estado) throws RemoteException {
        if (dispositivos.containsKey(id)) {
            dispositivos.put(id, estado);
            System.out.println("Dispositivo [" + id + "] cambiado a: " + (estado ? "ENCENDIDO" : "APAGADO"));
        } else {
            throw new RemoteException("Error: El dispositivo '" + id + "' no existe.");
        }
    }

    @Override
    public boolean getEstadoDispositivo(String id) throws RemoteException {
        return dispositivos.getOrDefault(id, false);
    }

    @Override
    public Map<String, Boolean> obtenerEstadoGeneral() throws RemoteException {
        return new HashMap<>(dispositivos);
    }

    @Override
    public String getNombreSistema() throws RemoteException {
        return "Central de Domótica Inteligente RMI";
    }
}