package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class ManejadorCliente implements Runnable {
    private Socket socket;

    private static ConcurrentHashMap<String, String> inventario = new ConcurrentHashMap<>();

    // Inicializamos el estado base de los dispositivos cuando arranca el servidor
    static {
        inventario.put("LUC1", "OFF");
        inventario.put("CLI1", "22");
        inventario.put("PER1", "0");
    }

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

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

    }

    @Override
    public void run() {
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {
            String mensajeCliente;
            boolean estaAutenticado = false;

            while ((mensajeCliente = entrada.readLine()) != null) {
                System.out.println("Recibido del cliente: " + mensajeCliente);

                String[] tokens = mensajeCliente.split(" ");
                String comando = tokens[0].trim().toUpperCase();

                switch (comando) {
                    case "REQ_LOGIN":
                        if (tokens.length == 3) {
                            String user = tokens[1];
                            String hashRecibido = tokens[2];

                            System.out.println("Intento de login de: " + user);

                            String hashEsperado = generarHashPassword("pass123");

                            if (user.equals("admin") && hashRecibido.equals(hashEsperado)) {
                                estaAutenticado = true;
                                salida.println("RES_LOGIN_OK " + user);
                                System.out.println("[LOG] Login exitoso para el usuario: " + user);
                            } else {
                                salida.println("ACK_ERR 401 NOT_AUTHENTICATED");
                                System.out.println("[LOG] Login fallido. Hash incorrecto.");
                            }
                        } else {
                            salida.println("ACK_ERR 400 FORMATO_INCORRECTO");
                        }
                        break;

                    case "REQ_LIST":
                        if (!estaAutenticado) {
                            salida.println("ACK_ERR 401 NOT_AUTHENTICATED");
                        } else {
                            // Leemos del inventario
                            StringBuilder respuesta = new StringBuilder("RES_LIST");
                            for (Map.Entry<String, String> entry : inventario.entrySet()) {
                                respuesta.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
                            }
                            salida.println(respuesta.toString());
                            System.out.println("[LOG] Listado de dispositivos enviado al cliente.");
                        }
                        break;

                    case "CMD_CONTROL":
                        // Formato:CMD_CONTROL LUC1 SET ON
                        if (!estaAutenticado) {
                            salida.println("ACK_ERR 401 NOT_AUTHENTICATED");
                        } else if (tokens.length == 4) {
                            String idDispositivo = tokens[1];
                            String accion = tokens[2];
                            String nuevoValor = tokens[3];

                            // 1. Validacion accion correcta
                            if (!accion.equals("SET")) {
                                salida.println("ACK_ERR 400 FORMATO_INCORRECTO");
                            }
                            // 2. Validacion equipo existe
                            else if (!inventario.containsKey(idDispositivo)) {
                                salida.println("ACK_ERR 404 DEVICE_NOT_FOUND");
                            }
                            // 3. Validacion valores correctos
                            else {
                                boolean valorValido = false;

                                if (idDispositivo.startsWith("LUC")
                                        && (nuevoValor.equals("ON") || nuevoValor.equals("OFF"))) {
                                    valorValido = true;
                                } else if (idDispositivo.startsWith("CLI")) {
                                    try {
                                        int temp = Integer.parseInt(nuevoValor);
                                        if (temp >= 20 && temp <= 30)
                                            valorValido = true;
                                    } catch (NumberFormatException e) {
                                    }
                                } else if (idDispositivo.startsWith("PER")) {
                                    try {
                                        int pos = Integer.parseInt(nuevoValor);
                                        if (pos >= 0 && pos <= 100)
                                            valorValido = true;
                                    } catch (NumberFormatException e) {
                                    }
                                }

                                // se aplica
                                if (valorValido) {
                                    inventario.put(idDispositivo, nuevoValor);
                                    salida.println("ACK_OK " + idDispositivo + " " + nuevoValor);
                                    System.out.println("[LOG] Cliente modificó " + idDispositivo + " a " + nuevoValor);
                                } else {
                                    salida.println("ACK_ERR 422 INVALID_VALUE");
                                }
                            }
                        } else {
                            salida.println("ACK_ERR 400 FORMATO_INCORRECTO");
                        }
                        break;

                    case "REQ_STATUS":

                        if (!estaAutenticado) {
                            salida.println("ACK_ERR 401 NOT_AUTHENTICATED");
                        } else if (tokens.length == 2) {
                            String idDispositivo = tokens[1];

                            if (inventario.containsKey(idDispositivo)) {
                                String estadoActual = inventario.get(idDispositivo);
                                salida.println("RES_STATUS " + idDispositivo + " " + estadoActual);
                            } else {
                                salida.println("ACK_ERR 404 DEVICE_NOT_FOUND");
                            }
                        } else {
                            salida.println("ACK_ERR 400 FORMATO_INCORRECTO");
                        }
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