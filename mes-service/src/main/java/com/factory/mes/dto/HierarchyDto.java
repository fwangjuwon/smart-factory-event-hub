package com.factory.mes.dto;

import java.util.List;

public class HierarchyDto {

    public record SiteDto(Long id, String name, String location, List<ProcessMainDto> processMainList) {}
    public record ProcessMainDto(Long id, String name, List<LineDto> lines) {}
    public record LineDto(Long id, String name, List<ProcessSubDto> processSubs) {}
    public record ProcessSubDto(Long id, String name, List<String> equipmentIds) {}
}
