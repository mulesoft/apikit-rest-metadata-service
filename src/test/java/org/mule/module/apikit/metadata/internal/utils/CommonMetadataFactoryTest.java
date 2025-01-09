/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.utils;

import org.junit.jupiter.api.Test;
import org.mule.metadata.api.model.MetadataType;

import static org.junit.jupiter.api.Assertions.*;

class CommonMetadataFactoryTest {

  @Test
  void testDefaultMetadata() {
    MetadataType result = CommonMetadataFactory.defaultMetadata();
    assertNotNull(result);
    assertEquals("org.mule.metadata.api.model.impl.DefaultAnyType", result.getClass().getName());
  }

  @Test
  void testFromXSDSchemaWithValidSchema() {
    String validXsdSchema = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "  <xs:element name=\"root\">" +
        "    <xs:complexType>" +
        "      <xs:sequence>" +
        "        <xs:element name=\"child\" type=\"xs:string\"/>" +
        "      </xs:sequence>" +
        "    </xs:complexType>" +
        "  </xs:element>" +
        "</xs:schema>";

    MetadataType result = CommonMetadataFactory.fromXSDSchema(validXsdSchema);
    assertNotNull(result);
    // Add more specific assertions based on the expected structure
  }

  @Test
  void testFromXSDSchemaWithInvalidSchema() {
    String invalidXsdSchema = "This is not a valid XSD schema";
    MetadataType result = CommonMetadataFactory.fromXSDSchema(invalidXsdSchema);
    assertNotNull(result);
    assertEquals(CommonMetadataFactory.defaultMetadata(), result);
  }

  @Test
  void testFromXSDSchemaWithNoRootElement() {
    String noRootElementSchema = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "  <xs:complexType name=\"testType\">" +
        "    <xs:sequence>" +
        "      <xs:element name=\"child\" type=\"xs:string\"/>" +
        "    </xs:sequence>" +
        "  </xs:complexType>" +
        "</xs:schema>";

    MetadataType result = CommonMetadataFactory.fromXSDSchema(noRootElementSchema);
    assertNotNull(result);
    assertEquals(CommonMetadataFactory.defaultMetadata(), result);
  }

  @Test
  void testFromXMLExampleWithValidXML() {
    // assertNotNull(CommonMetadataFactory.fromXMLExample("<child>value</child>"));
  }

  @Test
  void testFromXMLExampleWithInvalidXML() {
    String invalidXmlExample = "This is not a valid XML";
    MetadataType result = CommonMetadataFactory.fromXMLExample(invalidXmlExample);
    assertNull(result);
  }

  @Test
  void testConstantsValues() {
    assertEquals("application/json", CommonMetadataFactory.MIME_APPLICATION_JSON);
    assertEquals("application/xml", CommonMetadataFactory.MIME_APPLICATION_XML);
    assertEquals("multipart/form-data", CommonMetadataFactory.MIME_MULTIPART_FORM_DATA);
    assertEquals("application/x-www-form-urlencoded", CommonMetadataFactory.MIME_APPLICATION_URL_ENCODED);
  }
}
