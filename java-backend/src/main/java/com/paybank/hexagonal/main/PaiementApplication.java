package com.paybank.hexagonal.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import com.paybank.hexagonal.domaine.ServicePaiementImplementation;
import com.paybank.hexagonal.ports.ExecutionPaiementUseCase;
import com.paybank.hexagonal.ports.PasserelleBancaireSPI;
import com.paybank.hexagonal.ports.PersistancePaiementSPI;

@SpringBootApplication
@ComponentScan(basePackages = {"com.paybank.hexagonal"})
@EnableJpaRepositories(basePackages = "com.paybank.hexagonal.repository")
@EntityScan(basePackages = "com.paybank.hexagonal.entity")
public class PaiementApplication {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("password"));
		SpringApplication.run(PaiementApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
	
}