package com.ticketing.entrainement.application;

import java.util.UUID;

public record AttachTicketRequest(UUID parentId) {}