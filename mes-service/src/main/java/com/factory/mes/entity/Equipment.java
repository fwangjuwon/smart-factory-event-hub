package com.factory.mes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @Column(name = "equipment_id", length = 50)
    private String equipmentId;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_sub_id")
    private ProcessSub processSub;

    public String getEquipmentId() { return equipmentId; }
    public String getName() { return name; }
    public ProcessSub getProcessSub() { return processSub; }

    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public void setName(String name) { this.name = name; }
    public void setProcessSub(ProcessSub processSub) { this.processSub = processSub; }
}
