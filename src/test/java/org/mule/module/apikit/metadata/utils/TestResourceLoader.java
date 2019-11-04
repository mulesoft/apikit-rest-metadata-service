/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.mule.module.apikit.metadata.MetadataTestCase;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;

public class TestResourceLoader implements ResourceLoader {

  @Override
  public URI getResource(String resource) {
    try {
      URL url = MetadataTestCase.class.getResource(isSyncProtocol(resource) ? getTestPathForApiSync(resource) : resource);
      return url == null ? null : url.toURI();
    } catch (final URISyntaxException e) {
      e.printStackTrace();
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static String getTestPathForApiSync(String relativePath) throws URISyntaxException {
    String[] parts = relativePath.split(":");


    if (parts.length < 8) {
      throw new URISyntaxException("APISync Resource loader doesn't recognize this path",
                                   "Not enough parts at url. Expected 8 actual " + parts.length);
    }

    String type = parts[5];
    String artifactId = parts[3];
    String groupId = parts[2];
    String fileName = parts[7];
    String version = parts[4];
    if (type.equals("raml") || type.equals("oas") || type.equals("oas-yaml")) {
      return artifactId + "/" + fileName;
    }
    if (type.equals("raml-fragment")) {
      return groupId + "/exchange_modules/" + groupId + "/" + artifactId + "/" + version + "/"
          + fileName;
    }

    throw new URISyntaxException("Resource loader doesn't recognize this path", "Invalid type " + type);
  }

}
