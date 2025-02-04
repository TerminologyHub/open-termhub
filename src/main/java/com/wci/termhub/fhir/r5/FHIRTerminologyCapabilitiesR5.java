/*
 * Copyright 2025 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.termhub.fhir.r5;

import java.util.Collections;

import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r5.model.ContactDetail;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.TerminologyCapabilities;

import ca.uhn.fhir.model.api.annotation.ChildOrder;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

/**
 * Specification of the FHIR TerminologyCapabilities.
 */
@ResourceDef(name = "TerminologyCapabilities", profile = "http://hl7.org/fhir/StructureDefinition/TerminologyCapabilities")
@ChildOrder(names = { "url", "version", "name", "title", "status", "experimental", "date", "publisher", "contact",
		"description", "useContext", "jurisdiction", "purpose", "copyright", "kind", "software", "implementation",
		"lockedDate", "codeSystem", "expansion", "codeSearch", "validateCode", "translation", "closure" })
public class FHIRTerminologyCapabilitiesR5 extends TerminologyCapabilities implements IBaseConformance {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * With defaults.
	 *
	 * @return the FHIR terminology capabilities
	 */
	public FHIRTerminologyCapabilitiesR5 withDefaults() {
		setContact();
		setCodeSystem();
		setName("TermhubTerminologyCapabilities");
		setStatus(PublicationStatus.DRAFT);
		setTitle("TermHub Terminology Capability Statement");
		setVersion(getClass().getPackage().getImplementationVersion());
		return this;
	}

	/**
	 * Sets the code system.
	 */
	private void setCodeSystem() {
		final TerminologyCapabilitiesCodeSystemComponent tccsc = new TerminologyCapabilitiesCodeSystemComponent();
		tccsc.setUri("http://terminologyhub.com");
		setCodeSystem(Collections.singletonList(tccsc));
	}

	/**
	 * Sets the contact.
	 */
	private void setContact() {
		final ContactPoint contactPoint = new ContactPoint();
		contactPoint.setSystem(ContactPointSystem.EMAIL);
		contactPoint.setValue("info@terminologyhub.com");
		final ContactDetail contactDetail = new ContactDetail();
		contactDetail.addTelecom(contactPoint);
		setContact(Collections.singletonList(contactDetail));
	}

}
