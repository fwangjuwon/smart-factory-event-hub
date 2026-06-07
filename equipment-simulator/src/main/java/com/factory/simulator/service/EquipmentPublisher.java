package com.factory.simulator.service;

import com.factory.simulator.config.RabbitMQConfig;
import com.factory.simulator.model.EquipmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class EquipmentPublisher {

    private static final Logger log = LoggerFactory.getLogger(EquipmentPublisher.class);
    private static final List<String> EQUIPMENT_IDS = List.of("EQ-A", "EQ-B", "EQ-C", "EQ-D", "EQ-E", "EQ-F", "EQ-G");
    private static final Random RANDOM = new Random();

    private final RabbitTemplate rabbitTemplate;

    public EquipmentPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 3000, initialDelay = 5000)
    public void publishEvents() {
        for (String equipmentId : EQUIPMENT_IDS) {
            publishTemperature(equipmentId);
            publishOperationRate(equipmentId);
            publishDefectRate(equipmentId);
        }
    }

    private void publishTemperature(String equipmentId) {
        // 10% 확률로 이상값(80도 초과) 발생
        double value = RANDOM.nextInt(10) == 0
                ? 80 + RANDOM.nextDouble() * 30
                : RANDOM.nextDouble() * 80;
        publish(equipmentId, "TEMPERATURE", Math.round(value * 10.0) / 10.0);
    }

    private void publishOperationRate(String equipmentId) {
        // 15% 확률로 이상값(70% 미만) 발생
        double value = RANDOM.nextInt(100) < 15
                ? RANDOM.nextDouble() * 70
                : 70 + RANDOM.nextDouble() * 30;
        publish(equipmentId, "OPERATION_RATE", Math.round(value * 10.0) / 10.0);
    }

    private void publishDefectRate(String equipmentId) {
        // 12% 확률로 이상값(3% 이상) 발생
        double value = RANDOM.nextInt(100) < 12
                ? 3 + RANDOM.nextDouble() * 7
                : RANDOM.nextDouble() * 3;
        publish(equipmentId, "DEFECT_RATE", Math.round(value * 100.0) / 100.0);
    }

    private void publish(String equipmentId, String eventType, double value) {
        EquipmentEvent event = new EquipmentEvent(equipmentId, eventType, value, LocalDateTime.now());
        String routingKey = "equipment." + equipmentId.toLowerCase() + "." + eventType.toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
        log.info("Published [{}] {} = {}", equipmentId, eventType, value);
    }
}
