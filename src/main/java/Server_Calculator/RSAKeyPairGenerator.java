package Server_Encrypt_RSA;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.FileOutputStream;

public class RSAKeyPairGenerator {

    private PublicKey publicKey;            //Declaramos una clave publica
    private PrivateKey privateKey;          //Declaramos una clave privada

    public RSAKeyPairGenerator() throws Exception {                     // Metodo para generar las claves
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        writeToFile("RSA_Public.key", getPublicKey().getEncoded());         // Se genera los archivos, uno para cada clave..
        writeToFile("RSA_Private.key",getPrivateKey().getEncoded());       //... con el nombre y en la raiz del proyecto. 
    }                                                                                                                          // Contenido encriptado.

    public void writeToFile(String path, byte[] key) throws Exception {         // Metodo escribir las claves en un fichero
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(key);
            fos.flush();
        }
    }

    public PublicKey getPublicKey() {           // Metodo para obtener la clave publica
        return publicKey;
    }

    public PrivateKey getPrivateKey() {         // Metodo para obtener la clave privada
        return privateKey;
    }
}
