package com.decisionhub.mapper;

import com.decisionhub.dto.ReportRequest;
import com.decisionhub.dto.ReportResponse;
import com.decisionhub.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportMapper {

    @Mapping(target = "userId", source = "user.id")
    ReportResponse toResponse(Report report);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Report toEntity(ReportRequest request);
}
