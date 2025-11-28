# HacomOrderProcessing - Evaluación  Técnica
#### José  Basurto (basurtogarciajose@gmail.com)

Sistema  completo  de  procesamiento  de  órdenes que  integra:
* Spring Boot WebFlux (reactivo)
* gRPC
* Akka Classic Actors
* MongoDB Reactive
* SMPP (Cloudhopper) para  envío  de SMS
* Prometheus Actuator Metrics
* Swagger/OpenAPI para  documentación REST
* Log4j2 YAML
* Docker (opcional)

## Tabla de Contenido
1. **Descripción General**
2. **Tecnologías Utilizadas**
3. **Requisitos del Proyecto**
4. **Estructura del proyecto**
5. **Configuración**
6. **Ejecución del Proyecto (Modo 1 — Gradle)**
7. **Ejecución del Proyecto (Modo 2 — Docker)**
8. **Pruebas**
  

## 1. Descripción General
Este  proyecto  implementa  un  flujo  de  procesamiento  de  pedidos  totalmente  reactivo y asíncrono.

1. Un  cliente  envía  un  pedido  vía gRPC.
2. Un Actor de  Akka  recibe  la  petición y procesa  la  orden.
3. El actor guarda  la  información  en MongoDB Reactive.
4. Cuando  termina, envía  un SMS por SMPP al  cliente.
5. Existen API REST para:
	* Consultar el estatus  de  un  pedido.
	* Consultar el número total de  pedidos  dentro  de  un  rango  de  fechas.
6. Se  exponen  métricas  Prometheus a través  de Actuator.

Todo el sistema  puede  ejecutarse  tanto  con  **Gradle**  como  dentro  de  un  contenedor **Docker**.

## 2. Tecnologías Utilizadas
|Tecnología|Uso|
|--|--|
|Java 17|Lenguaje base|
|Spring Boot WebFlux|API reactiva|
|Spring Data Reactive MongoDB|Persistencia|
|gRPC + Protobuf|RPC para crear órdenes|
|Akka Classic|Actor system para orquestación|
|Cloudhopper SMPP|Enviar SMS|
|Log4j2 YAML|Logging|
|Spring Actuator + Prometheus|Métricas
|Docker|Distribución opcional|
|Swagger/OpenAPI|Documentación REST|

## 3. Requisitos del Proyecto
* Proyecto con Spring Boot, Java 17, Gradle
* Spring WebFlux + Mongo Reactive + Log4j2 YML + Actuator
* application.yml en lugar de properties
* Configuración programática:
	* Puerto WebFlux
	* Conexión MongoDB
* Servicio gRPC con inserción de órdenes
* Actor Akka que procesa la orden y devuelve la respuesta gRPC
* Inserción MongoDB
* Envío de SMS vía SMPP
* API REST:
	* /orders/{id} : estado de la orden
	* /orders?start=&end= : total de órdenes
* Logs en puntos clave
* Métrica Prometheus: orders_total

## 4. Estructura del proyecto
Se ha implementado una arquitectura hexagonal para este proyecto porque encaja muy bien con la idea central del ejercicio: integrar muchas tecnologías distintas (gRPC, Akka, MongoDB, SMPP, WebFlux, Actuator) sin que el núcleo de negocio dependa directamente de ninguna de ellas.

HacomOrderProcessing/
│── src/
│   │── main/
│   │   │── java/com/hacom/orderprocessing/
│   │   │   │── domain
│   │   │   │   │── model
│   │   │   │      │── repository
│   │   │   │── infrastructure
│   │   │   │    │── actor
│   │   │   │    │── config
│   │   │   │    │── grpc
│   │   │   │    │── persistence
│   │   │   │    │── rest
│   │   │   │    │  │── dto
│   │   │   │    │  │── smpp
│   │   │── proto/order.proto
│   │   │── resources/
│   │   │  │── application.yml
│   │   │  │── log4j2.yml
│── build.gradle
│── settings.gradle
│── Dockerfile
│── README.md

## 5. Configuración
>```mongodbDatabase: "exampleDb"
>mongodbUri: "mongodb://127.0.0.1:27017"
>apiPort: 9898
>grpcPort: 6565
>smpp:
>  enabled: true
>  host: "127.0.0.1"
>  port: 2775
>  systemId: "smppclient1"
>  password: "password"
>  source: "OrderProcessingAPI"```
  
MongoDB y WebFlux se configuran programáticamente, NO con auto-configuración de Spring.

## 6. Ejecución del Proyecto (Modo 1 — Gradle)
Requisitos:
* Java 17
* MongoDB ejecutándose localmente
* Servidor SMPP

### 1. Construir
>```./gradlew clean build```
### 2. Ejecutar
>```./gradlew bootRun```
### 3. Ver logs
>```tail -f build/logs/*.log```

## 7. Ejecución del Proyecto (Modo 2 — Docker)
El proyecto incluye un Dockerfile listo para producción.

### 1. Construir imagen
>```docker build -t orderprocessing-app .```
### 2. Ejecutar contenedor
>```docker run -d \
>--name orderprocessing \
>-p 9898:9898 \
>-p 6565:6565 \
>-e mongodbUri="mongodb://host.docker.internal:27017" \
>-e smpp_enabled=true \
>-e smpp_host="host.docker.internal" \
>-e smpp_port=2775 \
>orderprocessing-app```
### 3. Ver logs del contenedor
>```docker logs -f orderprocessing```
### 4. Detener contenedor
>```docker stop orderprocessing```

## 8. Pruebas
### 1. Crear orden via gRPC

>```grpcurl -plaintext \
>  -import-path src/main/proto \
>        -proto order.proto \
>          -d '{
>                   "orderId": "A1001",
>                   "customerId": "C900",
>                   "customerPhoneNumber": "5551112222",
>                   "items": { "items": ["item1", "item2"] }
>                }' \
>           localhost:6565 \
>           order.OrderService/CreateOrder```
### 2. API REST — Obtener estatus de orden
>```curl http://localhost:9898/orders/A1001```
### 3. API REST — Contar órdenes por rango
>```curl "http://localhost:9898/orders?start=2025-02-14T00:00:00Z&end=2025-02-16T00:00:00Z"```
### 4. Prometheus Metrics
>```curl http://localhost:9898/actuator/prometheus | grep orders_total```
### 5. Swagger UI (solo API REST)
>```http://localhost:9898/swagger-ui.html```
