# Proyecto de Domótica - Desarrollo de Aplicaciones en Red

## 1. Estructura e implementación del código

La implementación del proyecto se ha dividido en dos partes principales: cliente y servidor. El servidor se encarga de aceptar conexiones, autenticar usuarios y procesar las peticiones del protocolo, mientras que el cliente permite al usuario introducir comandos por consola y mostrar las respuestas recibidas.
### 1.1 Cliente

El cliente está implementado en la clase `MainCliente`. Su función es establecer la conexión TCP con el servidor, leer comandos introducidos por el usuario desde teclado y enviarlos al servidor en formato de texto. Después de cada envío, espera la respuesta correspondiente y la muestra por pantalla.

### 1.2 Servidor

El servidor está compuesto por las clases `MainServidor` y `ManejadorCliente`.

La clase `MainServidor` es la encargada de iniciar el servicio en el puerto configurado y permanecer a la espera de conexiones entrantes. Cada vez que un cliente se conecta, el servidor acepta la conexión y crea un nuevo hilo de ejecución asociado a un objeto `ManejadorCliente`, lo que permite atender a varios clientes de forma concurrente.

La clase `ManejadorCliente` implementa la lógica principal del protocolo. En ella se recibe cada mensaje enviado por el cliente, se separa en tokens y se identifica el comando solicitado. A partir de este análisis, el servidor ejecuta la operación correspondiente y genera la respuesta adecuada según las reglas del protocolo.

Además, el servidor mantiene un inventario compartido de dispositivos mediante una estructura `ConcurrentHashMap`, donde se almacenan los identificadores de los dispositivos y sus estados actuales. Esta estructura permite gestionar de manera segura el acceso concurrente desde varios hilos.

### 1.3 Procesamiento de comandos

El comando `REQ_LOGIN` permite autenticar al usuario. Si la contraseña recibida coincide con la esperada por el servidor, el cliente pasa a estado autenticado y puede utilizar el resto de funcionalidades.

El comando `REQ_LIST` devuelve el inventario completo de dispositivos y sus estados. El comando `REQ_STATUS` permite consultar el valor actual de un dispositivo concreto. Por su parte, `CMD_CONTROL` permite modificar el estado de un dispositivo, siempre que exista y que el valor enviado sea válido según su tipo.

Finalmente, el comando `REQ_LOGOUT` cierra la sesión del cliente, enviando una respuesta de confirmación y cerrando el socket asociado.

## 2. Gestión de errores

El protocolo implementa un sistema de gestión de errores basado en respuestas del tipo `ACK_ERR`, que incluyen un código numérico y un motivo descriptivo. Este mecanismo permite al cliente identificar de forma clara la causa del error y actuar en consecuencia.

La estructura general de un mensaje de error es la siguiente:


ACK_ERR <codigo-error> <motivo-error>


A continuación se describen los distintos códigos de error contemplados en el sistema:

### 400 - FORMATO_INCORRECTO

Este error se produce cuando la petición enviada por el cliente no cumple con la sintaxis definida en el protocolo. Puede deberse a un número incorrecto de parámetros, palabras clave mal escritas o formato inválido del mensaje.

### 401 - NOT_AUTHENTICATED

Indica que el cliente está intentando realizar una operación sin haber completado previamente el proceso de autenticación. Este error se devuelve en peticiones como `REQ_LIST`, `REQ_STATUS` o `CMD_CONTROL` si el usuario no ha iniciado sesión correctamente.

### 404 - DEVICE_NOT_FOUND

Se genera cuando el cliente hace referencia a un identificador de dispositivo que no existe en el sistema. Este error puede aparecer en peticiones como `REQ_STATUS` o `CMD_CONTROL`.

### 422 - INVALID_VALUE

Este error ocurre cuando el valor proporcionado para un dispositivo no es válido. Por ejemplo, al intentar asignar un estado incorrecto a una luz (distinto de `ON` o `OFF`), o un valor fuera de rango en dispositivos como climatización o persianas.

### 400 - COMANDO_DESCONOCIDO

Se produce cuando el cliente envía una petición que no corresponde a ninguno de los comandos definidos en el protocolo. En este caso, el servidor no puede interpretar la solicitud y responde con este error.

## 3. Especificación Formal del Protocolo (ABNF)

A continuación se detalla la especificación formal del protocolo de capa de aplicación diseñado para este proyecto, utilizando ABNF. El protocolo está basado en texto y utiliza UTF-8 con delimitación por salto de línea (`CRLF`).

