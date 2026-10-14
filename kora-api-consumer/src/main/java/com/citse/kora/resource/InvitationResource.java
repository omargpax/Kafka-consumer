package com.citse.kora.resource;

import com.citse.kora.model.dto.PendingInvitationsResponse;
import com.citse.kora.service.InvitationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/invitations")
@Produces(MediaType.APPLICATION_JSON)
public class InvitationResource {

    private static final int MAX_SIZE = 500;

    @Inject
    InvitationService invitationService;

    /** Invitados que aun no tienen registro como usuario/persona (nuevos por registrar). Requiere token de Kundu API. */
    @GET
    @Path("/pending")
    public PendingInvitationsResponse pending(@QueryParam("page") @DefaultValue("0") int page,
                                              @QueryParam("size") @DefaultValue("100") int size) {
        return invitationService.findPending(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE));
    }
}
