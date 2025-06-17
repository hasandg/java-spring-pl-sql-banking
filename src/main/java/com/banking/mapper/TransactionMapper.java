package com.banking.mapper;

import com.banking.dto.TransactionDto;
import com.banking.dto.TransactionRequestDto;
import com.banking.dto.TransactionResponseDto;
import com.banking.entity.Transaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TransactionMapper {

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    TransactionDto toDto(Transaction transaction);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    Transaction toEntity(TransactionDto dto);

    List<TransactionDto> toDtoList(List<Transaction> transactions);
    
    List<Transaction> toEntityList(List<TransactionDto> dtos);

    @Mapping(target = "accountNumber", source = "account.accountNumber")
    TransactionResponseDto toResponseDto(Transaction transaction);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Transaction toEntity(TransactionRequestDto dto);

    List<TransactionResponseDto> toResponseDtoList(List<Transaction> transactions);
} 