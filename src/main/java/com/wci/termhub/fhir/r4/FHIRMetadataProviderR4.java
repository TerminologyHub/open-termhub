/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r4;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationDefinition;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wci.termhub.fhir.rest.r4.FhirUtilityR4;
import com.wci.termhub.fhir.util.FHIRServerResponseException;
import com.wci.termhub.util.PropertyUtility;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestfulServerConfiguration;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * See https://www.hl7.org/fhir/terminologycapabilities.html. See
 * https://smilecdr.com/hapi-fhir/docs/server_plain/introduction.html#capability-statement-server-metadata.
 * Call using GET [base]/metadata?mode=terminology. See
 * https://github.com/jamesagnew/hapi-fhir/issues/1681.
 */
public class FHIRMetadataProviderR4 extends ServerCapabilityStatementProvider {
  /**
   * The logger.
   */
  private static Logger logger = LoggerFactory.getLogger(FHIRMetadataProviderR4.class);

  /**
   * Instantiates a new FHIR terminology capabilities provider.
   *
   * @param theServer the server
   */
  public FHIRMetadataProviderR4(final RestfulServer theServer) {
    super(theServer);
  }

  /**
   * Instantiates a new FHIR terminology capabilities provider.
   *
   * @param theContext the context
   * @param theServerConfiguration the server configuration
   */
  public FHIRMetadataProviderR4(final FhirContext theContext,
      final RestfulServerConfiguration theServerConfiguration) {
    super(theContext, theServerConfiguration);
  }

  /**
   * Instantiates a new FHIR terminology capabilities provider.
   *
   * @param theRestfulServer the restful server
   * @param theSearchParamRegistry the search param registry
   * @param theValidationSupport the validation support
   */
  public FHIRMetadataProviderR4(final RestfulServer theRestfulServer,
      final ISearchParamRegistry theSearchParamRegistry,
      final IValidationSupport theValidationSupport) {
    super(theRestfulServer, theSearchParamRegistry, theValidationSupport);
  }

  /**
   * Gets the metadata resource.
   *
   * @param request the request
   * @param requestDetails the request details
   * @return the metadata resource
   * @throws Exception the exception
   */
  // cacheMills is to ensure the results of this call are NOT cached
  @Metadata(cacheMillis = 0)
  public IBaseConformance getMetadataResource(final HttpServletRequest request,
    final RequestDetails requestDetails) throws Exception {

    try {
      final String mode = request.getParameter("mode");
      // Check if the request is for the terminology mode
      // TerminologyCapabilities
      if (mode != null && "terminology".equals(mode)) {

        return new FHIRTerminologyCapabilitiesR4().withDefaults(request, requestDetails);
      }

      // Check if the request is for general CapabilityStatement
      else {
        final IBaseConformance ibc = super.getServerConformance(request, requestDetails);
        final CapabilityStatement capabilityStatement = (CapabilityStatement) ibc;
        final CanonicalType instantiateUri =
            new CanonicalType("http://hl7.org/fhir/CapabilityStatement/terminology-server");

        capabilityStatement.setInstantiates(Collections.singletonList(instantiateUri));
        final String version =
            String.valueOf(PropertyUtility.getProperties().get("server.version"));
        capabilityStatement
            .setSoftware(new CapabilityStatement.CapabilityStatementSoftwareComponent()
                .setName("OPENTERMHUB").setVersion(version).setReleaseDate(new Date()));

        capabilityStatement.setUrl("https://www.terminologyhub.com/fhir/4/metadata");
        capabilityStatement.setVersion(version);
        capabilityStatement.setName("OPENTERMHUBFHIRTerminologyServer");
        capabilityStatement.setTitle("Open Termhub R4 FHIR Terminology Server");
        capabilityStatement.setStatus(Enumerations.PublicationStatus.ACTIVE);
        capabilityStatement.setExperimental(true);
        capabilityStatement.setPublisher("TERMHUB");
        capabilityStatement.setDate(new Date());

        addExtensions(capabilityStatement);

        // Find the "rest" component with mode = "server"
        final Optional<CapabilityStatement.CapabilityStatementRestComponent> serverRestComponent =
            capabilityStatement.getRest().stream()
                .filter(rest -> "server".equalsIgnoreCase(rest.getMode().toString())).findFirst();

        if (serverRestComponent.isPresent()) {
          final CapabilityStatement.CapabilityStatementRestComponent rest =
              serverRestComponent.get();
          CapabilityStatement.CapabilityStatementRestSecurityComponent security =
              rest.getSecurity();
          if (security.isEmpty()) {
            security = new CapabilityStatement.CapabilityStatementRestSecurityComponent();
            rest.setSecurity(security);
          }

          // Create the CodeableConcept for the "service" property
          final CodeableConcept serviceCodeableConcept = new CodeableConcept();
          serviceCodeableConcept.addCoding(
              new Coding("http://terminology.hl7.org/CodeSystem/restful-security-service",
                  "OAuth2.0", "OAuth 2.0")); // Example coding

          // Set the "service" property
          security.setService(Collections.singletonList(serviceCodeableConcept));
        }
        return capabilityStatement;
      }
    } catch (final FHIRServerResponseException e) {
      throw e;
    } catch (final Exception e) {
      logger.error("Unexpected FHIR error", e);
      throw FhirUtilityR4.exception("Failed to find metadata",
          org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION, 500);
    }
  }

