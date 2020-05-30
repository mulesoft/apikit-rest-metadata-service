/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import amf.client.model.StrField;
import amf.client.model.domain.AnyShape;
import amf.client.model.domain.EndPoint;
import amf.client.model.domain.Example;
import amf.client.model.domain.NodeShape;
import amf.client.model.domain.Operation;
import amf.client.model.domain.Parameter;
import amf.client.model.domain.Payload;
import amf.client.model.domain.PropertyShape;
import amf.client.model.domain.Request;
import amf.client.model.domain.Response;
import amf.client.model.domain.Shape;
import amf.client.model.domain.WebApi;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.amf.impl.AMFParser;
import org.mule.apikit.model.api.ApiReference;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.internal.utils.MetadataTypeWriter;
import org.mule.metadata.json.api.JsonTypeLoader;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore
public class AmfTestCase {

  @Test
  public void endPointsRaml10() {

    final WebApi apiModel = webApi(resource("uri-params-in-raml10/api.raml"));

    final List<EndPoint> endPoints = apiModel.endPoints();
    assertThat("Number of EndPoints differs.", endPoints.size(), is(equalTo(4)));

    assertThat(endPoints.get(0).path().value(), is(equalTo("/part")));
    assertThat(endPoints.get(1).path().value(), is(equalTo("/part/{uriParam1}")));
    assertThat(endPoints.get(2).path().value(), is(equalTo("/part/{uriParam1}/{uriParam2}")));
    assertThat(endPoints.get(3).path().value(), is(equalTo("/part/{uriParam1}/{uriParam2}/{uriParam3}")));

    // TODO Wrong Order !!!. This test will failed when AMF fix it
    List<Parameter> parameters = endPoints.get(2).parameters();
    assertThat(parameters.size(), is(equalTo(2)));
    assertThat(parameters.get(0).name().value(), is(equalTo("uriParam1")));
    assertThat(parameters.get(1).name().value(), is(equalTo("uriParam2")));

    parameters = endPoints.get(3).parameters();
    assertThat(parameters.size(), is(equalTo(3)));
    assertThat(parameters.get(0).name().value(), is(equalTo("uriParam1")));
    assertThat(parameters.get(1).name().value(), is(equalTo("uriParam2")));
    assertThat(parameters.get(2).name().value(), is(equalTo("uriParam3")));
  }

  @Test
  public void queryParamsRaml08() {

    final WebApi apiModel = webApi(resource("query-params-in-raml08/api.raml"));

    final List<EndPoint> endPoints = apiModel.endPoints();
    endPoints.forEach(endPoint -> endPoint.operations().forEach(operation -> {
      final Request request = operation.request();
      if (request != null)
        request.queryParameters().forEach(parameter -> {
          final Shape schema = parameter.schema();
          if (schema instanceof AnyShape) {
            try {
              final String jsonSchema = ((AnyShape) schema).buildJsonSchema();
              System.out.println("Parameter " + parameter.name() + ":");
              System.out.println(jsonSchema);
              System.out.println();
              final JsonTypeLoader typeLoader = new JsonTypeLoader(jsonSchema);
              final Optional<MetadataType> metadataType = typeLoader.load(null);
              final String metadata = new MetadataTypeWriter().toString(metadataType.get());
              System.out.println(metadata);
              System.out.println("--------------------------------------------------------------\n");

            } // TODO remove try catch when AMF fix it
            catch (final Throwable e) {
              System.out.println("Falló la obtencion de JsonSchema de " + schema.name() + " " + schema.getClass() + "\n"
                  + e.getMessage());
            }
          }
        });
    }));

  }

  @Test
  public void jsonSchemaFromMultipartRaml08() {

    final WebApi webApi = webApi(resource("api-in-raml08/api.raml"));

    final Optional<Operation> op = findOperation(webApi, "/multipart", "post");
    assertThat(op.isPresent(), is(true));
    op.ifPresent(operation -> {
      final Request request = operation.request();
      assertThat(request, notNullValue());
      final List<Payload> payloads = request.payloads();
      assertThat(payloads.size(), is(equalTo(1)));

      final Payload payload = payloads.get(0);
      final Shape schema = payload.schema();
      if (schema instanceof AnyShape) {
        final List<PropertyShape> properties = ((NodeShape) schema).properties();
        properties.forEach(p -> System.out.println(p.name()));

      }
    });
  }

  @Test
  public void jsonSchemaFromFileRaml10() {

    final WebApi apiModel = webApi(resource("query-params-in-raml10/api.raml"));

    final List<EndPoint> endPoints = apiModel.endPoints();
    endPoints.forEach(endPoint -> endPoint.operations().forEach(operation -> {
      final Request request = operation.request();
      if (request != null)
        request.queryParameters().forEach(parameter -> {
          final Shape schema = parameter.schema();
          if (schema instanceof AnyShape) {
            try {
              final String jsonSchema = ((AnyShape) schema).buildJsonSchema();
            } // TODO remove try catch when AMF fix it
            catch (final Throwable e) {
              System.out
                  .println("Error getting JsonSchema for " + schema.name() + " " + schema.getClass() + "\n" + e.getMessage());
            }
          }
        });
    }));
  }

