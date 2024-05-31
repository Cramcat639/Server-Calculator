package Server_Encrypt_RSA;

import Server_Encrypt_RSA.RSAKeyPairGenerator;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongBiFunction;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.Cipher;

public class ServerCalculadora {

    private static final Logger logger = Logger.getLogger(ServerCalculadora.class.getName());  //metodo para el log

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, Exception {
        int puerto = 1234;
        
        // Instanciamos la clase para generar los ficheros que continen las claves cada vez que iniciamos el servidor y poder usarlas en memoria.
        // A diferencia del cliente que vamos a leer los ficheros para obtener las claves.
        RSAKeyPairGenerator keyGen = new RSAKeyPairGenerator();
        
        // Formato de fecha para el nombre del archivo de registro
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        // Nombre del archivo de registro con la fecha actual
        String logFileName = dateFormat.format(date) + "_serverRSA.log";

        // Se crea el archivo para el registro en la carpeta raiz del proyecto.
        FileHandler fileHandler = new FileHandler(logFileName, true);  // true indica que si existe el archivo debe escribir a continuación
        fileHandler.setFormatter(new SimpleFormatter());    // Forma de escribir los datos en el archivo (no he provado otros formatos)
        logger.addHandler(fileHandler);

        ServerSocket serverSocket = new ServerSocket(puerto);
        logger.info("Servidor iniciado, esperando clientes...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            logger.info("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());   // le asigna un nombre basado en su IP

            // Crear un hilo para cada conexión con un cliente
            Thread clientThread = new Thread(new ClientHandler(clientSocket, keyGen));
            clientThread.start();
        }
    }

    private static class ClientHandler implements Runnable {

        private Socket clientSocket;
        private RSAKeyPairGenerator pair;
        private List<String> historial = new ArrayList<>();  // Historial local para este cliente (si lo pones global se mezclan los historiales de todos los clientes)

        public ClientHandler(Socket socket, RSAKeyPairGenerator pair) {
            this.clientSocket = socket;
            this.pair= pair;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                while (true) {
                    // Se envia al cliente la informacion encriptado con la clave publica.
                    out.println(encrypt("Elige una opción:   1. Calculadora   2. Consultar historial    3. Cerrar conexión", pair.getPublicKey())); 
                    
                    // Recive del cliente y usamos el metodo de desencriptacion con la clave privada.
                    String opcion = decrypt(in.readLine(), pair.getPrivateKey());
                    logger.info("Cliente seleccionó la opción: " + opcion);
                    switch (opcion) {
                        case "1":
                            logger.info("Opción 1 elegida. Calculando...");
                            String operacion = decrypt(in.readLine(), pair.getPrivateKey());   
                            logger.info("Operación solicitada por el cliente: " + operacion);
                            // Recibir y enviar el resultado al cliente
                            String resultado = new PowerCalculator(operacion).compact().verbose().toString();
                            if (resultado != null) {    // Compruevo que la operacion sea valida.
                                out.println(encrypt(resultado, pair.getPublicKey()));
                                historial.add(resultado);
                                logger.info("Resultado enviado al cliente: " + resultado);
                                logger.info("Operación agregada al historial");
                            } else {
                                // Enviar mensaje de error al cliente
                               out.println(encrypt("Error: operación no válida", pair.getPublicKey()));
                                logger.warning("Operación no válida solicitada por el cliente: " + operacion);
                            }
                            break;
                        case "2":
                            logger.info("Opción 2 elegida. Consultando historial...");

                            if (historial.isEmpty()) {      // Compruevo si la lista del historial esta vacia.
                                out.println(encrypt("Historial vacío.", pair.getPublicKey()));
                                logger.info("Historial vacío consultado por el cliente");
                            } else {
                                // Enviar el historial al cliente
                                out.println(encrypt("Historial de operaciones:", pair.getPublicKey()));
                                for (int i = 0; i < historial.size(); i++) {
                                    out.println(encrypt((i + 1) + ". " + historial.get(i), pair.getPublicKey()));
                                }
                                logger.info("Historial consultado por el cliente");
                            }
                            out.println(encrypt("", pair.getPublicKey()));
                            break;
                        case "3":
                            logger.info("Cliente solicitó cerrar la conexión.");
                            return;  // Salir del bucle para cerrar la conexión
                        default:
                            logger.warning("Cliente seleccionó una opción no válida: " + opcion);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, "Excepción en el manejo del cliente", e);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ServerCalculadora.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(ServerCalculadora.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ServerCalculadora.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, "Excepción al cerrar el socket del cliente", e);
                }
            }
        }
    }
    
    public static String encrypt(String plainText, PublicKey key) throws Exception {      // Metodo para enviar la informacion encriptada
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String cipherText, PrivateKey key) throws Exception {      // Metodo para desencriptar la informacion recibida
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decryptedBytes);
    }

    // Metodos del server para realizar las operaciones matematicas-
    static class PowerCalculator {
        // Comprueva y reescala la operacion(espacios, signos, etc.)
        private static final List<Pattern> PATTERNS;
        private static final Map<Character, ToLongBiFunction<Long, Long>> OPERATIONS;
        private static final String PARENTHESES
                = "(?<before>.*?)\\((?<inside>[^()]+)\\)(?<after>.*)";
        private static final String EXPRESSION
                = "(?<before>.*?)(?<x>(^-)?\\d+)(?: *)(?<op>[%s])(?: *)(?<y>-?\\d+)(?<after>.*)";
        private static final String SQRT_EXPRESSION
                = "(?<before>.*?)sqrt\\((?<inside>-?\\d+)\\)(?<after>.*)";
        
        private boolean isVerbose; // Ahora es una variable de instancia

        static {
            final String[] priority = new String[]{"\\^", "*/%", "+-"}; // Asigna el orden de las operaciones

            PATTERNS = new ArrayList<>();
            PATTERNS.add(Pattern.compile(PARENTHESES));
            PATTERNS.add(Pattern.compile(SQRT_EXPRESSION));
            for (final var operations : priority) {
                PATTERNS.add(Pattern.compile(String.format(EXPRESSION, operations)));
            }

            OPERATIONS = Map.of(
                '+', (a, b) -> a + b,
                '-', (a, b) -> a - b,
                '*', (a, b) -> a * b,
                '/', (a, b) -> a / b,
                '%', (a, b) -> a % b,
                '^', (a, b) -> (long) Math.pow(a, b));
        }

        long calculate(String expression) {
            if (isVerbose) {
                System.out.println(expression);
            }

            // Verificar si la expresión es una llamada a sqrt()
            Matcher sqrtMatcher = Pattern.compile("sqrt\\((\\d+)\\)").matcher(expression);
            if (sqrtMatcher.matches()) {
                // Extraer el argumento de la función sqrt()
                String inside = sqrtMatcher.group(1);
                // Convertir el argumento a un número y calcular la raíz cuadrada
                long result = (long) Math.sqrt(Long.parseLong(inside));
                // Retornar el resultado
                return result;
            }

            for (final var pattern : PATTERNS) {
                final var m = pattern.matcher(expression);
                if (!m.matches()) {
                    continue;
                }

                final long result;
                final var isParentheses = m.groupCount() == 3;

                if (isParentheses) {
                    result = calculate(m.group("inside"));
                } else if (pattern.pattern().equals(SQRT_EXPRESSION)) {
                    final var inside = Long.parseLong(m.group("inside"));
                    result = (long) Math.sqrt(inside);
                } else {
                    final var op = m.group("op").charAt(0);
                    final var x = Long.valueOf(m.group("x"));
                    final var y = Long.valueOf(m.group("y"));

                    result = OPERATIONS.get(op).applyAsLong(x, y);
                }
                return calculate(m.group("before") + result + m.group("after"));
            }
            return Long.parseLong(expression);
        }

        private String expression;
        private boolean invertSign;

        PowerCalculator(String expression) {
            this.expression = expression;
        }

        long calculate() {
            long result = calculate(this.expression);   // invierte el signo de positivo a negativo y viceversa.
            if (invertSign) {
                result *= -1;
            }
            return result;
        }

        PowerCalculator verbose() {
            this.isVerbose = true; // Configurando isVerbose para true
            return this;
        }

        PowerCalculator compact() {
            expression = expression.replaceAll(" +", "");
            return this;
        }

        public String getExpression() {
            return this.expression;
        }

        public PowerCalculator setExpression(String expression) {
            this.expression = expression;
            return this;
        }

        @Override
        public String toString() {
            return this.expression + " = " + this.calculate();  // Muestra el resultado en cadena de texto.
        }
    }
}