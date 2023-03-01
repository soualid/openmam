package openmam.worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {


	public static String currentAccessToken;


	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

}
