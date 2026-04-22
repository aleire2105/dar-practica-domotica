package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DomoticaInterfaz extends Remote {

    void login(String usuario, String hashPassword)
            throws RemoteException, DomoticaException;

    List<Dispositivo> listarDispositivos()
            throws RemoteException, DomoticaException;

    Dispositivo consultarEstado(String idDispositivo)
            throws RemoteException, DomoticaException;

    Dispositivo cambiarEstado(String idDispositivo, String nuevoValor)
            throws RemoteException, DomoticaException;

    void logout() throws RemoteException, DomoticaException;
}
