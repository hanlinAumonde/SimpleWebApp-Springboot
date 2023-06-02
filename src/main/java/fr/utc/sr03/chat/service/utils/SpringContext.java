package fr.utc.sr03.chat.service.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Cette classe permet de récupérer le contexte de Spring
 * Comme le WebSocket qu'on utilise n'est pas géré par Spring, on ne peut pas utiliser l'injection de dépendance
 * Donc on utilise cette classe pour récupérer le contexte de Spring
 * et pouvoir utiliser les Services dans le WebSocket pour gérer les données dans la base de données
 */
@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