  /**
   * Adds the extensions.
   *
   * @param capabilityStatement the capability statement
   */
  private void addExtensions(final CapabilityStatement capabilityStatement) {
    // First Extension
    final Extension featureExtension1 =
        new Extension("http://hl7.org/fhir/uv/application-feature/StructureDefinition/feature");

    final Extension definition1 = new Extension("definition",
        new CanonicalType("http://hl7.org/fhir/uv/tx-tests/FeatureDefinition/test-version"));
    final Extension value1 = new Extension("value", new CodeType("1.7.5"));

    featureExtension1.addExtension(definition1);
    featureExtension1.addExtension(value1);

    capabilityStatement.addExtension(featureExtension1);

    final Extension featureExtension2 =
        new Extension("http://hl7.org/fhir/uv/application-feature/StructureDefinition/feature");

    final Extension definition2 = new Extension("definition", new CanonicalType(
        "http://hl7.org/fhir/uv/tx-ecosystem/FeatureDefinition/CodeSystemAsParameter"));
    final Extension value2 = new Extension("value", new BooleanType("true"));

    featureExtension2.addExtension(definition2);
    featureExtension2.addExtension(value2);

    capabilityStatement.addExtension(featureExtension2);
  }

  /**
   * Read OperationDefinition resources.
   *
   * @param theId the the id
   * @return the operation definition
   */
  @Read(type = OperationDefinition.class)
  public OperationDefinition readOperationDefinition(@IdParam final IdType theId) {
    if ("versions".equals(theId.getIdPart()) || "-versions".equals(theId.getIdPart())) {
      return createVersionsOperationDefinition();
    }
    // Also handle the full URL case
    if (theId.hasBaseUrl()
        && theId.getValue().equals("http://hl7.org/fhir/OperationDefinition/-versions")) {
      return createVersionsOperationDefinition();
    }
    throw new ResourceNotFoundException("OperationDefinition/" + theId.getIdPart());
  }

  /**
   * System-level $versions operation.
   *
   * @param theRequest the the request
   * @param theResponse the the response
   * @param theRequestDetails the the request details
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  @Operation(name = "$versions", manualResponse = true, manualRequest = true, idempotent = true)
  public void versions(final HttpServletRequest theRequest, final HttpServletResponse theResponse,
    final RequestDetails theRequestDetails) throws Exception {

    // Create your version response
    final Parameters parameters = new Parameters();
    final String version = String.valueOf(PropertyUtility.getProperties().get("server.version"));
    parameters.addParameter().setName("version").setValue(new StringType(version));
    parameters.addParameter().setName("fhirVersion").setValue(new StringType("4.0.0"));

    // Return the response
    final FhirContext fhirContext = theRequestDetails.getFhirContext();
    final IParser parser = fhirContext.newJsonParser();
    theResponse.setContentType("application/fhir+json");
    theResponse.getWriter().write(parser.encodeResourceToString(parameters));
  }

  /**
   * Creates the versions operation definition.
   *
   * @return the operation definition
   */
  private OperationDefinition createVersionsOperationDefinition() {
    final OperationDefinition opDef = new OperationDefinition();
    opDef.setId("versions");
    opDef.setUrl("http://hl7.org/fhir/OperationDefinition/-versions");
    opDef.setName("versions");
    opDef.setTitle("Get Terminology Server Version Information");
    opDef.setStatus(Enumerations.PublicationStatus.ACTIVE);
    opDef.setKind(OperationDefinition.OperationKind.OPERATION);
    opDef.setCode("versions");
    opDef.setSystem(true);
    opDef.setType(false);
    opDef.setInstance(false);

    // Add parameters as needed for your version operation
    final OperationDefinition.OperationDefinitionParameterComponent param =
        new OperationDefinition.OperationDefinitionParameterComponent();
    param.setName("return");
    param.setUse(OperationDefinition.OperationParameterUse.OUT);
    param.setMin(1);
    param.setMax("1");
    param.setType("Parameters");
    opDef.addParameter(param);

    return opDef;
  }
}
