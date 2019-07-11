/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;

import java.net.URI;
import java.util.List;
import java.util.Optional;

// THIS CLASS WAS COPIED FROM git@github.com:mulesoft/mule-datasense-api.git
interface ApplicationModel {

  /**
   *
   * @param name
   * @return
   */
  Optional<ComponentAst> findNamedComponent(String name);

  List<String> findTypesDataList();

  ArtifactAst getMuleApplicationModel();

  default Optional<URI> findResource(String resource) {
    return Optional.empty();
  }
}
