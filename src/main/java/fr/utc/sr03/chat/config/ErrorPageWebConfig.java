package fr.utc.sr03.chat.config;

import fr.utc.sr03.chat.service.utils.ErrorPageInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ErrorPageWebConfig implements WebMvcConfigurer {
    /**
     * Ajouter l'intercepteur pour les pages d'erreur
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ErrorPageInterceptor());
    }
}
