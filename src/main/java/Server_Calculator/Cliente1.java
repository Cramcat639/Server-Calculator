package Server_Encrypt_RSA;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

// Operacion de prueva:  3^4+6*((9+12/6)*(24/2^3-1)).
public class Cliente1 {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, Exception {
        String hostName = "localhost";
        int puerto = 1234; // Mismo número de puerto que el servidor

        try (Socket socket = new Socket(hostName, puerto); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            
            
            //PRIVATE
            File filePrivate = new File("RSA_Private.key");
            DataInputStream disPrivate = new DataInputStream(new FileInputStream(filePrivate));    // Abrimos el archivo

            byte[] privKeyBytes = new byte[(int) filePrivate.length()];
            disPrivate.read(privKeyBytes);                                                  // Leemos el archivo
            disPrivate.close();

            // decode private key
            KeyFactory keyFactoryPrivate = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
            RSAPrivateKey privKey = (RSAPrivateKey) keyFactoryPrivate.generatePrivate(privSpec);            // Descodificamos el archivo

            //PUBLIC
            File filePublic = new File("RSA_Public.key");
            DataInputStream disPublic = new DataInputStream(new FileInputStream(filePublic));

            byte[] publicKeyBytes = new byte[(int) filePublic.length()];
            disPublic.read(publicKeyBytes);
            disPublic.close();

            // decode private key
            KeyFactory keyFactoryPublic = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec specPublic = new X509EncodedKeySpec(publicKeyBytes);
            RSAPublicKey pubKey = (RSAPublicKey) keyFactoryPublic.generatePublic(specPublic);

            boolean continuar = true;
            while (continuar) {
                // Recibir y mostrar el menú del servidor
                String menu = decrypt(in.readLine(), privKey);          // Desencriptamos con la clave privada
                System.out.println("Menú del servidor: " + menu);

                // Leer la elección del usuario y enviarla al servidor
                System.out.print("Tu elección: ");
                String eleccion = stdIn.readLine();
                 out.println(encrypt(eleccion, pubKey));            // Encriptamos con la clave publica

                switch (eleccion) {
                    case "1":
                        while (true) {
                            System.out.print("Introduce la operación matemática: ");
                            String operacion = stdIn.readLine();
                            if (operacion.matches("[-+*/%^()\\d]+")) {
                                // La operación es numérica
                                out.println(encrypt(operacion, pubKey));
                                // Recibir e imprimir el resultado desde el servidor
                                String resultado = decrypt(in.readLine(), privKey);
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
                        while (!(historialMsg = decrypt(in.readLine(), privKey)).isEmpty()) {
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

    public static String encrypt(String plainText, RSAPublicKey key) throws Exception {         // Metodo para enviar la informacion encriptada
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String cipherText, RSAPrivateKey key) throws Exception {       // Metodo para desencriptar la informacion recibida
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(decryptedBytes);
    }
}
