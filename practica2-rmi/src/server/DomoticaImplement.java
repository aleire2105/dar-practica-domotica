package server;

import common.Dispositivo;
import common.DomoticaException;
import common.DomoticaInterfaz;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DomoticaImplement extends UnicastRemoteObject implements DomoticaInterfaz {

    private ConcurrentHashMap<String, Dispositivo> dispositivos;
    // CAMBIO: Usamos un Set en lugar de un boolean para guardar qué clientes (IPs) están autenticados
    private Set<String> clientesAutenticados;

    public DomoticaImplement() throws RemoteException {
        super();

        // Inicializamos el Set de forma segura para concurrencia
        clientesAutenticados = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        dispositivos = new ConcurrentHashMap<>();

        dispositivos.put("LUC1", new Dispositivo("LUC1", "Luz", "OFF"));
        dispositivos.put("CLI1", new Dispositivo("CLI1", "Climatizacion", "22"));
        dispositivos.put("PER1", new Dispositivo("PER1", "Persiana", "0"));
    }

    @Override
    public void login(String usuario, String hashPassword) throws RemoteException, DomoticaException {
        String hashCorrecto = "1eeac4d4b3d1c8f8b9b2f5cb06c5d2f8d9d0a51d1f7a8f4d2b7d8e4c9c2d6f4b";

        if (hashPassword.equals(hashCorrecto)) {
            try {
                // CAMBIO: Obtenemos la IP del cliente y la registramos
                String ipCliente = RemoteServer.getClientHost();
                clientesAutenticados.add(ipCliente);
                System.out.println("Login correcto: " + usuario + " desde la IP: " + ipCliente);
            } catch (ServerNotActiveException e) {
                throw new DomoticaException("Error al obtener la IP del cliente");
            }
        } else {
            throw new DomoticaException("401 NOT_AUTHENTICATED");
        }
    }

    private void comprobarLogin() throws DomoticaException {
        try {
            // CAMBIO: Comprobamos si la IP de quien llama está en nuestra lista de autenticados
            String ipCliente = RemoteServer.getClientHost();
            if (!clientesAutenticados.contains(ipCliente)) {
                throw new DomoticaException("401 NOT_AUTHENTICATED");
            }
        } catch (ServerNotActiveException e) {
            throw new DomoticaException("401 NOT_AUTHENTICATED");
        }
    }

    @Override
    public List<Dispositivo> listarDispositivos() throws RemoteException, DomoticaException {
        comprobarLogin();
        return new ArrayList<>(dispositivos.values());
    }

    @Override
    public Dispositivo consultarEstado(String idDispositivo) throws RemoteException, DomoticaException {
        comprobarLogin();

        if (!dispositivos.containsKey(idDispositivo)) {
            throw new DomoticaException("404 DEVICE_NOT_FOUND");
        }

        return dispositivos.get(idDispositivo);
    }

    @Override
    public synchronized Dispositivo cambiarEstado(String idDispositivo, String nuevoValor) throws RemoteException, DomoticaException {
        comprobarLogin();

        if (!dispositivos.containsKey(idDispositivo)) {
            throw new DomoticaException("404 DEVICE_NOT_FOUND");
        }

        Dispositivo d = dispositivos.get(idDispositivo);
        boolean valido = false;

        if (idDispositivo.startsWith("LUC")) {
            if (nuevoValor.equals("ON") || nuevoValor.equals("OFF")) {
                valido = true;
            }
        } else if (idDispositivo.startsWith("CLI")) {
            try {
                int temp = Integer.parseInt(nuevoValor);
                if (temp >= 20 && temp <= 30) {
                    valido = true;
                }
            } catch (Exception e) {
                valido = false;
            }
        } else if (idDispositivo.startsWith("PER")) {
            try {
                int pos = Integer.parseInt(nuevoValor);
                if (pos >= 0 && pos <= 100) {
                    valido = true;
                }
            } catch (Exception e) {
                valido = false;
            }
        }

        if (!valido) {
            throw new DomoticaException("422 INVALID_VALUE");
        }

        d.setValor(nuevoValor);
        System.out.println("Dispositivo [" + idDispositivo + "] cambiado a: " + nuevoValor);

        return d;
    }

    @Override
    public void logout() throws RemoteException, DomoticaException {
        comprobarLogin();

        try {
            // CAMBIO: Borramos la IP del cliente cuando cierra sesión
            String ipCliente = RemoteServer.getClientHost();
            clientesAutenticados.remove(ipCliente);
            System.out.println("Sesion cerrada por la IP: " + ipCliente);
        } catch (ServerNotActiveException e) {
            // No hacemos nada grave, si falla es que ya no estaba conectado
        }
    }
}