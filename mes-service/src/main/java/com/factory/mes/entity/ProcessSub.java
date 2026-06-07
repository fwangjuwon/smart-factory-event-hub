package com.factory.mes.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_sub")
public class ProcessSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "processSub", fetch = FetchType.LAZY)
    private List<Equipment> equipments = new ArrayList<>();

    public Long getId() { return id; }
    public ProductionLine getProductionLine() { return productionLine; }
    public String getName() { return name; }
    public List<Equipment> getEquipments() { return equipments; }
}
