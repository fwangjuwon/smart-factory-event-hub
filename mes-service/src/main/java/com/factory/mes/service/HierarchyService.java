package com.factory.mes.service;

import com.factory.mes.dto.HierarchyDto;
import com.factory.mes.entity.*;
import com.factory.mes.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HierarchyService {

    private final SiteRepository siteRepository;
    private final ProcessMainRepository processMainRepository;
    private final ProductionLineRepository lineRepository;
    private final ProcessSubRepository processSubRepository;

    public HierarchyService(SiteRepository siteRepository,
                            ProcessMainRepository processMainRepository,
                            ProductionLineRepository lineRepository,
                            ProcessSubRepository processSubRepository) {
        this.siteRepository       = siteRepository;
        this.processMainRepository = processMainRepository;
        this.lineRepository        = lineRepository;
        this.processSubRepository  = processSubRepository;
    }

    public List<HierarchyDto.SiteDto> getFullHierarchy() {
        // 단계별 조회 - MultipleBagFetchException 방지
        return siteRepository.findAllWithProcessMain().stream()
                .map(this::toSiteDto)
                .toList();
    }

    private HierarchyDto.SiteDto toSiteDto(Site site) {
        List<HierarchyDto.ProcessMainDto> pmDtos = processMainRepository
                .findBySiteIdWithLines(site.getId()).stream()
                .map(this::toPmDto)
                .toList();

        return new HierarchyDto.SiteDto(site.getId(), site.getName(), site.getLocation(), pmDtos);
    }

    private HierarchyDto.ProcessMainDto toPmDto(ProcessMain pm) {
        List<HierarchyDto.LineDto> lineDtos = lineRepository
                .findByProcessMainIdWithSubs(pm.getId()).stream()
                .map(this::toLineDto)
                .toList();

        return new HierarchyDto.ProcessMainDto(pm.getId(), pm.getName(), lineDtos);
    }

    private HierarchyDto.LineDto toLineDto(ProductionLine line) {
        List<HierarchyDto.ProcessSubDto> subDtos = processSubRepository
                .findByLineIdWithEquipments(line.getId()).stream()
                .map(ps -> new HierarchyDto.ProcessSubDto(
                        ps.getId(),
                        ps.getName(),
                        ps.getEquipments().stream().map(Equipment::getEquipmentId).toList()
                ))
                .toList();

        return new HierarchyDto.LineDto(line.getId(), line.getName(), subDtos);
    }
}
