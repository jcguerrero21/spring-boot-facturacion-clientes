package jc.guerrero.app;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//en springboot 2.0 a cambiado WebMvcConfigurer

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter{

	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/*Este  método sería si queremos guardar el archivo en una misma carpeta dentro del proyecto
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);
		
		String resourcePath = Paths.get("uploads").toAbsolutePath().toUri().toString();
		log.info(resourcePath);
		
		registry.addResourceHandler("/uploads/**").addResourceLocations(resourcePath);
	}*/
	
	
	
}
