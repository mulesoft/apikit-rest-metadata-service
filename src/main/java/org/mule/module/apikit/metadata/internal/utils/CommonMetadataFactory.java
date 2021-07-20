/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.utils;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.xml.api.ModelFactory;
import org.mule.metadata.xml.api.SchemaCollector;
import org.mule.metadata.xml.api.XmlTypeLoader;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Optional;

import static org.mule.metadata.api.builder.BaseTypeBuilder.create;

public class CommonMetadataFactory {

  public static final String MIME_APPLICATION_JSON = "application/json";
  public static final String MIME_APPLICATION_XML = "application/xml";
  public static final String MIME_MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String MIME_APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

  private static final MetadataType DEFAULT_METADATA = create(MetadataFormat.JAVA).anyType().build();

  /**
   * Creates default metadata, that can be of any type
   *
   * @return The newly created MetadataType
   */
  public static MetadataType defaultMetadata() {
    return DEFAULT_METADATA;
  }

  /**
   * Loads metadata from XSD schema. Note that it takes into consideration a single schema so referenced schemas are not resolved.
   *
   * @param xsdSchema
   * @return
   */
  public static MetadataType fromXSDSchema(String xsdSchema) {
    final Optional<QName> rootElementName;
    try {
      rootElementName = getXmlSchemaRootElementName(xsdSchema);
    } catch (XmlException e) {
      return defaultMetadata();
    }
    return rootElementName.map(qName -> {
      /*
       * See
       * https://github.com/mulesoft/metadata-model-api/blob/d1b8147a487fb1986821276cd9fd4bb320124604/metadata-model-raml/src/main
       * /java/org/mule/metadata/raml/api/XmlRamlTypeLoader.java#L58
       */
      final XmlTypeLoader xmlTypeLoader = new XmlTypeLoader(SchemaCollector.getInstance().addSchema("", xsdSchema));
      return xmlTypeLoader.load(qName.toString()).orElse(defaultMetadata());
    }).orElse(defaultMetadata());
  }

  /**
   * Get root element from XSD schema. This code was copied from
   * {@link org.mule.metadata.xml.api.utils.XmlSchemaUtils#getXmlSchemaRootElementName(List, String)} in order to provide the
   * correct classloader.
   *
   * @param xsdSchema
   * @return
   * @throws XmlException
   */
  private static Optional<QName> getXmlSchemaRootElementName(String xsdSchema) throws XmlException {
    final XmlOptions options = new XmlOptions();
    options.setCompileNoUpaRule();
    options.setCompileNoValidation();
    options.setCompileDownloadUrls();

    /* Load the schema */
    final XmlObject[] schemaRepresentation = new XmlObject[1];
    final SchemaTypeLoader contextTypeLoader =
        SchemaTypeLoaderImpl.build(new SchemaTypeLoader[] {BuiltinSchemaTypeSystem.get()}, null,
                                   CommonMetadataFactory.class.getClassLoader());
    XmlObject schemaObject = contextTypeLoader.parse(xsdSchema, null, null);
    schemaRepresentation[0] = schemaObject;

    SchemaTypeSystemImpl schemaTypeSystem =
        SchemaTypeSystemCompiler.compile(null, null, schemaRepresentation, null, contextTypeLoader, null, options);
    final SchemaGlobalElement[] globalElements = schemaTypeSystem.globalElements();

    if (globalElements.length == 1) {
      return Optional.ofNullable(globalElements[0].getName());
    }
    return Optional.empty();
  }

  /**
   * Builds metadata from example if possible. If not possible, returns null.
   *
   * @param xmlExample
   * @return
   */
  public static MetadataType fromXMLExample(String xmlExample) {
    try {
      ModelFactory modelFactory = ModelFactory.fromExample(xmlExample);
      return new XmlTypeLoader(modelFactory).load(null).orElse(null);
    } catch (Exception | AssertionError e) {
      return null;
    }
  }
}
