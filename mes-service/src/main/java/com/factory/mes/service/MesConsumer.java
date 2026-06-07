package com.factory.mes.service;

import com.factory.mes.config.RabbitMQConfig;
import com.factory.mes.dto.EquipmentEventMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MesConsumer {

    private final EventProcessingService processingService;

    public MesConsumer(EventProcessingService processingService) {
        this.processingService = processingService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consume(EquipmentEventMessage message) {
        processingService.process(message);
    }
}
