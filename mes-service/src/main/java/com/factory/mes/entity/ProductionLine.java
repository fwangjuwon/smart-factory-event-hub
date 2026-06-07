package com.factory.mes.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_line")
public class ProductionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_main_id", nullable = false)
    private ProcessMain processMain;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "productionLine", fetch = FetchType.LAZY)
    private List<ProcessSub> processSubs = new ArrayList<>();

    public Long getId() { return id; }
    public ProcessMain getProcessMain() { return processMain; }
    public String getName() { return name; }
    public List<ProcessSub> getProcessSubs() { return processSubs; }
}
