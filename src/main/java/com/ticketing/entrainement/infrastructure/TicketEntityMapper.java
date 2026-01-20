package com.ticketing.entrainement.infrastructure;

import com.ticketing.entrainement.domain.Ticket;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface TicketEntityMapper {
    Ticket toDomain(TicketEntity entity);
    TicketEntity toEntity(Ticket ticket);
}
