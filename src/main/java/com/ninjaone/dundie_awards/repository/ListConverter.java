package com.ninjaone.dundie_awards.repository;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Converter
public class ListConverter implements AttributeConverter<List<Long>, String> {

    private static final String EMPTY = "[]";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return EMPTY;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("LongListToJsonConverter.convertToDatabaseColumn Failed to convert List<Long> to JSON: {}",
                    list, e);
            throw new RuntimeException("Failed to convert List<Long> to JSON", e);
        }
    }

    @Override
    public List<Long> convertToEntityAttribute(String json) {
        if (isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            log.error("LongListToJsonConverter.convertToEntityAttribute Failed to convert JSON to List<Long>: {}",
                    json, e);
            throw new RuntimeException("Failed to convert JSON to List<Long>", e);
        }
    }
}