package com.ticketing.entrainement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TicketCleanupConfig {

    @Bean
    Duration ticketCleanupInactivityDuration(
            @Value("${ticket.cleanup.inactivity-days:2}") long days
    ) {
        return Duration.ofDays(days);
    }
}