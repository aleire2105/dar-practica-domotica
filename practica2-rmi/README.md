
# Práctica 2 - Sistema Domótico con Java RMI

## Descripción

Implementación de un sistema domótico distribuido utilizando Java RMI (Remote Method Invocation).

El sistema sigue una arquitectura cliente-servidor donde un objeto remoto centralizado gestiona el estado de todos los dispositivos de la vivienda. Los clientes pueden conectarse remotamente, autenticarse y realizar operaciones de consulta y control sobre los dispositivos.
## Estructura del proyecto

```text
practica2-rmi/
└── src/
    ├── client/
    │   └── MainClienteRMI.java
    │
    ├── common/
    │   ├── Dispositivo.java
    │   ├── DomoticaException.java
    │   └── DomoticaInterfaz.java
    │
    └── server/
        ├── DomoticaImplement.java
        └── MainServidor.java
```
## Funcionalidades implementadas

* Autenticación mediante hash SHA-256
* Listado de dispositivos
* Consulta de estado
* Modificación de dispositivos
* Gestión de errores mediante excepciones personalizadas
* Comunicación remota mediante Java RMI
* Acceso concurrente mediante `ConcurrentHashMap`
* Exclusión mutua en operaciones críticas con `synchronized`

## Dispositivos disponibles

| ID   | Tipo          | Valores válidos |
| ---- | ------------- | --------------- |
| LUC1 | Luz           | ON / OFF        |
| CLI1 | Climatización | 20 - 30         |
| PER1 | Persiana      | 0 - 100         |

## Comandos del cliente
login <usuario> <password>
list
status <ID>
set <ID> <valor>
logout
exit
## Ejemplo de uso
> login jorge pass123
Login correcto.

> list
PER1 (Persiana) -> 0
LUC1 (Luz) -> OFF
CLI1 (Climatizacion) -> 22

> set LUC1 ON

## Gestión de errores

El sistema utiliza excepciones personalizadas mediante `DomoticaException`.

Errores implementados:

* 401 NOT_AUTHENTICATED
* 404 DEVICE_NOT_FOUND
* 422 INVALID_VALUE

## Aspectos técnicos

* El servidor publica el objeto remoto en el puerto `1099`
* Los objetos `Dispositivo` son serializables
* RMI gestiona automáticamente la comunicación cliente-servidor
* Las operaciones críticas se sincronizan para evitar modificaciones concurrentes inconsistentes


