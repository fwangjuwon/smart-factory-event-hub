package com.factory.mes.controller;

import com.factory.mes.dto.SpecDto;
import com.factory.mes.service.SpecService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specs")
public class SpecController {

    private final SpecService specService;

    public SpecController(SpecService specService) {
        this.specService = specService;
    }

    @GetMapping
    public ResponseEntity<List<SpecDto>> getAll(
            @RequestParam(required = false) List<String> equipmentIds) {
        List<SpecDto> result = specService.getAll();
        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            result = result.stream()
                    .filter(s -> equipmentIds.contains(s.equipmentId()))
                    .toList();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<SpecDto> save(@RequestBody SpecDto dto) {
        return ResponseEntity.ok(specService.save(dto));
    }
}
