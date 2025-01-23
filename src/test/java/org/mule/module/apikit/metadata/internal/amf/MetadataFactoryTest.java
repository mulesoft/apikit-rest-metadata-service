/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.amf;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import amf.apicontract.client.platform.AMFConfiguration;
import amf.apicontract.client.platform.AMFElementClient;
import amf.core.client.platform.model.domain.Shape;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mule.metadata.api.model.*;
import amf.shapes.client.platform.model.domain.*;
import amf.apicontract.client.platform.APIConfiguration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIConfiguration.class})
public class MetadataFactoryTest {

  @Test
  void testFromJSONSchemaWithEmptyShape() {
    Shape emptyShape = mock(Shape.class);
    MetadataType result = MetadataFactory.fromJSONSchema(emptyShape, "");
    assertTrue(result instanceof AnyType);
  }

  @Test
  void testDefaultMetadataWithFileShape() {
    FileShape fileShape = mock(FileShape.class);
    MetadataType result = MetadataFactory.defaultMetadata(fileShape);
    assertTrue(result instanceof StringType);
  }

  @Test
  void testDefaultMetadataWithArrayShape() {
    ArrayShape arrayShape = mock(ArrayShape.class);
    MetadataType result = MetadataFactory.defaultMetadata(arrayShape);
    assertTrue(result instanceof ArrayType);
    ArrayType arrayType = (ArrayType) result;
    assertTrue(arrayType.getType() instanceof StringType);
  }

  @Test
  void testStringMetadata() {
    MetadataType result = MetadataFactory.stringMetadata();
    assertTrue(result instanceof StringType);
  }

  @Test
  void testArrayStringMetadata() {
    MetadataType result = MetadataFactory.arrayStringMetadata();
    assertTrue(result instanceof ArrayType);
    ArrayType arrayType = (ArrayType) result;
    assertTrue(arrayType.getType() instanceof StringType);
  }

  @Test
  void testBooleanMetadata() {
    MetadataType result = MetadataFactory.booleanMetadada();
    assertTrue(result instanceof BooleanType);
  }

  @Test
  void testNumberMetadata() {
    MetadataType result = MetadataFactory.numberMetadata();
    assertTrue(result instanceof NumberType);
  }

  @Test
  void testIntegerMetadata() {
    MetadataType result = MetadataFactory.integerMetadata();
    assertTrue(result instanceof NumberType);
  }

  @Test
  void testDateTimeMetadata() {
    MetadataType result = MetadataFactory.dateTimeMetadata();
    assertTrue(result instanceof DateTimeType);
  }

  @Test
  void testObjectMetadata() {
    MetadataType result = MetadataFactory.objectMetadata();
    assertTrue(result instanceof ObjectType);
  }

  @Test
  void testBinaryMetadata() {
    MetadataType result = MetadataFactory.binaryMetadata();
    assertTrue(result instanceof BinaryType);
  }
}
