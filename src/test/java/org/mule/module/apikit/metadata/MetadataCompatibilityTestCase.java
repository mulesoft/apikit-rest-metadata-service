/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This test is ignored on purpose. There are differences between JAVA & AMF Metadata. Running this test you can understand these
 * differences, some are in favor of AMF, some in favor of JAVA
 */
@Disabled
public class MetadataCompatibilityTestCase extends AbstractMetadataTestCase {

  @ParameterizedTest
  @MethodSource("getData")
  public void compatibilityMetadata(String flow, File app) {
    final File javaGoldenFile = goldenFile(flow, app, RAML);
    final File amfGoldenFile = goldenFile(flow, app, AMF);

    final String javaMetadata = readFile(javaGoldenFile.toPath());
    final String amfMetadata = readFile(amfGoldenFile.toPath());

    assertThat(format("Java/AMF metadata differ. App: '%s' Flow: '%s'", app.getParentFile().getName(), flow),
               javaMetadata,
               is(equalTo(amfMetadata)));
  }

  public static Stream<Object[]> getData() {
    List<Object[]> parameters = new ArrayList<>();

    try {
      scanApps().forEach(app -> {
        try {
          // TODO : remove ignore and inject ExtensionManager
          findFlows(app, null).forEach(flow -> {
            parameters.add(new Object[] {flow, app});
          });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }

    return parameters.stream();
  }
}
