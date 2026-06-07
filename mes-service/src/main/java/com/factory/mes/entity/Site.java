package com.factory.mes.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String location;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private List<ProcessMain> processMainList = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public List<ProcessMain> getProcessMainList() { return processMainList; }
}
