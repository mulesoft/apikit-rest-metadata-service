/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.utils.MetadataFixer;
import org.mule.module.apikit.metadata.utils.MetadataTypeWriter;
import org.mule.module.apikit.metadata.utils.MockedApplicationModel;
import org.mule.module.apikit.metadata.utils.TestNotifier;
import org.mule.module.apikit.metadata.utils.TestResourceLoader;
import org.mule.runtime.apikit.metadata.api.Metadata;
import org.mule.runtime.apikit.metadata.api.MetadataBuilder;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.core.api.extension.ExtensionManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public abstract class AbstractMetadataTestCase extends MuleArtifactFunctionalTestCase {

  protected static final String AMF = "AMF";
  protected static final String RAML = "RAML";

  private static final PathMatcher API_MATCHER = FileSystems.getDefault().getPathMatcher("glob:app.xml");

  protected static List<File> scanApps() throws IOException, URISyntaxException {
    final URI baseFolder =
        new File(AbstractMetadataTestCase.class.getResource("/anchor.test.resource.file").toURI()).getParentFile().toURI();
    return scan(baseFolder);
  }

  @Override
  protected String getConfigFile() {
    return "mule-config.xml";
  }

  protected static List<File> scan(final URI resources) throws IOException {

    return Files.walk(Paths.get(resources))
        // .peek(path -> System.out.println("Path:" + path + " isMuleApp:" + API_MATCHER.matches(path.getFileName())))
        .filter(path -> Files.isRegularFile(path) && API_MATCHER.matches(path.getFileName()))
        .map(Path::toFile)
        .collect(toList());
  }

  protected static ArtifactAst createApplicationModel(final File app, ExtensionManager extensionManager) throws Exception {
    MockedApplicationModel mockedApplicationModel = new MockedApplicationModel.Builder()
        .addConfig(app)
        .extensionManager(extensionManager)
        .build();
    return mockedApplicationModel.getMuleApplicationModel();
  }

  protected static List<String> findFlows(final File app, ExtensionManager extensionManager) throws Exception {
    final ArtifactAst applicationModel = createApplicationModel(app, extensionManager);

    // Only APIKit flows
    return applicationModel.topLevelComponentsStream()
        .filter(componentAst -> componentAst.getIdentifier().getNamespace().equals("mule") &&
            componentAst.getIdentifier().getName().equals("flow"))
        .map(componentAst -> (String) componentAst.getParameter("General", "name").getValue().getRight())
        .filter(flow -> isApikitFlow(flow))
        .collect(toList());
  }

  private static boolean isApikitFlow(final String name) {
    return name.startsWith("get:") || name.startsWith("post:") || name.startsWith("put:") ||
        name.startsWith("delete:") || name.startsWith("head:") || name.startsWith("patch:") ||
        name.startsWith("options:") || name.startsWith("trace:") || name.startsWith("connect:");

  }

  protected static Optional<FunctionType> getMetadata(MetadataBuilder metadataBuilder, final ArtifactAst applicationModel,
                                                      final String flow) {

    final Metadata metadata = metadataBuilder
        .withApplicationModel(applicationModel)
        .withResourceLoader(new TestResourceLoader())
        .withNotifier(new TestNotifier()).build();
    return metadata.getMetadataForFlow(flow);
  }

  protected static String metadataToString(String parser, final FunctionType functionType) {
    final String result = new MetadataTypeWriter().toString(functionType);
    return AMF.equals(parser) ? MetadataFixer.normalizeEnums(result) : result;
  }

  protected File goldenFile(final String flow, final File app, final String parser) {
    final String fileName = flow
        .replace("\\", "")
        .replace(":", "-") + ".out";

    final File parserFolder = new File(app.getParentFile(), parser.toLowerCase());
    return new File(parserFolder, fileName);
  }

  protected static String readFile(final Path path) {
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static Path createGoldenFile(final File goldenFile, final String content) throws IOException {

    final String srcPath = goldenFile.getPath().replace("target/test-classes", "src/test/resources");
    final Path goldenPath = Paths.get(srcPath);
    System.out.println("*** Create Golden " + goldenPath);

    // Write golden files with current values
    final Path parent = goldenPath.getParent();
    if (!Files.exists(parent)) {
      Files.createDirectory(parent);
    }
    return Files.write(goldenPath, content.getBytes("UTF-8"));
  }
}
