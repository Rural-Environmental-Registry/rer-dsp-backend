package br.car.dsp;

import br.car.dsp.config.InstallationConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InstallationConfigProperties.class)
public class DspApplication {

	public static void main(String[] args) {
		SpringApplication.run(DspApplication.class, args);
	}
}
