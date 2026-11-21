package io.github.projectlinden.api.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tells the logged-in user who they are, according to their token.
 * There is no user id in the URL: the identity comes only from the signed JWT.
 */
@RestController
@RequestMapping("/api/v1/me")
class MeController {

    @GetMapping
    MeResponse getCurrentUser(@AuthenticationPrincipal Jwt token) {
        return new MeResponse(token.getSubject());
    }
}