/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import amf.client.model.domain.AnyShape;
import amf.client.model.domain.ArrayShape;
import amf.client.model.domain.Example;
import amf.client.model.domain.FileShape;
import amf.client.model.domain.Shape;
import java.util.List;
import java.util.Optional;
import org.mule.metadata.api.TypeLoader;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonExampleTypeLoader;
import org.mule.metadata.json.api.JsonTypeLoader;

import static java.util.Optional.empty;
import static org.mule.metadata.api.builder.BaseTypeBuilder.create;

class MetadataFactory {

  private static final MetadataType DEFAULT_METADATA = create(MetadataFormat.JAVA).anyType().build();
  private static final MetadataType STRING_METADATA = create(MetadataFormat.JAVA).stringType().build();
  private static final MetadataType ARRAY_STRING_METADATA = create(MetadataFormat.JAVA).arrayType().of(STRING_METADATA).build();
  private static final MetadataType DATE_TIME_METADATA = create(MetadataFormat.JAVA).dateTimeType().build();
  private static final MetadataType BOOLEAN_METADATA = create(MetadataFormat.JAVA).booleanType().build();
  private static final MetadataType INTEGER_METADATA = create(MetadataFormat.JAVA).numberType().integer().build();
  private static final MetadataType NUMBER_METADATA = create(MetadataFormat.JAVA).numberType().build();

  private MetadataFactory() {}

  public static MetadataType from(final Shape shape) {

    Optional<MetadataType> metadataType = empty();

    if (shape instanceof AnyShape) {
      final AnyShape anyShape = (AnyShape) shape;

      final List<Example> examples = anyShape.examples();
      final TypeLoader typeLoader = anyShape.isDefaultEmpty() && !examples.isEmpty()
          ? createJsonExampleTypeLoader(examples.get(0).value().value()) : new JsonTypeLoader(anyShape.toJsonSchema());

      metadataType = typeLoader.load(null);
    }

    return metadataType.orElse(defaultMetadata());
  }

  static MetadataType defaultMetadata(final Shape shape) {
    if (shape instanceof FileShape)
      return stringMetadata();

    if (shape instanceof ArrayShape) {
      return arrayStringMetadata();
    }

    return defaultMetadata();
  }

  private static JsonExampleTypeLoader createJsonExampleTypeLoader(final String example) {
    final JsonExampleTypeLoader typeLoader = new JsonExampleTypeLoader(example);
    typeLoader.setFieldRequirementDefault(false);
    return typeLoader;
  }

  /**
  * Creates default metadata, that can be of any type
  * @return The newly created MetadataType
  */
  static MetadataType defaultMetadata() {
    return DEFAULT_METADATA;
  }

  /**
   * Creates metadata to describe an string type
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
