package com.ticketing.entrainement.infrastructure;

import com.ticketing.entrainement.domain.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TicketEntityMapper {
    Ticket toDomain(TicketEntity entity);
    @Mapping(
            target = "normalizedTitle",
            expression = "java(com.ticketing.entrainement.domain.TicketText.normalize(ticket.title()))"
    )
    TicketEntity toEntity(Ticket ticket);
}
