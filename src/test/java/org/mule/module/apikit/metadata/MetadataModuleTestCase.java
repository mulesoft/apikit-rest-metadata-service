/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mule.module.apikit.metadata.utils.TestNotifier.DEBUG;
import static org.mule.module.apikit.metadata.utils.TestNotifier.ERROR;
import static org.mule.module.apikit.metadata.utils.TestNotifier.INFO;
import static org.mule.module.apikit.metadata.utils.TestNotifier.WARN;

import org.mule.metadata.api.model.FunctionType;
import org.mule.module.apikit.metadata.internal.MetadataBuilderImpl;
import org.mule.module.apikit.metadata.utils.MockedApplicationModel;
import org.mule.module.apikit.metadata.utils.TestNotifier;
import org.mule.module.apikit.metadata.utils.TestResourceLoader;
import org.mule.runtime.apikit.metadata.api.Metadata;
import org.mule.runtime.apikit.metadata.api.Notifier;
import org.mule.runtime.apikit.metadata.api.ResourceLoader;
import org.mule.runtime.ast.api.ArtifactAst;

import java.util.Optional;

import org.junit.Test;

public class MetadataModuleTestCase {

  @Test
  public void testBasicMetadataModule() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    Notifier notifier = new TestNotifier();

    ArtifactAst applicationModel = createApplicationModel("org/mule/module/apikit/metadata/flow-mappings/app.xml");
    assertThat(applicationModel, notNullValue());

    Metadata metadata = new MetadataBuilderImpl()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();

    Optional<FunctionType> createNewBookFlow = metadata.getMetadataForFlow("createNewBook");
    Optional<FunctionType> getAllBooks = metadata.getMetadataForFlow("get:\\books:router-config");
    Optional<FunctionType> flowMappingDoesNotExist = metadata.getMetadataForFlow("flowMappingDoesNotExist");
    Optional<FunctionType> petshopApiGetCustomers = metadata.getMetadataForFlow("get:\\customers\\pets:petshop-api");
    Optional<FunctionType> petShopApiCreateCustomer = metadata.getMetadataForFlow("post:\\customers:petshop-api");

    assertThat(createNewBookFlow.isPresent(), is(true));
    assertThat(getAllBooks.isPresent(), is(true));
    assertThat(flowMappingDoesNotExist.isPresent(), is(false));
    assertThat(petShopApiCreateCustomer.isPresent(), is(true));
    assertThat(petshopApiGetCustomers.isPresent(), is(true));
  }


  @Test
  public void singleApiWithFlowsWithoutConfigRef() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    Notifier notifier = new TestNotifier();

    ArtifactAst applicationModel =
        createApplicationModel("org/mule/module/apikit/metadata/single-api-with-no-name/app.xml");
    assertThat(applicationModel, notNullValue());

    Metadata metadata = new MetadataBuilderImpl()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();

    Optional<FunctionType> getAllCustomersPets = metadata.getMetadataForFlow("get:\\customers\\pets");
    Optional<FunctionType> createCustomer = metadata.getMetadataForFlow("post:\\customers");

    assertThat(getAllCustomersPets.isPresent(), is(true));
    assertThat(createCustomer.isPresent(), is(true));
  }

  @Test
  public void ramlApplicationInRaml08() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    Notifier notifier = new TestNotifier();

    ArtifactAst applicationModel = createApplicationModel("org/mule/module/apikit/metadata/api-in-raml08/app.xml");
    assertThat(applicationModel, notNullValue());

    Metadata metadata = new MetadataBuilderImpl()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();

    Optional<FunctionType> putResources = metadata.getMetadataForFlow("put:\\resources:application\\json:router-config");
    Optional<FunctionType> getResources = metadata.getMetadataForFlow("get:\\resources:router-config");
    Optional<FunctionType> postResources = metadata.getMetadataForFlow("post:\\resources:router-config");
    Optional<FunctionType> postUrlEncoded =
        metadata.getMetadataForFlow("post:\\url-encoded:application\\x-www-form-urlencoded:router-config");
    Optional<FunctionType> postMultipart = metadata.getMetadataForFlow("post:\\multipart:multipart\\form-data:router-config");

    assertThat(putResources.isPresent(), is(true));
    assertThat(getResources.isPresent(), is(true));
    assertThat(postResources.isPresent(), is(true));
    assertThat(postUrlEncoded.isPresent(), is(true));
    assertThat(postMultipart.isPresent(), is(true));
  }

  @Test
  public void testNotifyingOnlyInfoMesages() throws Exception {

    ResourceLoader resourceLoader = new TestResourceLoader();
    TestNotifier notifier = new TestNotifier();

    ArtifactAst applicationModel = createApplicationModel("org/mule/module/apikit/metadata/api-in-raml08/app.xml");
    assertThat(applicationModel, notNullValue());

    final Metadata metadata = getMetadata(resourceLoader, notifier, applicationModel);

    assertNotifierMessages(notifier, 0, 0, 0, 0);

    metadata.getMetadataForFlow("get:\\resources:router-config");
    assertNotifierMessages(notifier, 0, 0, 1, 0);
  }

  @Test
  public void testNotifyingOnlyErrorMessages() throws Exception {
    ResourceLoader resourceLoader = new TestResourceLoader();
    TestNotifier notifier = new TestNotifier();

    ArtifactAst model = createApplicationModel("org/mule/module/apikit/metadata/invalid-raml-file-location/app.xml");
    assertThat(model, notNullValue());

    Metadata metadata;

    metadata = getMetadata(resourceLoader, notifier, model);
    assertNotifierMessages(notifier, 0, 0, 0, 0);

    metadata.getMetadataForFlow("get:\\flow1:router-config");
    assertNotifierMessages(notifier, 2, 0, 0, 0);

    notifier = new TestNotifier();
    metadata = getMetadata(resourceLoader, notifier, model);
    metadata.getMetadataForFlow("get:\\flow2:router-config");
    assertNotifierMessages(notifier, 2, 0, 0, 0);

    notifier = new TestNotifier();
    metadata = getMetadata(resourceLoader, notifier, model);
    metadata.getMetadataForFlow("get:\\flow3:router-config");
    assertNotifierMessages(notifier, 2, 0, 0, 0);

  }

  private Metadata getMetadata(ResourceLoader resourceLoader, TestNotifier notifier, ArtifactAst model) {
    return new MetadataBuilderImpl()
        .withApplicationModel(model)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier)
        .build();
  }

  private ArtifactAst createApplicationModel(String resourceName) throws Exception {
    final MockedApplicationModel.Builder builder = new MockedApplicationModel.Builder();
    builder.addConfig("apiKitSample", getClass().getClassLoader().getResourceAsStream(resourceName));
    final MockedApplicationModel mockedApplicationModel = builder.build();
    return mockedApplicationModel.getMuleApplicationModel();
  }


  private static void assertNotifierMessages(TestNotifier notifier, int error, int warning, int info, int debug) {
    assertThat(notifier.messages(ERROR).size(), is(error));
    assertThat(notifier.messages(WARN).size(), is(warning));
    assertThat(notifier.messages(INFO).size(), is(info));
    assertThat(notifier.messages(DEBUG).size(), is(debug));
  }
}
