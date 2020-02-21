/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.module.apikit.metadata.internal.MetadataBuilderImpl.MULE_APIKIT_PARSER;

import org.mule.metadata.api.model.FunctionType;
import org.mule.runtime.apikit.metadata.api.MetadataBuilder;
import org.mule.runtime.apikit.metadata.api.MetadataService;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class MetadataTestCase extends AbstractMetadataTestCase {

  private final String parser;
  private final File app;

  // SET TO TRUE IF YOU WANT THE TESTS THAT MISS MACH TO GENERATE A NEW FILE
  private final boolean generateFixedFiles = false;

  public MetadataTestCase(final String parser, final File app) {
    this.parser = parser;
    this.app = app;
  }

  @Before
  public void beforeTest() {
    System.setProperty(MULE_APIKIT_PARSER, parser);
  }

  @After
  public void afterTest() {
    System.clearProperty(MULE_APIKIT_PARSER);
  }

  @Test
  public void checkMetadata() throws Exception {
    if (app.getAbsolutePath().contains("oas") && parser.equals(RAML))
      return;

    for (String flow : findFlows(app)) {
      final File goldenFile = goldenFile(flow, app, parser);

      final ArtifactAst applicationModel = createApplicationModel(app);
      assertThat(applicationModel, notNullValue());

      MetadataService service = getService(MetadataService.class);
      MetadataBuilder apikitMetadataBuilder = service.getApikitMetadataBuilder();
      final Optional<FunctionType> metadata = getMetadata(apikitMetadataBuilder, applicationModel, flow);

      if (isInvalidFileLocation()) {
        assertThat(metadata.isPresent(), is(false));
        return;
      }

      assertThat(metadata.isPresent(), is(true));

      final String current = metadataToString(parser, metadata.get()).trim();

      final Path goldenPath = goldenFile.exists() ? goldenFile.toPath() : createGoldenFile(goldenFile, current);
      final String expected = readFile(goldenPath).trim();

      try {
        assertThat("Metadata differ from expected on flow: " + flow + ". File: " + relativePath(goldenFile), current,
                   is(expected));
      } catch (final AssertionError error) {
        final String name = goldenFile.getName();
        final File folder = goldenFile.getParentFile();
        if (generateFixedFiles) {
          final File newGoldenFile = new File(folder, name + ".fixed");
          createGoldenFile(newGoldenFile, current);
        }
        throw error;
      }
    }
  }

  private String relativePath(File goldenFile) {
    try {
      File base = new File(this.getClass().getClassLoader().getResource("").toURI());
      return base.toURI().relativize(goldenFile.toURI()).getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Parameterized.Parameters(name = "{0} -> {1}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {

    final List<Object[]> parameters = new ArrayList<>();

    scanApps().forEach(app -> {
      try {
        parameters.add(new Object[] {RAML, app});
        parameters.add(new Object[] {AMF, app});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return parameters;
  }

  private boolean isInvalidFileLocation() {
    return app.getPath().contains("invalid-raml-file-location");
  }
}
