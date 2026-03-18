package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainCliente {

    private static final String HOST = "localhost";
    private static final int PUERTO = 8080;

    public static void main(String[] args) {

        System.out.println("Conectando al servidor domótico...");

        try (
            Socket socket = new Socket(HOST, PUERTO);
            BufferedReader entradaServidor = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter salidaServidor = new PrintWriter(
                    socket.getOutputStream(), true);
            Scanner teclado = new Scanner(System.in)
        ) {

            System.out.println("Conectado al servidor en " + HOST + ":" + PUERTO);
            System.out.println("Escribe comandos del protocolo. Ejemplo:");
            System.out.println("REQ_LOGIN admin 1234");
            System.out.println("REQ_LIST");
            System.out.println("REQ_STATUS LUC1");
            System.out.println("CMD_CONTROL LUC1 SET ON");
            System.out.println("REQ_LOGOUT");

            while (true) {

                System.out.print("> ");
                String comando = teclado.nextLine();

                salidaServidor.println(comando);

                String respuesta = entradaServidor.readLine();

                if (respuesta == null) {
                    System.out.println("El servidor cerró la conexión.");
                    break;
                }

                System.out.println("Servidor: " + respuesta);

                if (comando.equalsIgnoreCase("REQ_LOGOUT")) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}