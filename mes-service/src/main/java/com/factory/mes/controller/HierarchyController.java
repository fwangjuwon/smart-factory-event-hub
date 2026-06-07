package com.factory.mes.controller;

import com.factory.mes.dto.HierarchyDto;
import com.factory.mes.service.HierarchyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hierarchy")
public class HierarchyController {

    private final HierarchyService hierarchyService;

    public HierarchyController(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    @GetMapping
    public ResponseEntity<List<HierarchyDto.SiteDto>> getHierarchy() {
        return ResponseEntity.ok(hierarchyService.getFullHierarchy());
    }
}
