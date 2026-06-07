package com.factory.mes.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_main")
public class ProcessMain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "processMain", fetch = FetchType.LAZY)
    private List<ProductionLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public Site getSite() { return site; }
    public String getName() { return name; }
    public List<ProductionLine> getLines() { return lines; }
}
