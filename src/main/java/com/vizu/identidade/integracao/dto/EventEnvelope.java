package com.vizu.identidade.integracao.dto;
import com.fasterxml.jackson.databind.JsonNode; import java.util.UUID;
public record EventEnvelope(UUID eventId,String eventType,JsonNode payload) {}
