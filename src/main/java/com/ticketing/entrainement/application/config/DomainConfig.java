package com.ticketing.entrainement.application.config;

import com.ticketing.entrainement.domain.TicketHierarchyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public TicketHierarchyService ticketHierarchyService() {
        return new TicketHierarchyService();
    }
}