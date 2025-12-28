package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients		// Habilita el uso de clientes OpenFeign
@EnableDiscoveryClient	// Se activa como cliente Eureka
@SpringBootApplication
public class MsvcProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcProductApplication.class, args);
	}

}
