package goorm.ddok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DdokApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdokApplication.class, args);
	}

}
