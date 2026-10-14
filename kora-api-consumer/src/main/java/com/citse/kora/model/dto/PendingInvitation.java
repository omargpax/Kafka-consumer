package com.citse.kora.model.dto;

import java.time.LocalDate;

public record PendingInvitation(Integer id, String email, Integer invitedBy, LocalDate date) {
}
