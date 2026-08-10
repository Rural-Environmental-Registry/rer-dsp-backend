package br.car.dsp;

import br.car.dsp.config.DownloadConfigProperties;
import br.car.dsp.config.InstallationConfigProperties;
import br.car.dsp.config.MapConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		InstallationConfigProperties.class,
		MapConfigProperties.class,
		DownloadConfigProperties.class
})
public class DspApplication {

	public static void main(String[] args) {
		SpringApplication.run(DspApplication.class, args);
	}
}
