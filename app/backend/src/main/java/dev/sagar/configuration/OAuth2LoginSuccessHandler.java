package dev.sagar.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import dev.sagar.conversations.User;
import dev.sagar.conversations.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        var provider = authenticationToken.getAuthorizedClientRegistrationId();
        var providerId = authentication.getName();

        logger.info("Provider: {}, Provider ID: {}", provider, providerId);

        String email = authenticationToken.getPrincipal().getAttribute("email");

        logger.info("User attributes: {}", authenticationToken.getPrincipal().getAttributes());

        logger.info("User logged in: {}", email);

        var user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            logger.info("User already exists: {}", email);
        } else {
            logger.info("Creating new user: {}", email);
            registerOAuth2User(authenticationToken);
        }
        response.sendRedirect("http://localhost:3000/chat");
    }

    private void registerOAuth2User(OAuth2AuthenticationToken authenticationToken) {

        String email = authenticationToken.getPrincipal().getAttribute("email");
        String name = authenticationToken.getPrincipal().getAttribute("name");

        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setName(name);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
