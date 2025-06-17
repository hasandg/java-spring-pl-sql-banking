package com.banking.mapper;

import com.banking.dto.AuditLogDto;
import com.banking.entity.AuditLog;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AuditLogMapper {

    AuditLogDto toDto(AuditLog auditLog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    AuditLog toEntity(AuditLogDto dto);

    List<AuditLogDto> toDtoList(List<AuditLog> auditLogs);
} 