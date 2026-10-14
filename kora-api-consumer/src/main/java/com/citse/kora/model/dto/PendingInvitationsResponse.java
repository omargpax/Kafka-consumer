package com.citse.kora.model.dto;

import java.util.List;

public record PendingInvitationsResponse(long total, int page, int size, List<PendingInvitation> invitations) {
}
