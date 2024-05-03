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
import org.junit.jupiter.params.provider.Arguments;
import org.mule.metadata.api.model.FunctionType;
import org.mule.runtime.apikit.metadata.api.MetadataBuilder;
import org.mule.runtime.apikit.metadata.api.MetadataService;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.core.api.extension.ExtensionManager;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mule.module.apikit.metadata.internal.MetadataBuilderImpl.MULE_APIKIT_PARSER;

public class MetadataTestCase extends AbstractMetadataTestCase {

  @Inject
  private ExtensionManager extensionManager;

  // SET TO TRUE IF YOU WANT THE TESTS THAT MISS MACH TO GENERATE A NEW FILE
  private final boolean generateFixedFiles = false;

  // TODO: Add support for injecting extensionManager in JUnit5
  @ParameterizedTest
  @MethodSource("getData")
  @Disabled()
  public void checkMetadata(String parser, File app) throws Exception {
    System.setProperty(MULE_APIKIT_PARSER, parser);

    if (app.getAbsolutePath().contains("oas") && parser.equals(RAML)) {
      return;
    }

    for (String flow : findFlows(app, extensionManager)) {
      final File goldenFile = goldenFile(flow, app, parser);

      final ArtifactAst applicationModel = createApplicationModel(app, extensionManager);
      assertThat(applicationModel, notNullValue());

      MetadataService service = getService(MetadataService.class);
      MetadataBuilder apikitMetadataBuilder = service.getApikitMetadataBuilder();
      final Optional<FunctionType> metadata = getMetadata(apikitMetadataBuilder, applicationModel, flow);

      if (isInvalidFileLocation(app)) {
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
    System.clearProperty(MULE_APIKIT_PARSER);
  }

  private String relativePath(File goldenFile) {
    try {
      File base = new File(this.getClass().getClassLoader().getResource("").toURI());
      return base.toURI().relativize(goldenFile.toURI()).getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static Stream<Arguments> getData() throws IOException, URISyntaxException {
    return scanApps().stream()
        .flatMap(app -> {
          try {
            return Stream.of(
                             Arguments.of(RAML, app),
                             Arguments.of(AMF, app));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  private boolean isInvalidFileLocation(File app) {
    return app.getPath().contains("invalid-raml-file-location");
  }
}
