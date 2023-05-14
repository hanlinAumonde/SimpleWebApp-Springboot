package fr.utc.sr03.chat.service.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class ErrorPageInterceptor implements HandlerInterceptor {
    private final Map<Integer,String> errorCodes = new HashMap<Integer,String>(
            Map.ofEntries(
                    Map.entry(403,"Forbidden"),
                    Map.entry(404,"Not Found"),
                    Map.entry(500,"Internal Server Error")
            )
    );

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