```abnf
; --- REGLAS GENERALES ---
mensaje = (peticion / respuesta) CRLF
CRLF = %d13.10 ; Retorno de carro y salto de línea (\r\n)
SP = %d32      ; Espacio en blanco

; --- 1. PETICIONES DEL CLIENTE ---
peticion = peticion-login / peticion-list / peticion-control / peticion-status / peticion-logout

peticion-login = "REQ_LOGIN" SP usuario SP hash
hash = 1*(ALPHA / DIGIT)
peticion-list    = "REQ_LIST"
peticion-control = "CMD_CONTROL" SP id-dispositivo SP "SET" SP valor
peticion-status  = "REQ_STATUS" SP id-dispositivo
peticion-logout  = "REQ_LOGOUT"

; --- 2. RESPUESTAS DEL SERVIDOR ---
respuesta = res-login / res-list / res-control / res-status / res-logout / res-error

res-login   = "RES_LOGIN_OK" SP usuario
res-list    = "RES_LIST" *(SP estado-dispositivo)
res-control = "ACK_OK" SP id-dispositivo SP valor
res-status  = "RES_STATUS" SP id-dispositivo SP valor
res-logout  = "RES_LOGOUT_OK"

; --- 3. GESTIÓN DE ERRORES ---
res-error = "ACK_ERR" SP codigo-error SP motivo-error
codigo-error = "400" / "401" / "404" / "422"
motivo-error = "FORMATO_INCORRECTO" / "NOT_AUTHENTICATED" / "DEVICE_NOT_FOUND" / "INVALID_VALUE" / "COMANDO_DESCONOCIDO"

; --- 4. TIPOS DE DATOS BASE ---
usuario            = 1*ALPHA
password           = 1*(ALPHA / DIGIT)
id-dispositivo     = prefijo-disp 1*DIGIT
prefijo-disp       = "LUC" / "CLI" / "PER"
estado-dispositivo = id-dispositivo ":" valor
valor              = estado-luz / estado-num
estado-luz         = "ON" / "OFF"
estado-num         = 1*3DIGIT

ALPHA = %x41-5A / %x61-7A
DIGIT = %x30-39 de cierre de sesión, responde al cliente y cierra la conexión asociada.
```


## 4. Diagrama de secuencia del protocolo

Además de la especificación formal en ABNF, se incluye un diagrama de secuencia que representa el flujo principal de interacción entre cliente y servidor. Este diagrama permite visualizar de forma clara el intercambio de mensajes durante las fases de conexión, autenticación, consulta de dispositivos, control de actuadores y cierre de sesión.

<img width="440" height="814" alt="XP9DQiCm44Rt1jrXbkt2j8Mk0Yu9ibk2tQYrdRKHiWW3jcHaEVHUEK8kLgbnVXQDlPutxuDlPjbRRkpGDYImQ1i9XJHx5AWXrktnO55e2256lZEyvL1xG0nI2UJma67ReNua0GcuiDe0RL0gAqdGEQcpT5pPax7MncDH15Gh-OF7" src="https://github.com/user-attachments/assets/c7467c0b-f71e-49ac-9aac-248fc729005d" />

El diagrama de secuencia representa de forma gráfica el intercambio de mensajes entre el cliente y el servidor durante una sesión completa de comunicación. En él se puede observar el orden temporal en el que se producen las distintas peticiones y respuestas definidas en el protocolo, desde el establecimiento de la conexión hasta el cierre de la sesión.

En primer lugar, el cliente establece una conexión TCP con el servidor. Una vez aceptada dicha conexión, comienza la fase de autenticación mediante el envío de la petición `REQ_LOGIN`, que incluye el nombre de usuario y la contraseña. Si las credenciales son válidas, el servidor responde con `RES_LOGIN_OK`, permitiendo continuar con la interacción. En caso contrario, devuelve un mensaje de error `ACK_ERR 401 NOT_AUTHENTICATED`.

Tras una autenticación satisfactoria, el cliente puede realizar las principales operaciones contempladas en el protocolo. Entre ellas se encuentran la solicitud del listado de dispositivos mediante `REQ_LIST`, la consulta del estado de un dispositivo concreto con `REQ_STATUS` y el envío de órdenes de control mediante `CMD_CONTROL`. Para cada una de estas peticiones, el servidor genera la respuesta correspondiente, ya sea devolviendo la información solicitada o confirmando la actualización realizada.

Además, el diagrama también refleja la posibilidad de que se produzcan errores durante la interacción, como intentos de acceso sin autenticación, referencias a dispositivos inexistentes o envío de valores no válidos. En estos casos, el servidor responde con mensajes de error `ACK_ERR`, acompañados del código y motivo correspondiente.


## 5. Diagramas de estados

Para complementar la especificación del protocolo, se incluyen dos diagramas de estados: uno correspondiente al cliente y otro al servidor. Estos diagramas describen la evolución interna de ambos extremos de la comunicación en función de los eventos y mensajes intercambiados.

### 5.1 Diagrama de estados del cliente

El cliente pasa por los estados de desconectado, conectado y autenticado, pudiendo realizar distintas operaciones una vez validado por el servidor, hasta finalizar la sesión con la petición de logout.

