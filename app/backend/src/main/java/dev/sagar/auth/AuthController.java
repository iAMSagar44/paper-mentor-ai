package dev.sagar.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    @GetMapping("user")
    public ResponseEntity<UserInfo> getUserInfo() {
        // Check if the user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication
                        .getPrincipal() instanceof OAuth2AuthenticatedPrincipal) {

            // Access the authenticated user's details
            OAuth2AuthenticatedPrincipal oauth2User = (OAuth2AuthenticatedPrincipal) authentication.getPrincipal();
            logger.info("Authenticated user is: {}", (String) oauth2User.getAttribute("email"));
            logger.debug("User Attributes: {}", oauth2User.getAttributes());
            var userName = oauth2User.getName(); // User's name
            Map<String, Object> attributes = oauth2User.getAttributes(); // User attributes

            // Example: Get email from attributes
            // Adjust this based on the OAuth2 provider
            var email = (String) attributes.get("email");
            var name = (String) attributes.get("name");

            UserInfo userInfo = new UserInfo(email, name);
            logger.info("User info: {}", userInfo);
            return ResponseEntity.ok(userInfo);
        } else {
            // User is not authenticated
            logger.warn("User is not authenticated");
            return ResponseEntity.ok(null);
        }
    }

}
