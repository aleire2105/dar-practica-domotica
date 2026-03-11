package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String mensajeCliente;

            while ((mensajeCliente = entrada.readLine()) != null) {
                System.out.println("Recibido del cliente: " + mensajeCliente);

                String[] tokens = mensajeCliente.split(" ");
                String comando = tokens[0];

                switch (comando) {
                    case "REQ_LOGIN":
                        if (tokens.length == 3) {
                            String user = tokens[1];
                            String pass = tokens[2];
                            System.out.println("Intento de login de: " + user);

                            salida.println("RES_LOGIN_OK " + user);
                        } else {
                            salida.println("ACK_ERR 400 FORMATO_INCORRECTO");
                        }
                        break;

                    case "REQ_LIST":
                        salida.println("RES_LIST LUC1:ON CLI1:22 PER1:0");
                        break;

                    case "REQ_LOGOUT":
                        salida.println("RES_LOGOUT_OK");
                        socket.close();
                        return;

                    default:
                        salida.println("ACK_ERR 400 COMANDO_DESCONOCIDO");
                        break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error de comunicación con el cliente: " + e.getMessage());
        }
    }
}