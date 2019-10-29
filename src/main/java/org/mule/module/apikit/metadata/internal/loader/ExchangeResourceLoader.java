/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.loader;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;

public class ExchangeResourceLoader implements ResourceLoader {

  private File workingDir;
  private static final Pattern DEPENDENCY_PATH_PATTERN = Pattern.compile("^exchange_modules/|/exchange_modules/");

  public ExchangeResourceLoader(ResourceLoader resourceLoader, String rootFile) {
    URI rootPath = resourceLoader.getResource(rootFile);
    this.workingDir = rootPath != null ? new File(rootPath).getParentFile() : null;
  }

  @Override
  public URI getResource(String relativePath) {
    if (isNullOrEmpty(relativePath)) {
      return null;
    }
    if (workingDir != null && workingDir.exists()) {
      final String resourceName;
      final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(relativePath);
      if (matcher.find()) {
        final int dependencyIndex = relativePath.lastIndexOf(matcher.group(0));
        resourceName =
            dependencyIndex <= 0 ? relativePath : relativePath.substring(dependencyIndex);
        return Paths.get(workingDir.getPath(), resourceName).toUri();
      }
    }
    return null;
  }
}
