/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.runtime.core.api.extension.ExtensionManager;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This test is ignored on purpose. There are differences between JAVA & AMF Metadata. Running this test you can undestand this
 * differences, some are in favor of AMF, some in favor of JAVA
 */
@RunWith(Parameterized.class)
@Ignore
public class MetadataCompatibilityTestCase extends AbstractMetadataTestCase {

  private File app;
  private String flow;


  public MetadataCompatibilityTestCase(final File app, final String flow) {
    this.app = app;
    this.flow = flow;
  }

  @Test
  public void compatibilityMetadata() {
    final File javaGoldenFile = goldenFile(flow, app, RAML);
    final File amfGoldenFile = goldenFile(flow, app, AMF);

    final String javaMetadata = readFile(javaGoldenFile.toPath());
    final String amfMetadata = readFile(amfGoldenFile.toPath());

    assertThat(format("Java/AMF metadata differ. App: '%s' Flow: '%s'", app.getParentFile().getName(), flow),
               javaMetadata,
               is(equalTo(amfMetadata)));
  }

  @Parameterized.Parameters(name = "{0}-{2}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {

    final List<Object[]> parameters = new ArrayList<>();

    scanApps().forEach(app -> {
      try {
        final String folderName = app.getParentFile().getName();
        // TODO : remove ignore and inject ExtensionManager
        findFlows(app, null).forEach(flow -> parameters.add(new Object[] {folderName, app, flow}));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return parameters;
  }
}
