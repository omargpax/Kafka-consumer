package com.citse.kora.security;

import com.citse.kora.model.entity.User;
import com.citse.kora.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashSet;
import java.util.Optional;

/**
 * Carga el usuario del token desde tsg_users (la misma BD de Kundu API), comprueba el token
 * y construye el SecurityIdentity con el rol y sus permisos como roles.
 */
@ApplicationScoped
public class JwtIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

    @Inject
    JwtService jwtService;
    @Inject
    UserRepository userRepository;

    @Override
    public Class<TokenAuthenticationRequest> getRequestType() {
        return TokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request, AuthenticationRequestContext context) {
        // Acceso a BD (bloqueante): se ejecuta en un worker thread, nunca en el event loop.
        return context.runBlocking(() -> {
            final String jwt = request.getToken().getToken();
            try {
                final String username = jwtService.extractUsername(jwt);
                Optional<User> found = QuarkusTransaction.joiningExisting().call(() -> userRepository.findByUsername(username));
                if (found.isEmpty()) {
                    throw new AuthenticationFailedException();
                }
                User user = found.get();
                if (!jwtService.isTokenValid(jwt, user)) {
                    throw new AuthenticationFailedException();
                }
                QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                        .setPrincipal(new QuarkusPrincipal(user.getUsername()))
                        .addCredential(request.getToken());
                builder.addRole(user.getRole().name());
                builder.addRoles(new HashSet<>(user.getRole().getAuthorities()));
                return builder.build();
            } catch (JwtException | IllegalArgumentException e) {
                throw new AuthenticationFailedException();
            }
        });
    }
}
