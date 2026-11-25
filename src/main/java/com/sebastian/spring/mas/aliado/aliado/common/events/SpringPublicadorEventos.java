package com.sebastian.spring.mas.aliado.aliado.common.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringPublicadorEventos implements PublicadorEventos {
    private final ApplicationEventPublisher publisher;

    public SpringPublicadorEventos(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(Object evento) {
        publisher.publishEvent(evento);
    }
}
