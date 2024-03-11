package edu.agh.bpmnai.generator.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.generator.impl.PropertySortUtils;
import edu.agh.bpmnai.generator.v2.functions.parameter.Description;
import jakarta.annotation.Nullable;

public class OpenAIFunctionParametersSchemaFactory {

    public static JsonNode getSchemaForParametersDto(Class<?> dtoClass) {
        // There's no information in the documentation whether any of those classes is thread-safe
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder
                .without(Option.FLATTENED_ENUMS_FROM_TOSTRING)
                .without(Option.SCHEMA_VERSION_INDICATOR)
                .build();

        configBuilder.forFields()
                .withRequiredCheck(field -> field.getAnnotationConsideringFieldAndGetter(Nullable.class) == null)
                .withDescriptionResolver(field -> field.getAnnotation(Description.class).value());

        configBuilder.forTypesInGeneral()
                .withPropertySorter(PropertySortUtils.SORT_PROPERTIES_FIELDS_BEFORE_METHODS);

        SchemaGenerator generator = new SchemaGenerator(config);
        return generator.generateSchema(dtoClass);
    }
}
