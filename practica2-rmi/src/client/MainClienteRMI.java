package client;

import common.Dispositivo;
import common.DomoticaException;
import common.DomoticaInterfaz;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class MainClienteRMI {

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
        String host = (args.length < 1) ? "localhost" : args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            DomoticaInterfaz central = (DomoticaInterfaz) registry.lookup("CentralDomotica");

            Scanner sc = new Scanner(System.in);

            System.out.println("Conectado al sistema domotico por RMI.");
            System.out.println("Comandos:");
            System.out.println("login <usuario> <password>");
            System.out.println("list");
            System.out.println("status <ID>");
            System.out.println("set <ID> <valor>");
            System.out.println("logout");
            System.out.println("exit");

            while (true) {
                System.out.print("> ");
                String linea = sc.nextLine().trim();
                String[] cmd = linea.split("\\s+");

                if (cmd[0].equalsIgnoreCase("login")) {
                    if (cmd.length != 3) {
                        System.out.println("Uso: login <usuario> <password>");
                        continue;
                    }

                    String usuario = cmd[1];
                    String hash = generarHashPassword(cmd[2]);
                    central.login(usuario, hash);
                    System.out.println("Login correcto.");

                } else if (cmd[0].equalsIgnoreCase("list")) {
                    List<Dispositivo> dispositivos = central.listarDispositivos();
                    for (Dispositivo d : dispositivos) {
                        System.out.println(d);
                    }

                } else if (cmd[0].equalsIgnoreCase("status")) {
                    if (cmd.length != 2) {
                        System.out.println("Uso: status <ID>");
                        continue;
                    }

                    Dispositivo d = central.consultarEstado(cmd[1]);
                    System.out.println(d);

                } else if (cmd[0].equalsIgnoreCase("set")) {
                    if (cmd.length != 3) {
                        System.out.println("Uso: set <ID> <valor>");
                        continue;
                    }

                    Dispositivo d = central.cambiarEstado(cmd[1], cmd[2]);
                    System.out.println("Actualizado: " + d);

                } else if (cmd[0].equalsIgnoreCase("logout")) {
                    central.logout();
                    System.out.println("Sesion cerrada.");

                } else if (cmd[0].equalsIgnoreCase("exit")) {
                    System.out.println("Cliente finalizado.");
                    break;

                } else {
                    System.out.println("Comando desconocido.");
                }
            }

            sc.close();

        } catch (DomoticaException e) {
            System.err.println("Error funcional: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Excepcion en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}