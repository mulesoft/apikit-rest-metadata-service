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
import org.junit.Test;
import org.mule.metadata.api.model.*;
import amf.shapes.client.platform.model.domain.*;
import amf.apicontract.client.platform.APIConfiguration;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIConfiguration.class})
public class MetadataFactoryTest {

  @Test
  public void testFromJSONSchema_withAnyShapeAndExample() {

    AnyShape anyShape = mock(AnyShape.class);
    String example = "{\"key\": \"value\"}";

    AMFConfiguration apiConfigurationMock = mock(AMFConfiguration.class);
    AMFElementClient elementClientMock = mock(AMFElementClient.class);
    PowerMockito.mockStatic(APIConfiguration.class);
    when(APIConfiguration.API()).thenReturn(apiConfigurationMock);
    when(apiConfigurationMock.elementClient()).thenReturn(elementClientMock);
    when(elementClientMock.buildJsonSchema(anyShape)).thenReturn("{\"type\": \"object\"}");

    MetadataType result = MetadataFactory.fromJSONSchema(anyShape, example);

    assertNotNull(result);
    assertTrue(result instanceof ObjectType);
  }

  @Test
  public void testFromJSONSchemaWithEmptyShape() {
    Shape emptyShape = mock(Shape.class);
    MetadataType result = MetadataFactory.fromJSONSchema(emptyShape, "");
    assertTrue(result instanceof AnyType);
  }

  @Test
  public void testDefaultMetadataWithFileShape() {
    FileShape fileShape = mock(FileShape.class);
    MetadataType result = MetadataFactory.defaultMetadata(fileShape);
    assertTrue(result instanceof StringType);
  }

  @Test
  public void testDefaultMetadataWithArrayShape() {
    ArrayShape arrayShape = mock(ArrayShape.class);
    MetadataType result = MetadataFactory.defaultMetadata(arrayShape);
    assertTrue(result instanceof ArrayType);
    ArrayType arrayType = (ArrayType) result;
    assertTrue(arrayType.getType() instanceof StringType);
  }

  @Test
  public void testStringMetadata() {
    MetadataType result = MetadataFactory.stringMetadata();
    assertTrue(result instanceof StringType);
  }

  @Test
  public void testArrayStringMetadata() {
    MetadataType result = MetadataFactory.arrayStringMetadata();
    assertTrue(result instanceof ArrayType);
    ArrayType arrayType = (ArrayType) result;
    assertTrue(arrayType.getType() instanceof StringType);
  }

  @Test
  public void testBooleanMetadata() {
    MetadataType result = MetadataFactory.booleanMetadada();
    assertTrue(result instanceof BooleanType);
  }

  @Test
  public void testNumberMetadata() {
    MetadataType result = MetadataFactory.numberMetadata();
    assertTrue(result instanceof NumberType);
  }

  @Test
  public void testIntegerMetadata() {
    MetadataType result = MetadataFactory.integerMetadata();
    assertTrue(result instanceof NumberType);
  }

  @Test
  public void testDateTimeMetadata() {
    MetadataType result = MetadataFactory.dateTimeMetadata();
    assertTrue(result instanceof DateTimeType);
  }

  @Test
  public void testObjectMetadata() {
    MetadataType result = MetadataFactory.objectMetadata();
    assertTrue(result instanceof ObjectType);
  }

  @Test
  public void testBinaryMetadata() {
    MetadataType result = MetadataFactory.binaryMetadata();
    assertTrue(result instanceof BinaryType);
  }
}
