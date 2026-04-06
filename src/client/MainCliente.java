package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class MainCliente {

    private static final String HOST = "localhost";
    private static final int PUERTO = 8080;

    private static String generarHashPassword(String password) throws Exception {
        String codificada = Base64.getEncoder()
                .encodeToString(password.getBytes(StandardCharsets.UTF_8));

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(codificada.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        System.out.println("Conectando al servidor domótico...");

        try (
                Socket socket = new Socket(HOST, PUERTO);
                BufferedReader entradaServidor = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter salidaServidor = new PrintWriter(
                        socket.getOutputStream(), true);
                Scanner teclado = new Scanner(System.in)) {

            System.out.println("Comandos disponibles:");
            System.out.println("REQ_LOGIN <usuario> <password>   -> Iniciar sesión");
            System.out.println("REQ_LIST                        -> Listar dispositivos");
            System.out.println("REQ_STATUS <ID>                 -> Consultar estado de un dispositivo");
            System.out.println("CMD_CONTROL <ID> SET <valor>    -> Modificar un dispositivo");
            System.out.println("REQ_LOGOUT                      -> Cerrar sesión");

            while (true) {

                System.out.print("> ");
                String comando = teclado.nextLine();

                String[] partes = comando.trim().split("\\s+");

                if (partes.length == 3 && partes[0].equalsIgnoreCase("REQ_LOGIN")) {
                    String usuario = partes[1];
                    String password = partes[2];

                    String hashPassword = generarHashPassword(password);

                    comando = "REQ_LOGIN " + usuario + " " + hashPassword;
                }
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