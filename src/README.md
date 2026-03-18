# Proyecto de Domótica - Desarrollo de Aplicaciones en Red

## 1. Especificación Formal del Protocolo (ABNF)

A continuación se detalla la especificación formal del protocolo de capa de aplicación diseñado para este proyecto, utilizando ABNF. El protocolo está basado en texto y utiliza UTF-8 con delimitación por salto de línea (`CRLF`).

```abnf
; --- REGLAS GENERALES ---
mensaje = (peticion / respuesta) CRLF
CRLF = %d13.10 ; Retorno de carro y salto de línea (\r\n)
SP = %d32      ; Espacio en blanco

; --- 1. PETICIONES DEL CLIENTE ---
peticion = peticion-login / peticion-list / peticion-control / peticion-status / peticion-logout

peticion-login   = "REQ_LOGIN" SP usuario SP password
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
estado-num         = 1*3DIGIT ; Valores entre 0 y 100 (persianas) o 20 y 30 (clima)

ALPHA = %x41-5A / %x61-7A ; Letras A-Z, a-z
DIGIT = %x30-39           ; Números 0-9