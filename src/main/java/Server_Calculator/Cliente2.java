package Server_Calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Operacion de prueva:  3^4+6*((9+12/6)*(24/2^3-1)).

public class Cliente2 {

    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int puerto = 1234; // Mismo número de puerto que el servidor
        boolean continuar = true;

        try (Socket socket = new Socket(hostName, puerto); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            while (continuar) {
                // Recibir y mostrar el menú del servidor
                String menu = in.readLine();
                System.out.println("Menú del servidor: " + menu);

                // Leer la elección del usuario y enviarla al servidor
                System.out.print("Tu elección: ");
                String eleccion = stdIn.readLine();
                out.println(eleccion);

                switch (eleccion) {
                    case "1":
                        while (true) {
                            System.out.print("Introduce la operación matemática: ");
                            String operacion = stdIn.readLine();
                            if (operacion.matches("[-+*/%^()\\d]+")) {
                                // La operación es numérica
                                out.println(operacion);
                                // Recibir e imprimir el resultado desde el servidor
                                String resultado = in.readLine();
                                System.out.println("Resultado: " + resultado);
                                break; // salir del bucle while
                            } else {
                                System.out.println("La operación no es numérica. Intenta nuevamente.");
                            }
                        }
                        break;

                    case "2":
                        System.out.println("Consultando historial...");

                        // Recibir e imprimir el historial del servidor
                        String historialMsg;
                        while ((historialMsg = in.readLine()) != null && !historialMsg.equals("")) {
                            System.out.println(historialMsg);
                        }
                        break;
                    case "3":
                        System.out.println("Cerrando conexión...");
                        continuar = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
        }
    }
}