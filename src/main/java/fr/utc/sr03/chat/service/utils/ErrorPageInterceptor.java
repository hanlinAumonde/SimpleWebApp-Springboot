package fr.utc.sr03.chat.service.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Cette classe permet de gérer les erreurs
 */
@Component
public class ErrorPageInterceptor implements HandlerInterceptor {
    private final Map<Integer,String> errorCodes = new HashMap<Integer,String>(
            Map.ofEntries(
                    Map.entry(401,"Unauthorized"),
                    Map.entry(403,"Forbidden"),
                    Map.entry(404,"Not Found"),
                    Map.entry(409,"Conflict"),
                    Map.entry(500,"Internal Server Error")
            )
    );

    /**
     * Cette méthode permet de rediriger vers la page d'erreur correspondante avec le code d'erreur et le message
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(errorCodes.containsKey(response.getStatus())){
            request.getSession().setAttribute("errorInfo", errorCodes.get(response.getStatus()));
            response.sendRedirect("/errorPage/" + response.getStatus());
            return false;
        }
        return true;
    }
}