  // TODO  https://www.mulesoft.org/jira/browse/APIMF-904
  @Test
  public void remoteRaml10() throws URISyntaxException {

    final URI uri =
        new URI("https://raw.githubusercontent.com/mulesoft/apikit/M4-1.x/mule-apikit-module/src/test/resources/org/mule/module/apikit/router-remote-raml/remote.raml");
    webApi(uri.toString());
  }

  @Test
  public void queryParamsOrderRaml08() {

    final WebApi webApi = webApi(resource("sanity-test/api.raml"));

    final Optional<Operation> op = findOperation(webApi, "/clients", "get");
    assertThat(op.isPresent(), is(true));
    op.ifPresent(operation -> {
      final Request request = operation.request();
      assertThat(request, notNullValue());
      final List<String> parameters = request.queryParameters().stream().map(o -> o.name().value()).collect(toList());
      assertThat(mkString(parameters), equalTo("code,size,color,description"));
    });
    //dump(webApi);
  }

  @Test
  public void clientsMetadataRaml08() {

    final WebApi webApi = webApi(resource("sanity-test/api.raml"));

    final Optional<Operation> op = findOperation(webApi, "/clients", "get");

    assertThat(op.isPresent(), is(true));

    dump(op.get());

    op.ifPresent(operation -> {
      final List<Response> responses = operation.responses();
      responses.forEach(response -> {
        final List<Payload> payloads = response.payloads();
        payloads.forEach(payload -> {
          final Shape schema = payload.schema();
          assertThat(schema, notNullValue());

        });
      });
    });
    //dump(webApi);
  }



  private static void dump(final WebApi webApi) {
    final List<EndPoint> endPoints = webApi.endPoints();
    endPoints.forEach(AmfTestCase::dump);
  }

  private static void dump(final EndPoint endPoint) {
    final List<String> methods = endPoint.operations().stream().map(o -> o.method().value()).collect(toList());
    System.out.println("Endpoint Path:" + endPoint.path() + " Operations=" + mkString(methods));
    endPoint.operations().forEach(AmfTestCase::dump);
  }

  private static void dump(final Operation operation) {
    final List<String> ct = operation.contentType().stream().map(StrField::value).collect(toList());
    System.out.println("\tOperation: " + operation.method().value() + " contentTypes:" + mkString(ct));

    final Request request = operation.request();
    if (request != null) {
      System.out.println("\t\tRequest:");
      if (!request.queryParameters().isEmpty()) {
        final List<String> params = request.queryParameters().stream().map(o -> o.name().value()).collect(toList());
        System.out.println("\t\t\tqueryParameters=" + mkString(params));
      }
    }

    System.out.println("\t\tRequest:");
    final List<Response> responses = operation.responses();
    responses.forEach(response -> {
      final List<Payload> payloads = response.payloads();
      payloads.forEach(payload -> {
        final Shape schema = payload.schema();
        assertThat(schema, notNullValue());

        if (schema instanceof AnyShape) {
          AnyShape anyShape = (AnyShape) schema;
          System.out.println("\t\t\tJsonSchema:\n" + anyShape.buildJsonSchema());
          final List<Example> examples = anyShape.examples();
          System.out.println("\t\t\tExamples:");
          examples.forEach(example -> {
            System.out.println("\t\t\t\t" + example.value().value());
            System.out.println();
          });
        }
      });
    });
  }

  private static Optional<EndPoint> findEndPoint(final WebApi webApi, final String path) {
    return webApi.endPoints().stream()
        .filter(e -> e.path().value().equals(path)).findFirst();
  }

  private static Optional<Operation> findOperation(final WebApi webApi, final String path, final String method) {
    return webApi.endPoints().stream()
        .filter(e -> e.path().value().equals(path)).findFirst()
        .flatMap(endPoint -> endPoint.operations().stream()
            .filter(operation -> operation.method().value().equalsIgnoreCase(method)).findFirst());
  }

  private static String mkString(final List<String> list) {
    return list.stream().map(Object::toString).collect(Collectors.joining(","));
  }

  private static WebApi webApi(final String path) {
    ApiReference apiRef = ApiReference.create(path);
    AMFParser parserWrapper = new AMFParser(apiRef, true);
    final WebApi webApi = parserWrapper.getWebApi();
    assertThat(webApi, notNullValue());
    return webApi;
  }

  private static String resource(final String resource) {

    final URL url = AmfTestCase.class.getResource(resource);
    try {
      final URI uri = url.toURI();
      return uri.toString();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error getting URI from resource" + resource, e);
    }
  }
}
