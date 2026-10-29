package upeu.edu.pe.Saludabemente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SaludabementeErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaludabementeErpApplication.class, args);
	}

}
