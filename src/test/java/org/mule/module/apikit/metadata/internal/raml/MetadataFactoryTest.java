/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.apikit.model.MimeType;
import org.mule.metadata.api.model.MetadataType;
import org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

public class MetadataFactoryTest {

  @Mock
  private RamlApiWrapper api;
  @Mock
  private MimeType mimeType;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPayloadMetadata_ApplicationJson() {
    when(mimeType.getType()).thenReturn("application/json");
    when(mimeType.getSchema()).thenReturn("{\"type\": \"object\"}");
    when(mimeType.getExample()).thenReturn("{\"key\": \"value\"}");
    MetadataType result = MetadataFactory.payloadMetadata(api, mimeType);
    assertNotNull(result);
    // Add more assertions based on the expected MetadataType
  }

  @Test
  public void testPayloadMetadata_ApplicationXsd() {
    String xmlSchema = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "  <xs:element name=\"root\">" +
        "    <xs:complexType>" +
        "      <xs:sequence>" +
        "        <xs:element name=\"child\" type=\"xs:string\"/>" +
        "      </xs:sequence>" +
        "    </xs:complexType>" +
        "  </xs:element>" +
        "</xs:schema>";
    when(mimeType.getType()).thenReturn("application/xml");
    when(mimeType.getSchema()).thenReturn(xmlSchema);
    when(mimeType.getExample()).thenReturn("<root><child>value</child></root>");
    MetadataType result = MetadataFactory.payloadMetadata(api, mimeType);
    assertNotNull(result);
  }

  @Test
  public void testPayloadMetadata_ApplicationXml_WithExample() {
    String example = "<root><child>value</child></root>";
    when(mimeType.getType()).thenReturn("application/xml");
    when(mimeType.getSchema()).thenReturn(null);
    when(mimeType.getExample()).thenReturn(example);
    MetadataType result = MetadataFactory.payloadMetadata(api, mimeType);
    assertNotNull(result);
  }

  @Test
  public void testPayloadMetadata_NullBody() {
    MetadataType result = MetadataFactory.payloadMetadata(api, null);
    assertNotNull(result);
  }
  // @Test
  // public void testResolveSchema() {
  // Map<String, String> consolidatedSchemas = new HashMap<>();
  // consolidatedSchemas.put("schemaName", "<schema>XMLSchema</schema>");
  // when(api.getConsolidatedSchemas()).thenReturn(consolidatedSchemas);
  // when(mimeType.getSchema()).thenReturn("schemaName");
  // String result = MetadataFactory.resolveSchema(api, mimeType);
  // assertEquals("<schema>XMLSchema</schema>", result);
  // }
}
