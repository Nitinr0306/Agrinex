package com.agrinex.common.web;

import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.type.LogicalType;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON rules of the contract (docs/api/API_CONVENTIONS.md sections 2-4):
 * <ul>
 * <li>request bodies are closed: an unknown property is rejected (Jackson 3's default, kept here);</li>
 * <li>JSON types are not coerced: a string is never read as a number, boolean or text from another
 * type, and a fractional number is never truncated into an integer (field code INVALID_TYPE);</li>
 * <li>numbers are read as exact decimals, never through binary floating point (ADR-0008 item 4), and written
 * without exponent notation.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
class JsonConfiguration {

	@Bean
	JsonMapperBuilderCustomizer agrinexJsonRules() {
		return (builder) -> builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.enable(JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
			.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
			.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
			.enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
			.withCoercionConfig(LogicalType.Integer, (config) -> config.setCoercion(CoercionInputShape.String, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
			.withCoercionConfig(LogicalType.Float, (config) -> config.setCoercion(CoercionInputShape.String, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
			.withCoercionConfig(LogicalType.Boolean, (config) -> config.setCoercion(CoercionInputShape.String, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Float, CoercionAction.Fail))
			.withCoercionConfig(LogicalType.Textual, (config) -> config.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
				.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
	}

}
