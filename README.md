# Cómo crear y desplegar microservicios con Spring Boot, Spring Cloud Netflix y Docker

En este tutorial vamos a aprender cómo crear microservicios con Spring Boot, cómo configurar servicios que utilicen los componentes Eureka y Zuul de Spring Cloud Netflix y cómo desplegarlos en contenedores Docker.

## Índice de contenidos
 - [Introducción](#introduccion)
 - [Entorno](#entorno)
 - [Servicio de Spring Cloud Netflix: Eureka](#eureka)
 - [Servicio de Spring Cloud Netflix: Zuul](#zuul)
 - [Microservicio  de ejemplo](#microservicio)
 - [Creación de imágenes](#imagenes)
 - [Creación de contenedores y despliegue](#contenedores)
 - [Conclusiones](#conclusiones)
 - [Referencias](#referencias)

<a name="introduccion"></a>
## Introducción

La arquitectura de microservicios es una arquitectura que recientemente ha ganado mucha popularidad, existiendo casos de éxito reconocidos mundialmente, como Netflix o Amazon. Esta arquitectura conlleva un gran esfuerzo, pero para aplicaciones que necesiten ser escalables y muy flexibles es una arquitectura ideal. Si quieres saber más sobre esta arquitectura, puedes consultar esta <a href="https://www.adictosaltrabajo.com/2016/03/25/introduccion-a-los-microservicios/" target="_blank">guía</a>.

En primer lugar, vamos a explorar cómo Spring Cloud Netflix nos ayuda a aplicar patrones fundamentales en la arquitectura de microservicios, como lo son el patrón de Descubrimiento de Servicios (mediante Eureka) y el patrón de API Gateway (mediante Zuul). Si quieres leer más información al respecto de Spring Cloud Netflix, puedes consultar este <a href="https://www.adictosaltrabajo.com/2017/04/26/introduccion-a-la-gestion-de-servicios-web-con-spring-cloud-y-netflix-oss/" target="_blank">tutorial</a> de nuestro compañero Jose Mangialomini.

Una vez creados estos servicios, los desplegaremos utilizando Docker. Esta parte es el motivo detrás de este tutorial, ya que hice un Trabajo de Fin de Grado sobre esta arquitectura, en el que me quedé con ganas de desplegar los servicios utilizando Docker. Si os pica la curiosidad podéis verlo <a href="https://github.com/SamuelGarciaG/TFG-Microservices-Netflix" target="_blank">aquí</a>. Dicho esto, vamos a por ello.

<a name="entorno"></a>
## Entorno

- **Hardware:** MacBook Pro 13' (2,6GHz IntelCore i5, 8GB DDR3 RAM)
- **Sistema Operativo:** MacOS Catalina 10.15.7
- **Maven:** Versión 3.6.3
- **Java**: Versión 1.8.0_261
- **Docker Engine**: Versión 19.03.13

<a name="eureka"></a>
## Servicio de Spring Cloud Netflix: Eureka

Primero vamos a crear el servicio Eureka, que actuará como un servidor en el que se registrarán todas las instancias de microservicios que despleguemos.

Crearemos los proyectos de Spring Boot con <a href="https://start.spring.io/" target="_blank">Spring Initializr</a>, que es una herramienta que nos creará el esqueleto del proyecto, junto con las dependencias que nosotros elijamos.  La configuración en Spring Initializr debe ser la siguiente:

<img width="1280" alt="InitializrEureka" src="https://user-images.githubusercontent.com/47883616/101922505-72c8dc00-3bce-11eb-864d-2cde00aadefa.png">

Tras crearlo, lo descomprimimos y abrimos con nuestro IDE favorito. No hace falta que toquemos su pom.xml, ya que vendrá de serie con la configuración y dependencias que hemos definido, pero si se nos ha olvidado algo o queremos añadir cualquier cosa, siempre podremos hacerlo mayor sin problema.

Según la configuración que hemos elegido, debería quedar así:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.3.6.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.autentia</groupId>
	<artifactId>DockerSpringNetflix-ZuulService</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>DockerSpringNetflix-ZuulService</name>
	<description>Zuul service for our dockerized application</description>

	<properties>
		<java.version>1.8</java.version>
		<spring-cloud.version>Hoxton.SR9</spring-cloud.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
	
</project>
```

Tras comprobar que las dependencias están correctamente definidas, vamos a la clase main del proyecto, que tendremos que anotar con **@EnableEurekaServer** para activar la autoconfiguración como Servidor Eureka.

```java
package com.autentia.dockerspringnetflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class DockerSpringNetflixEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerSpringNetflixEurekaServerApplication.class, args);
	}

}

```

Tras habilitar la configuración de Eureka, definimos la configuración de la aplicación en el archivo application.yml. Este archivo está ubicado en la carpeta src/main/resources y originalmente se llamará application.properties, pero personalmente, prefiero la extensión .yml, ya que resulta más legible, pudiendo cambiarla simplemente renombrando el archivo.

En él, daremos nombre a la aplicación, configuraremos el puerto del servidor embebido Tomcat y las propiedades de configuración de Eureka, que en este caso son fetch-registry y register-with-eureka con valor false, para que no se auto-registre, ya que el servidor Eureka contiene también un cliente de Eureka dentro de sí mismo.

```.yml
spring:
  application:
    name: eureka-server

#Tomcat Embedded Server Configuration
server: 
  port: 8761

#Eureka Configuration Properties
eureka:
  client:
    registerWithEureka: false
    fetchRegistry: false
```

Una vez terminados todos estos pasos, procedemos a generar el fichero .jar de esta aplicación, que posteriormente incluiremos en un contenedor para poder ejecutarla. Ejecutamos el siguiente comando para generarlo:

```
mvn clean install
```

Es muy importante que siempre que realicemos algún cambio en la aplicación, volvamos a ejecutar este comando para actualizar el fichero .jar para poder disponer de esos cambios dentro de este fichero, ya que simplemente guardando los archivos no conseguiríamos esto.

<a name="zuul"></a>
## Servicio de Spring Cloud Netflix: Zuul

Tras crear el servicio Eureka, vamos a crear el servicio Zuul. Este servicio implementa el patrón de API Gateway, actuando como una "puerta" a través de la cuál entrarán las peticiones a nuestra aplicación, y distribuyéndolas a los servicios correspondientes.

Creamos el proyecto con Spring Initializr con la siguiente configuración y dependencias:

<img width="1280" alt="InitializrZuul" src="https://user-images.githubusercontent.com/47883616/101922756-c89d8400-3bce-11eb-9b5c-1c9f4c70f0e7.png">

Si nos damos cuenta, este proyecto tendrá la versión 2.3.6 de Spring Boot, pero en el de Eureka hemos elegido la 2.4, ya que Zuul no está disponible para esa versión. ¿Esto podría causar que nuestra aplicación no funcionase? Por supuesto que no, ya que aquí es donde entra en juego una de las características más atractivas de los microservicios, que es su independencia de la implementación en la comunicación mediante APIs de tipo REST. Eureka está construido con este tipo de API, por lo que puede recibir peticiones desde aplicaciones Java o no-Java, permitiéndonos registrar servicios de todo tipo. Puede parecer una diferencia muy sutil y casi imperceptible, pero es de una importancia vital aclarar este concepto de cara a desarrollar aplicaciones basadas en microservicios.

Una vez creado, lo abrimos con el IDE que queramos y procedemos a configurarlo. Antes de nada, nos aseguramos de que su pom.xml tenga el siguiente aspecto:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.3.6.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.autentia</groupId>
	<artifactId>DockerSpringNetflix-ZuulService</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>DockerSpringNetflix-ZuulService</name>
	<description>Zuul service for our dockerized application</description>

	<properties>
		<java.version>1.8</java.version>
		<spring-cloud.version>Hoxton.SR9</spring-cloud.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
```

El siguiente paso será añadir la anotación **@EnableZuulProxy**, que activará la configuración de Zuul para esta aplicación.

```java
package com.autentia.dockerspringnetflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class DockerSpringNetflixZuulServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerSpringNetflixZuulServiceApplication.class, args);
	}

}
```

Una vez activada la configuración, debemos definirla. Este servicio es bastante especial, ya que tenemos que registrar las rutas de los microservicios que queramos exponer en su configuración, además de registrarlo en Eureka. 

En este caso, definimos una ruta para el futuro microservicio que vamos a crear. La ruta de registro en eureka es un poco particular, ya que en los ejemplos habituales suele ser algo como localhost:8761, pero en nuestro caso, vamos a usar el nombre del servicio de eureka (porque localhost en Docker no nos vale). Haremos todo esto de la siguiente manera:

```.yml
spring:
  application:
    name: zuul-service

#Tomcat Embedded Server Configuration
server:
  port: 7000

#Zuul Configuration Properties
zuul:
  routes:
      serv-greet:
        path: /greeting-service/**
        serviceId: greeting-microservice

#Eureka Configuration Properties
eureka: 
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka
```

Al igual que en el anterior servicio, terminamos la creación de este generando el .jar:

```
mvn clean install
```

<a name="microservicio"></a>
## Microservicio  de ejemplo

Este servicio es un ejemplo muy simple, pero podríamos implementar cualquier cosa que nos imaginemos, teniendo siempre claro que los microservicios casi siempre deben tener una **única responsabilidad**.

Lo crearemos con la siguiente configuración y dependencias:

<img width="1280" alt="InitializrMicroservice" src="https://user-images.githubusercontent.com/47883616/101922851-e9fe7000-3bce-11eb-9c1e-d6c748f1a20d.png">

Lo descomprimimos y abrimos con el IDE que nos apetezca en ese momento y comenzamos a desarrollar la funcionalidad más simple del mundo. Su pom.xml debería quedar así:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.4.0</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.autentia</groupId>
	<artifactId>DockerSpringNetflix-GreetingMicroservice</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>DockerSpringNetflix-GreetingMicroservice</name>
	<description>Eureka Server for our dockerized application</description>

	<properties>
		<java.version>1.8</java.version>
		<spring-cloud.version>2020.0.0-M6</spring-cloud.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

	<repositories>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
		</repository>
	</repositories>

</project>
```

Una vez comprobado el pom.xml, debemos anotar la clase Main con **@EnableDiscoveryClient**, para que Eureka pueda registrar este servicio.

```java
package com.autentia.dockerspringnetflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DockerSpringNetflixGreetingMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerSpringNetflixGreetingMicroserviceApplication.class, args);
	}

}
```

Ahora vamos a desarrollar la funcionalidad de este microservicio, que será simplemente devolver una cadena que nos diga el puerto en el que está configurado su servidor embebido Tomcat.

Para ello, desarrollaremos un Controlador, una Interfaz de Servicio y una implementación de la misma, dentro del mismo paquete que la clase Main.

La interfaz debería quedar así:

```java
package com.autentia.dockerspringnetflix;

public interface GreetingService {

    String getServiceGreeting() throws Exception;

}
```

Una vez creada la interfaz, la implementaremos en otra clase:

```java
package com.autentia.dockerspringnetflix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

@Service
public class GreetingServiceImpl implements GreetingService {

    @Autowired
    private Environment env;

	@Override
	public String getServiceGreeting() throws Exception {
        String portValue = env.getProperty("server.port");
        String returnValue = "Something unexpected happened, no greeting for you";
        if(portValue!= null && !portValue.isEmpty()) {
            returnValue = new StringBuilder().append("Hello from port: ").append(portValue).append("\n").toString();
        }
    	return returnValue;
	}

}
```

Y por último, el controlador:

```java
package com.autentia.dockerspringnetflix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
	
	GreetingService greetingService;
	
	public GreetingController(@Autowired GreetingService greetingService) {
		this.greetingService=greetingService;
	}

    @GetMapping("/greet")
    public String getGreeting() throws Exception {
    	return greetingService.getServiceGreeting();
    }

}
```

Una vez implementados el controlador y el servicio, vamos a terminar definiendo la configuración de este servicio, que será muy sencilla:

```.yml
spring:
  application:
    name: greeting-microservice

#Tomcat Embedded Server Configuration
server: 
  port: 8001

#Eureka Configuration Properties  
eureka: 
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka
```

Una vez guardados todos los archivos, generamos el .jar de la aplicación:

```
mvn clean install
```

<a name="imagenes"></a>
## Creación de imágenes

Tras el proceso de desarrollo, entramos en el mundo de DevOps, donde crearemos los archivos y configuraciones necesarias para desplegar nuestros tres servicios en Docker.

El proceso es bastante sencillo, ya que Docker nos ofrece Dockerfile como herramienta para crear imágenes de contenedores. Una imagen es una plantilla a través de la cuál, Docker creará los contenedores cuando así queramos, pero **NO** es un contenedor (es muy importante tener los conceptos claros para poder saber de qué estamos hablando, ya que es muy común confundir los términos imagen y contenedor).

El proceso es el siguiente. Iremos a la raíz de cada uno de los proyectos (o el directorio que queramos, pero en este ejemplo trabajaremos en el raíz), y en ella crearemos un archivo llamado Dockerfile. En este archivo, definiremos tres propiedades, aunque el mundo de Docker va mucho más allá, este ejemplo básico nos servirá para poder ejecutar la aplicación. Este Dockerfile será el de Eureka:

```Dockerfile
FROM openjdk:8-jdk-alpine
ADD target/DockerSpringNetflix-EurekaServer-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

- La propiedad **FROM** es la imagen base que vamos a tomar, que será la imagen openjdk:8-jdk-alpine, constituida por una distribución de Linux extremadamente ligera y un jdk para ejecutar aplicaciones Java.
- La propiedad **ADD** nos servirá para crear un directorio en el que alojaremos el ejecutable de cada una de nuestras aplicaciones.
- La propiedad **ENTRYPOINT** será el comando que se ejecute cuando se levante el contenedor con esta imagen, el cuál ejecutará nuestra aplicación usando el fichero .jar.

Para los que estéis familiarizados con Docker, quizá notéis la ausencia de la propiedad **EXPOSE**, que nos permite exponer ese puerto en concreto al exterior. La explicación viene más adelante, pero tiene que ver con cómo haremos que se comuniquen los microservicios.
Ya que sabemos qué significan estas propiedades, podemos crear los Dockerfile del resto de aplicaciones.

Dockerfile de Zuul:

```Dockerfile
FROM openjdk:8-jdk-alpine
ADD target/DockerSpringNetflix-ZuulService-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

Dockerfile de GreetingMicroservice:

```Dockerfile
FROM openjdk:8-jdk-alpine
ADD target/DockerSpringNetflix-GreetingMicroservice-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

<a name="contenedores"></a>
## Creación de contenedores y despliegue

Una vez creadas las imágenes de los servicios, de alguna manera tenemos que usarlas para crear los contenedores en los que se ejecutarán estos servicios. Docker ofrece la herramienta docker-compose, que nos ahorrará ejecutar un comando para levantar cada contenedor, que en este ejemplo sólo serían tres, pero para casos con muchos más servicios, nos permite centralizar la configuración de despliegue, lo cuál es mucho más práctico.

La sintaxis de este fichero es bastante sencilla, y podemos alojarlo donde queramos, ya que lo único que necesitamos son las imágenes creadas anteriormente, que ya están subidas a nuestro repositorio de imágenes en local.

Deberíamos configurarlo tal que así:
```docker-compose
version: '3.8'
services:
  eureka-server:
    image: eureka #nombre de la imagen que hemos creado
    restart: always
    container_name: eureka
    ports:
      - '8761:8761'
    networks:
      - 'dockernetflix-network'
  zuul-service:
    image: zuul #nombre de la imagen que hemos creado
    restart: always
    container_name: zuul
    ports:
      - '7000:7000'
    networks:
      - 'dockernetflix-network'
  greeting-microservice:
    image: greetmicroservice #nombre de la imagen que hemos creado
    restart: always
    container_name: greetmicroservice
    networks:
      - 'dockernetflix-network'
networks:
  dockernetflix-network: #necesitamos crear en una red para alojar los servicios en ella y que puedan comunicarse entre sí

```

Como apunte, es una buena práctica crear los servicios dentro de una red, lo que permitirá que los servicios que se ejecuten dentro de ella tendrán acceso a todos los puertos de los servicios que se estén ejecutando dentro de ella (por eso no necesitamos la propiedad **EXPOSE** en los Dockerfiles), además de bloquear todos los puertos al exterior, creando un entorno seguro para nuestras aplicaciones.

Los puertos que definimos en los servicios serán los únicos que vamos a permitir que se salten el "bloqueo" de la network, por ejemplo, para el servicio Eureka, usaremos el puerto del contenedor 8761 y será accesible desde mi host en el puerto 8761, de ahí la sintaxis "doble" 8761:8761.

Tenemos otras alternativas a crear una network, por ejemplo *link*, que sirven para conectar dos o más contenedores, pero su configuración es mucho más tediosa, además de ser legacy, lo que significa que Docker en algún momento eliminará esta característica. 

Por último, sólo nos queda desplegar los contenedores. Lo haremos con el siguiente comando, donde -d significa que los contenedores se ejecutarán dejándonos la terminal libre y sin imprimir sus salidas en ella:

```
docker-compose up -d
```

Podemos monitorizar el proceso de despliegue desde Docker Dashboard, donde la red que acabamos de crear tendrá todos los logs de los tres servicios.

Para ver que la aplicación funciona correctamente, podemos verlo en las siguientes direcciones.

- En http://localhost:8761 veremos el panel de control de Eureka, que tiene información sobre los servicios desplegados y su salud.
- En http://localhost:7000/greeting-service/greet  veremos que el servicio nos responde con: Hello from port: 8001.

No podremos acceder directamente con el GreetingMicroservice, por una razón muy sencilla. A la hora de levantar el contenedor de este servicio, no hemos definido ningún puerto para que sea accesible desde el host, por lo que el acceso a sus recursos sólo será a través de Zuul.

<a name="conclusiones"></a>
## Conclusiones

Con este ejemplo he querido ilustrar cómo funciona la arquitectura de microservicios con Spring Cloud Netflix y Docker, en la que todos los servicios serán registrados y monitorizados por Eureka, y solicitaremos los recursos a través de Zuul, y el mensaje que recibimos es el que ha sido creado por el GreetingMicroservice, pero devuelto por Zuul. Hemos creado una red de contenedores relativamente segura, en la que no podremos acceder a los microservicios que se estén ejecutando dentro, excepto a Eureka y a Zuul porque así lo hemos definido en el docker-compose.

<a name="referencias"></a>
## Referencias

 - <a href="https://spring.io/projects/spring-cloud-netflix" target="_blank">Spring Cloud Netflix</a>
 - <a href="https://docs.docker.com/" target="_blank">Docker</a>
 - <a href="https://www.adictosaltrabajo.com/2016/03/25/introduccion-a-los-microservicios/" target="_blank">Introducción a los Microservicios</a>
 - <a href="https://www.adictosaltrabajo.com/2017/04/26/introduccion-a-la-gestion-de-servicios-web-con-spring-cloud-y-netflix-oss/" target="_blank">Spring Boot y Spring Cloud Netflix</a>
 - <a href="https://github.com/SamuelGarciaG/TFG-Microservices-Netflix" target="_blank">Estudio de la arquitectura de Microservicios y Spring Boot</a>
