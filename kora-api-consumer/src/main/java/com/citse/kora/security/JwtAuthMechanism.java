package com.citse.kora.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;

/**
 * Misma autenticacion que Kundu API: lee "Authorization: Bearer xxx", valida firma/expiracion
 * y delega la carga del usuario en JwtIdentityProvider. Las respuestas 401 llevan el mismo JSON (AuthJWTResponse).
 */
@ApplicationScoped
public class JwtAuthMechanism implements HttpAuthenticationMechanism {

    static final String INVALID_TOKEN_ATTR = "kora.jwt.invalid";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    JwtService jwtService;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        final String authHeader = context.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Uni.createFrom().nullItem(); // sin credenciales: la ruta exige autenticacion
        }
        final String jwt = authHeader.substring(7);
        try {
            jwtService.extractUsername(jwt); // valida firma y expiracion
        } catch (JwtException | IllegalArgumentException e) {
            context.put(INVALID_TOKEN_ATTR, Boolean.TRUE);
            return Uni.createFrom().failure(new AuthenticationFailedException());
        }
        return identityProviderManager.authenticate(new TokenAuthenticationRequest(new TokenCredential(jwt, "bearer")));
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }

    // (sin @Override a proposito: metodo default de la interfaz, asi compila en cualquier 3.x)
    public Uni<Boolean> sendChallenge(RoutingContext context) {
        boolean invalidToken = Boolean.TRUE.equals(context.get(INVALID_TOKEN_ATTR));
        String message = invalidToken
                ? "The token has expired or is invalid"
                : "You are not authorized to access this resource";
        try {
            String body = MAPPER.writeValueAsString(new AuthJWTResponse(context.request().path(), message, 401));
            context.response().setStatusCode(401).putHeader("Content-Type", "application/json").end(body);
        } catch (Exception e) {
            context.response().setStatusCode(401).end();
        }
        return Uni.createFrom().item(true);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(TokenAuthenticationRequest.class);
    }

    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return Uni.createFrom().item(getCredentialTransport());
    }

    public HttpCredentialTransport getCredentialTransport() {
        return new HttpCredentialTransport(HttpCredentialTransport.Type.AUTHORIZATION, "bearer");
    }
}
