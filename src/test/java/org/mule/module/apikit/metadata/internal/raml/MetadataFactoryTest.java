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
import org.mule.apikit.implv2.v10.model.ParameterImpl;
import org.mule.apikit.model.MimeType;
import org.mule.metadata.api.model.MetadataType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  }

  @Test
  public void testPayloadMetadata_ApplicationJson_SchemaNull() {
    when(mimeType.getType()).thenReturn("application/json");
    when(mimeType.getExample()).thenReturn("{\"key\": \"value\"}");
    MetadataType result = MetadataFactory.payloadMetadata(api, mimeType);
    assertNotNull(result);
  }

  @Test
  public void testPayloadMetadata_ApplicationJson_EmptyBody() {
    when(mimeType.getType()).thenReturn("application/json");
    MetadataType result = MetadataFactory.payloadMetadata(api, mimeType);
    assertNotNull(result);
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
  public void testPayloadMetadata_MultipartFormData() {
    String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
    String multipartSchema = "{\n" +
        "  \"type\": \"object\",\n" +
        "  \"properties\": {\n" +
        "    \"username\": {\"type\": \"string\"},\n" +
        "    \"email\": {\"type\": \"string\", \"format\": \"email\"},\n" +
        "    \"profile_picture\": {\"type\": \"string\", \"format\": \"binary\"}\n" +
        "  },\n" +
        "  \"required\": [\"username\", \"email\", \"profile_picture\"]\n" +
        "}";

    String multipartExample = "------" + boundary + "\n" +
        "Content-Disposition: form-data; name=\"username\"\n\n" +
        "john_doe\n" +
        "------" + boundary + "\n" +
        "Content-Disposition: form-data; name=\"email\"\n\n" +
        "john_doe@example.com\n" +
        "------" + boundary + "\n" +
        "Content-Disposition: form-data; name=\"profile_picture\"; filename=\"profile.jpg\"\n" +
        "Content-Type: image/jpeg\n\n" +
        "<binary image data>\n" +
        "------" + boundary + "--";

    when(mimeType.getType()).thenReturn("multipart/form-data");
    when(mimeType.getSchema()).thenReturn(multipartSchema);
    when(mimeType.getExample()).thenReturn(multipartExample);
    TypeDeclaration usernameTypeDeclaration = mock(TypeDeclaration.class);
    when(usernameTypeDeclaration.name()).thenReturn("username");
    when(usernameTypeDeclaration.type()).thenReturn("string");
    ParameterImpl usernameParameter = new ParameterImpl(usernameTypeDeclaration);
    Map<String, List<Parameter>> formParameters = new HashMap<>();
    formParameters.put("username", Collections.singletonList(usernameParameter));
    when(mimeType.getFormParameters()).thenReturn(formParameters);

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
    when(mimeType.getType()).thenReturn("application/unknown");
    MetadataType result = MetadataFactory.payloadMetadata(api, mimeType);
    assertNotNull(result);
  }
}
