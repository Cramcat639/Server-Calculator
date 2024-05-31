# Server Calculadora Multi-Cliente en Hilos con JAVA y encriptación RSA

¡Bienvenido al repositorio de la aplicación Cliente-Servidor de una Calculadora! Este proyecto permite realizar operaciones matemáticas avanzadas y mantener un historial de operaciones, utilizando una conexión segura cliente-servidor y una interfaz de usuario mejorada.

## Características Principales

- **Operaciones Matemáticas Avanzadas:**
  - Realiza operaciones matemáticas complejas con facilidad y precisión.
  
- **Historial de Operaciones:**
  - Mantén un registro de todas las operaciones realizadas para revisar y consultar cuando lo necesites.
  
- **Conexión Segura Cliente-Servidor:**
  - Implementamos una conexión segura para garantizar la integridad y confidencialidad de los datos.
  
- **Interfaz de Usuario Mejorada:**
  - Experiencia más intuitiva y agradable gracias a la interfaz actualizada.
  
- **Funcionalidad Offline:**
  - Realiza operaciones sin conexión a Internet, mejorando la accesibilidad y la fiabilidad.
  
- **Sistema de Logs:**
  - Monitorea y registra todas las actividades y errores, mejorando la capacidad de depuración y mantenimiento.
  
- **Estructura Cliente-Servidor con Hilos:**
  - Manejo de múltiples clientes simultáneamente utilizando hilos, garantizando un rendimiento óptimo.
  
- **Sistema de Encriptación Asimétrica RSA:**
  - Implementación de encriptación RSA. Cada vez que se inicia el servidor, se crea un Keystore con claves pública y privada para una mayor seguridad en la comunicación de datos.

## Requisitos

- Java JDK 17 o superior
- Maven 3.6.0 o superior

## Instalación y Ejecución

1. Clona el repositorio:
    ```sh
    git clone https://github.com/Cramcat639/Server-Calculator.git
    cd server-calculadora
    ```

2. Compila y construye el proyecto usando Maven:
    ```sh
    mvn clean install
    ```

3. Ejecuta el servidor:
    ```sh
    java -jar target/server-calculadora-2.0.0.jar
    ```

4. Ejecuta el cliente:
    ```sh
    java -jar target/client-calculadora-2.0.0.jar
    ```

## Uso

Una vez iniciado el servidor, puedes conectar múltiples clientes que podrán realizar operaciones matemáticas. El servidor registrará todas las actividades y errores en los logs, y las operaciones serán encriptadas utilizando el sistema RSA.

## Contribuciones

Las contribuciones son bienvenidas. Si tienes sugerencias o encuentras algún problema, por favor abre un issue o un pull request.

## Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

## Contacto

Cramcat639 - [marc639@outlook.com](mailto:marc639@outlook.com)

¡Gracias por tu interés en nuestro proyecto! Esperamos que lo disfrutes y estamos ansiosos de escuchar tus comentarios y sugerencias.
