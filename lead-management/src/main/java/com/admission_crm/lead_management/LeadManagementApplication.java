package com.admission_crm.lead_management;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LeadManagementApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().directory("D:\\VK18\\My Projects\\Admission CRM\\Admission_CRM_Backend\\lead-management\\.env").load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(LeadManagementApplication.class, args);
	}

}