<img width="651" height="441" alt="cliente" src="https://github.com/user-attachments/assets/7ac64d89-e000-4ee7-b44d-10a309b32e46" />

El cliente inicia la comunicación estableciendo una conexión TCP con el servidor. Una vez conectado, debe autenticarse mediante el envío de la petición `REQ_LOGIN`, proporcionando un usuario y una contraseña válidos. Si la autenticación es correcta, el servidor responde con `RES_LOGIN_OK` y el cliente pasa al estado autenticado.

A partir de este momento, el cliente puede interactuar con el sistema domótico mediante distintas operaciones. Puede solicitar el listado completo de dispositivos con `REQ_LIST`, consultar el estado de un dispositivo concreto mediante `REQ_STATUS`, o modificar el estado de un dispositivo con `CMD_CONTROL`. Todas estas peticiones reciben una respuesta del servidor, que puede ser de éxito (por ejemplo, `RES_LIST`, `RES_STATUS` o `ACK_OK`) o de error (`ACK_ERR`) en caso de que ocurra algún problema, como falta de autenticación, dispositivo inexistente o valores inválidos.

Finalmente, el cliente puede finalizar la sesión enviando la petición `REQ_LOGOUT`. El servidor responde con `RES_LOGOUT_OK` y se cierra la conexión TCP, dando por terminada la comunicación.


### 5.2 Diagrama de estados del servidor

El servidor parte del estado de espera de conexión, acepta clientes entrantes y, tras una autenticación correcta, atiende las peticiones de consulta y control de dispositivos. Finalmente, al recibir la orden de cierre de sesión, responde al cliente y cierra la conexión asociada.
<img width="724" height="441" alt="servidor" src="https://github.com/user-attachments/assets/95741085-1df2-42c1-a836-879226e55f9c" />

El servidor inicia su ejecución creando un socket de escucha en el puerto definido y permanece en estado de espera de conexiones entrantes. Cuando un cliente se conecta, el servidor acepta la conexión y crea un manejador dedicado para gestionar la comunicación con dicho cliente.

Una vez establecida la conexión, el servidor espera la recepción de peticiones por parte del cliente. En primer lugar, el cliente debe autenticarse mediante la petición `REQ_LOGIN`. El servidor valida las credenciales y, en caso de ser correctas, responde con `RES_LOGIN_OK`, permitiendo al cliente acceder al resto de funcionalidades. Si la autenticación falla, el servidor devuelve un mensaje de error (`ACK_ERR 401 NOT_AUTHENTICATED`) y el cliente permanece sin autenticar.

Cuando el cliente está autenticado, el servidor procesa las distintas peticiones que puede recibir. Ante una petición `REQ_LIST`, devuelve el listado de dispositivos con sus estados actuales (`RES_LIST`). Para una petición `REQ_STATUS`, comprueba la existencia del dispositivo solicitado y responde con su estado (`RES_STATUS`) o con un error (`ACK_ERR 404 DEVICE_NOT_FOUND`) si no existe. En el caso de `CMD_CONTROL`, valida tanto la existencia del dispositivo como el valor proporcionado, actualizando su estado si es correcto (`ACK_OK`) o devolviendo un error (`ACK_ERR 422 INVALID_VALUE`) en caso contrario.

En cualquier momento, si el cliente envía una petición con formato incorrecto o un comando desconocido, el servidor responde con el código de error correspondiente (`ACK_ERR 400`). Además, si el cliente intenta realizar operaciones sin estar autenticado, se devuelve un error de autorización (`ACK_ERR 401`).



## 6. Análisis de Tráfico de Red (Wireshark)

En este repositorio se incluye el archivo `captura_domótica.pcap` que contiene la traza de red generada durante una ejecución estándar del cliente y el servidor.

Al abrir el archivo en Wireshark (filtrando por `tcp.port == 8080`), se pueden observar claramente las tres fases de la comunicación:

1. **Establecimiento de la conexión (Three-way Handshake):**
   Se observa el intercambio inicial de paquetes TCP (`SYN`, `SYN-ACK`, `ACK`) entre la IP del cliente y la IP del servidor en el puerto 8080, confirmando la apertura del Socket.

2. **Intercambio de mensajes del protocolo de aplicación:**
   Mediante los paquetes marcados con el flag `PSH` (Push), se observa la transferencia de datos en texto plano. En la inspección de la carga útil (Payload) de estos paquetes se pueden identificar los comandos de nuestro protocolo, tales como la solicitud de autenticación (`REQ_LOGIN`) y las respuestas del servidor.

3. **Cierre de la comunicación:**
   Al finalizar la ejecución del cliente, se observa el intercambio de paquetes con el flag `FIN` (`FIN, ACK` -> `ACK`), que demuestra el cierre ordenado y la liberación de los recursos (cierre del Socket) por ambas partes.
