/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.module.apikit.metadata.internal.MetadataBuilderImpl.MULE_APIKIT_PARSER;

import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.internal.utils.MetadataTypeWriter;
import org.mule.module.apikit.metadata.internal.model.Flow;
import org.mule.runtime.ast.api.ArtifactAst;

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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MetadataTestCase extends AbstractMetadataTestCase {

  private final String parser;
  private final File app;
  private final Flow flow;

  // SET TO TRUE IF YOU WANT THE TESTS THAT MISS MACH TO GENERATE A NEW FILE
  private boolean generateFixedFiles = true;

  public MetadataTestCase(final String parser, final String folderName, final File app, final Flow flow) {
    this.parser = parser;
    this.app = app;
    this.flow = flow;
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

    final File goldenFile = goldenFile(flow, app, parser);

    final ArtifactAst applicationModel = createApplicationModel(app);
    assertThat(applicationModel, notNullValue());

    final Optional<FunctionType> metadata = getMetadata(applicationModel, flow);

    if (isInvalidFileLocation()) {
      assertThat(metadata.isPresent(), is(false));
      return;
    }

    assertThat(metadata.isPresent(), is(true));

    final String current = metadataToString(parser, metadata.get()).trim();

    final Path goldenPath = goldenFile.exists() ? goldenFile.toPath() : createGoldenFile(goldenFile, current);
    final String expected = readFile(goldenPath).trim();

    try {
      assertThat("Metadata differ from expected. File: " + goldenFile.getName(), current, is(expected));
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

  @Parameterized.Parameters(name = "{0} -> {1}-{3}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {

    final List<Object[]> parameters = new ArrayList<>();

    scanApps().forEach(app -> {
      try {
        final String folderName = app.getParentFile().getName();
        findFlows(app).forEach(flow -> {
          parameters.add(new Object[] {RAML, folderName, app, flow});
          parameters.add(new Object[] {AMF, folderName, app, flow});
        });
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
