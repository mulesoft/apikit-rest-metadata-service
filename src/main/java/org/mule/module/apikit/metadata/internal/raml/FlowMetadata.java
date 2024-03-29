/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.internal.raml;

import org.mule.apikit.model.Action;
import org.mule.apikit.model.MimeType;
import org.mule.apikit.model.Response;
import org.mule.apikit.model.parameter.Parameter;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.FunctionTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.message.api.MessageMetadataType;
import org.mule.metadata.message.api.MessageMetadataTypeBuilder;
import org.mule.metadata.message.api.MuleEventMetadataType;
import org.mule.metadata.message.api.MuleEventMetadataTypeBuilder;
import org.mule.module.apikit.metadata.internal.model.ApiCoordinate;
import org.mule.module.apikit.metadata.internal.model.CertificateFields;
import org.mule.module.apikit.metadata.internal.model.HttpRequestAttributesFields;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.apikit.metadata.api.MetadataSource;
import org.mule.runtime.apikit.metadata.api.Notifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static org.mule.module.apikit.metadata.internal.utils.CommonMetadataFactory.defaultMetadata;
import static org.mule.runtime.api.metadata.MediaType.parse;

public class FlowMetadata implements MetadataSource {

  private static final String PARAMETER_INPUT_METADATA = "inputMetadata";
  private static final Pattern STATUS_CODE_2XX_PATTERN = Pattern.compile("^2\\d{2}$");

  final private Action action;
  final private ApiCoordinate coordinate;
  final private Map<String, Parameter> baseUriParameters;
  final private String httpStatusVar;
  final private String outboundHeadersVar;
  final private RamlApiWrapper api;
  final private Notifier notifier;

  public FlowMetadata(final RamlApiWrapper api, final Action action, final ApiCoordinate coordinate,
                      final Map<String, Parameter> baseUriParameters,
                      final String httpStatusVar,
                      final String outboundHeadersVar, Notifier notifier) {
    this.api = api;
    this.action = action;
    this.coordinate = coordinate;
    this.baseUriParameters = baseUriParameters;
    this.httpStatusVar = httpStatusVar;
    this.outboundHeadersVar = outboundHeadersVar;
    this.notifier = notifier;
  }

  @Override
  public Optional<FunctionType> getMetadata() {
    final MuleEventMetadataType inputEvent = inputMetadata(action, coordinate, baseUriParameters);
    final MuleEventMetadataType outputEvent = outputMetadata(action, coordinate, outboundHeadersVar, httpStatusVar);

    // FunctionType
    final FunctionTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).functionType();
    final FunctionType function = builder
        .addParameterOf(PARAMETER_INPUT_METADATA, inputEvent)
        .returnType(outputEvent)
        .build();

