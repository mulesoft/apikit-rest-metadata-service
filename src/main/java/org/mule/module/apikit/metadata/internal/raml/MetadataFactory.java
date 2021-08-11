/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonExampleTypeLoader;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.runtime.api.metadata.MediaType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mule.metadata.api.builder.BaseTypeBuilder.create;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.MIME_APPLICATION_JSON;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.MIME_APPLICATION_URL_ENCODED;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.MIME_APPLICATION_XML;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.MIME_MULTIPART_FORM_DATA;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.defaultMetadata;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.fromXMLExample;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.fromXSDSchema;
import static org.mule.runtime.api.metadata.MediaType.parse;

class MetadataFactory {

  private static final MetadataType STRING_METADATA = create(MetadataFormat.JAVA).stringType().build();

  private MetadataFactory() {}

  public static MetadataType payloadMetadata(final RamlApiWrapper api, final @Nullable MimeType body) {
    if (body == null) {
      return defaultMetadata();
    }

    final MediaType mType = parse(body.getType());
    final String type = mType.getPrimaryType() + "/" + mType.getSubType();
    final String schema = resolveSchema(api, body);
    final String example = body.getExample();

    switch (type) {
      case MIME_APPLICATION_JSON:
        return applicationJsonMetadata(schema, example);
      case MIME_APPLICATION_XML:
        return applicationXmlMetadata(schema, example);
      case MIME_APPLICATION_URL_ENCODED:
      case MIME_MULTIPART_FORM_DATA:
        return formMetadata(body.getFormParameters());
      default:
        return defaultMetadata();
    }
  }

  private static String resolveSchema(RamlApiWrapper api, MimeType body) {
    String schema = body.getSchema();

    // As body.getSchema() can return the name of the schema, null or
    // the schema itself, first we assume that it has the schema name
    // and we try to get the schema def from the api consolidated
    // schemas
    if (api.getConsolidatedSchemas().containsKey(schema)) {
      schema = api.getConsolidatedSchemas().get(schema);
    }

    return schema;
  }

  private static MetadataType formMetadata(Map<String, List<Parameter>> formParameters) {
    return MetadataFactory.fromFormMetadata(formParameters);
  }

  private static MetadataType applicationXmlMetadata(String schema, String example) {
    if (schema != null && schema.contains("XMLSchema")) {
      return fromXSDSchema(schema);
    } else if (example != null && !example.isEmpty()) {
      MetadataType fromExample = fromXMLExample(example);
      if (fromExample != null) {
        return fromExample;
      }
    }

    return defaultMetadata();
  }

  private static MetadataType applicationJsonMetadata(String schema, String example) {
    if (schema != null) {
      return MetadataFactory.fromJsonSchema(schema);
    } else if (example != null) {
      return MetadataFactory.fromJsonExample(example);
    }

    return defaultMetadata();
  }


  /**
   * Creates metadata from a JSON Schema
   *
   * @param jsonSchema The schema we want to create metadata from
   * @return The metadata if the Schema is valid, null otherwise
   */
  public static MetadataType fromJsonSchema(String jsonSchema) {
    final JsonTypeLoader jsonTypeLoader = new JsonTypeLoader(jsonSchema);
    final Optional<MetadataType> root = jsonTypeLoader.load(null);

    // We didn't managed to get the schema.
    return root.orElse(defaultMetadata());
  }


  /**
   * Creates metadata from the specified JSON Example
   *
   * @param jsonExample
   * @return The metadata if the example is valid, null otherwise
   */
  private static MetadataType fromJsonExample(String jsonExample) {
    JsonExampleTypeLoader jsonExampleTypeLoader = new JsonExampleTypeLoader(jsonExample);
    jsonExampleTypeLoader.setFieldRequirementDefault(false);
    Optional<MetadataType> root = jsonExampleTypeLoader.load(null);

    // We didn't managed to get the schema.
    return root.orElse(defaultMetadata());
  }

  public static MetadataType fromFormMetadata(Map<String, List<Parameter>> formParameters) {
    final ObjectTypeBuilder parameters = create(MetadataFormat.JAVA).objectType();

    for (Map.Entry<String, List<Parameter>> entry : formParameters.entrySet()) {
      parameters.addField()
          .key(entry.getKey())
          .value().anyType();
    }

    return parameters.build();
  }

  /**
   * Creates metadata to describe an string type
   *
   * @return The newly created MetadataType
   */
  public static MetadataType stringMetadata() {
    return STRING_METADATA;
  }

  public static MetadataType objectMetadata() {
    return create(MetadataFormat.JAVA).objectType().build();
  }

  public static MetadataType binaryMetadata() {
    return create(MetadataFormat.JAVA).binaryType().build();
  }

}
