package fr.utc.sr03.chat.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccountAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Redirect vers la page de login, avec un message d'erreur
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String error = exception.getMessage();
        request.getSession().setAttribute("error", error);
        response.sendRedirect("/login");
    }
}
