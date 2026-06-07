package com.factory.mes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "equipment_spec",
       uniqueConstraints = @UniqueConstraint(columnNames = {"equipment_id", "event_type"}))
public class EquipmentSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false, length = 50)
    private String equipmentId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal threshold;

    @Column(name = "tolerance_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal tolerancePct;

    public Long getId() { return id; }
    public String getEquipmentId() { return equipmentId; }
    public String getEventType() { return eventType; }
    public BigDecimal getThreshold() { return threshold; }
    public BigDecimal getTolerancePct() { return tolerancePct; }

    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
    public void setTolerancePct(BigDecimal tolerancePct) { this.tolerancePct = tolerancePct; }
}