    return of(function);
  }

  private MuleEventMetadataType inputMetadata(final Action action, final ApiCoordinate coordinate,
                                              final Map<String, Parameter> baseUriParameters) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getInputPayload(action, coordinate))
        .attributes(getInputAttributes(action, baseUriParameters)).build();

    return new MuleEventMetadataTypeBuilder().message(message).build();
  }

  private MuleEventMetadataType outputMetadata(final Action action, final ApiCoordinate coordinate,
                                               final String outboundHeadersVar,
                                               String httpStatusVar) {
    final MessageMetadataType message = new MessageMetadataTypeBuilder()
        .payload(getOutputPayload(action, coordinate)).build();

    return new MuleEventMetadataTypeBuilder().message(message)
        .addVariable(outboundHeadersVar, getOutputHeaders(action).build())
        .addVariable(httpStatusVar, MetadataFactory.stringMetadata()).build();
  }

  private ObjectTypeBuilder getOutputHeaders(final Action action) {
    final Map<String, Parameter> headers = findFirstResponse(action).map(Response::getHeaders).orElse(emptyMap());

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    headers.forEach((name, value) -> builder.addField().key(name.toLowerCase()).value(value.getMetadata())
        .required(value.isRequired()));

    return builder;
  }

  private Optional<Response> findFirstResponse(final Action action) {
    return action.getResponses().entrySet().stream()
        .filter(response -> STATUS_CODE_2XX_PATTERN.matcher(response.getKey()).matches() && response.getValue().hasBody())
        .map(Map.Entry::getValue).findFirst();
  }

  private ObjectTypeBuilder getQueryParameters(Action action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getQueryParameters().forEach(
                                        (key, value) -> builder.addField().key(key).value(value.getMetadata())
                                            .required(value.isRequired()));

    return builder;
  }

  private ObjectTypeBuilder getInputHeaders(final Action action) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    action.getHeaders().forEach(
                                (key, value) -> builder.addField().key(key).value(value.getMetadata())
                                    .required(value.isRequired()));

    return builder;
  }

  private ObjectType getInputAttributes(final Action action, final Map<String, Parameter> baseUriParameters) {

    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_CLIENT_CERTIFICATE.getName())
        .required(false)
        .value(getClientCertificate());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_HEADERS.getName())
        .required(true)
        .value(getInputHeaders(action));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LISTENER_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_METHOD.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_PARAMS.getName())
        .required(true)
        .value(getQueryParameters(action));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_QUERY_STRING.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_RELATIVE_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REMOTE_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_PATH.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_REQUEST_URI.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_SCHEME.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_URI_PARAMS.getName())
        .required(true)
        .value(getUriParameters(action, baseUriParameters));
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_VERSION.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());
    builder.addField()
        .key(HttpRequestAttributesFields.ATTRIBUTES_LOCAL_ADDRESS.getName())
        .required(true)
        .value(MetadataFactory.stringMetadata());

    return builder.build();
  }

  private MetadataType getClientCertificate() {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_PUBLIC_KEY.getName()).value(MetadataFactory.objectMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_TYPE.getName()).value(MetadataFactory.stringMetadata());
    builder.addField().key(CertificateFields.CLIENT_CERTIFICATE_ENCODED.getName()).value(MetadataFactory.binaryMetadata());

    return builder.build();
  }

  private ObjectTypeBuilder getUriParameters(final Action action, Map<String, Parameter> baseUriParameters) {
    final ObjectTypeBuilder builder = BaseTypeBuilder.create(MetadataFormat.JAVA).objectType();

    baseUriParameters.forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata())
        .required(parameter.isRequired()));
    action.getResource().getResolvedUriParameters()
        .forEach((name, parameter) -> builder.addField().key(name).value(parameter.getMetadata())
            .required(parameter.isRequired()));

    return builder;
  }

  private MetadataType getOutputPayload(final Action action, final ApiCoordinate coordinate) {
    final Optional<Collection<MimeType>> mimeTypes = findFirstResponse(action)
        .map(response -> response.getBody().values());

    @Nullable
    final MimeType mimeType;
    if (mimeTypes.isPresent()) {
      mimeType = mimeTypes.get().stream().findFirst().orElse(null);
    } else {
      mimeType = null;
    }

    return loadIOPayloadMetadata(mimeType, coordinate, api, "output");
  }

  private MetadataType getInputPayload(final Action action, final ApiCoordinate coordinate) {
    @Nullable
    MimeType mimeType = null;

    if (action.hasBody()) {
      if (action.getBody().size() == 1) {
        mimeType = action.getBody().values().iterator().next();
      } else if (coordinate.getMediaType() != null) {
        MediaType mType = parse(coordinate.getMediaType());
        mimeType = action.getBody().entrySet().stream().filter(map -> parse(map.getKey()).matches(mType)).findFirst()
            .map(map -> map.getValue()).orElse(null);
      }
    }

    return loadIOPayloadMetadata(mimeType, coordinate, api, "input");
  }

  private MetadataType loadIOPayloadMetadata(final MimeType mimeType, final ApiCoordinate coordinate, final RamlApiWrapper api,
                                             final String payloadDescription) {
    try {
      return MetadataFactory.payloadMetadata(api, mimeType);
    } catch (Exception e) {
      notifier.warn(format("Error while trying to resolve %s payload metadata for flow '%s'.\nDetails: %s", payloadDescription,
                           coordinate.getFlowName(), e.getMessage()));
      return defaultMetadata();
    }
  }

}
