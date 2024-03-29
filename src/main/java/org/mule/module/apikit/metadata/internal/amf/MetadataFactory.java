/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;


import amf.apicontract.client.platform.APIConfiguration;
import amf.core.client.platform.model.domain.Shape;
import amf.shapes.client.platform.model.domain.AnyShape;
import amf.shapes.client.platform.model.domain.ArrayShape;
import amf.shapes.client.platform.model.domain.FileShape;
import org.json.JSONObject;
import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.AnyType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonExampleTypeLoader;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static org.mule.metadata.api.builder.BaseTypeBuilder.create;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.fromXMLExample;

class MetadataFactory {

  private static final MetadataType STRING_METADATA = create(MetadataFormat.JAVA).stringType().build();
  private static final MetadataType ARRAY_STRING_METADATA = create(MetadataFormat.JAVA).arrayType().of(STRING_METADATA).build();
  private static final MetadataType DATE_TIME_METADATA = create(MetadataFormat.JAVA).dateTimeType().build();
  private static final MetadataType BOOLEAN_METADATA = create(MetadataFormat.JAVA).booleanType().build();
  private static final MetadataType INTEGER_METADATA = create(MetadataFormat.JAVA).numberType().integer().build();
  private static final MetadataType NUMBER_METADATA = create(MetadataFormat.JAVA).numberType().build();
  private static final String AMF_XSD_SCHEMA_KEY = "x-amf-schema";

  private MetadataFactory() {}

  public static MetadataType fromJSONSchema(final Shape shape, String example) {
    Optional<MetadataType> metadataType = empty();

    if (shape instanceof AnyShape) {
      final AnyShape anyShape = (AnyShape) shape;

      TypeLoader typeLoader = new JsonTypeLoader(APIConfiguration.API().elementClient().buildJsonSchema(anyShape));
      metadataType = typeLoader.load(null);
      if ((!metadataType.isPresent() || metadataType.get() instanceof AnyType) && !example.isEmpty()) {
        metadataType = createJsonExampleTypeLoader(example).load(null);
      }
    }
    return metadataType.orElse(CommonMetadataFactory.defaultMetadata());
  }

  /**
   * Looks for the schema definition inside the JSON schema built by AMF by the key "x-amf-schema". If no schema found, and there
   * is an example, tries to build the metadata from it. Otherwise, defaults to building metadata from JSON schema.
   *
   * @param shape
   * @param example
   * @return
   */
  public static MetadataType fromXSDSchema(Shape shape, String example) {
    if (shape instanceof AnyShape) {
      AnyShape anyShape = (AnyShape) shape;
      Optional<String> xsdSchema = getXSDSchemaFromShape(anyShape);
      if (xsdSchema.isPresent()) {
        return CommonMetadataFactory.fromXSDSchema(xsdSchema.get());
      }
    }
    if (example != null && !example.isEmpty()) {
      MetadataType fromExample = fromXMLExample(example);
      if (fromExample != null) {
        return fromExample;
      }
    }
    return MetadataFactory.fromJSONSchema(shape, example);
  }

  /**
   * Returns XSD schema from shape if exists.
   *
   * @param anyShape
   * @return
   */
  private static Optional<String> getXSDSchemaFromShape(AnyShape anyShape) {
    JSONObject jsonSchema =
        new JSONObject(APIConfiguration.API().elementClient().buildJsonSchema(anyShape));
    String[] reference = jsonSchema.get("$ref").toString().split("/");
    JSONObject jsonSchemaDefinition = jsonSchema.getJSONObject("definitions").getJSONObject(reference[reference.length - 1]);
    Set<String> keys = jsonSchemaDefinition.keySet();
    if (keys.contains(AMF_XSD_SCHEMA_KEY)) {
      return Optional.of(jsonSchemaDefinition.getString(AMF_XSD_SCHEMA_KEY));
    }
    return Optional.empty();
  }

  static MetadataType defaultMetadata(final Shape shape) {
    if (shape instanceof FileShape) {
      return stringMetadata();
    }

    if (shape instanceof ArrayShape) {
      return arrayStringMetadata();
    }

    return CommonMetadataFactory.defaultMetadata();
  }

  private static JsonExampleTypeLoader createJsonExampleTypeLoader(final String example) {
    final JsonExampleTypeLoader typeLoader = new JsonExampleTypeLoader(example);
    typeLoader.setFieldRequirementDefault(false);
    return typeLoader;
  }

  /**
   * Creates metadata to describe an string type
   *
   * @return The newly created MetadataType
   */
  static MetadataType stringMetadata() {
    return STRING_METADATA;
  }

  static MetadataType arrayStringMetadata() {
    return ARRAY_STRING_METADATA;
  }

  static MetadataType booleanMetadada() {
    return BOOLEAN_METADATA;
  }

  static MetadataType numberMetadata() {
    return NUMBER_METADATA;
  }

  static MetadataType integerMetadata() {
    return INTEGER_METADATA;
  }

  static MetadataType dateTimeMetadata() {
    return DATE_TIME_METADATA;
  }

  static MetadataType objectMetadata() {
    return create(MetadataFormat.JAVA).objectType().build();
  }

  static MetadataType binaryMetadata() {
    return create(MetadataFormat.JAVA).binaryType().build();
  }

}
