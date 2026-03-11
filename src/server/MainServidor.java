package server;

import java.net.ServerSocket;
import java.net.Socket;

public class MainServidor {

    private static final int PUERTO = 8080;

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor Domótico en el puerto " + PUERTO + "...");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor listo y esperando conexiones.");

            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("¡Nuevo cliente conectado desde: " + socketCliente.getInetAddress() + "!");

                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                new Thread(manejador).start();
            }

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}