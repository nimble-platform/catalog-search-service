package eu.nimble.service.catalog.search.impl;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import de.biba.triple.store.access.dmo.Entity;
import de.biba.triple.store.access.enums.Language;
import de.biba.triple.store.access.jena.Reader;
import de.biba.triple.store.access.marmotta.MarmottaReader;
import eu.nimble.service.catalog.search.impl.dao.CustomPropertyInformation;
import eu.nimble.service.catalog.search.impl.dao.enums.PropertySource;
import eu.nimble.service.catalog.search.impl.dao.output.OutputForPropertiesFromConcept;
import eu.nimble.service.catalog.search.mediator.MediatorSPARQLDerivationAndExecution;
import eu.nimble.service.catalog.search.mediator.NimbleSpecificSPARQLDeriviationAndExecution;
import eu.nimble.service.catalog.search.services.SQPDerivationService;

public class TestNimbleSpecificSPARQLDeriviation {

	private static final String C_ONTOLOGY_FURNITURE_TAXONOMY_V1_4_BIBA_OWL = "C:/ontology/FurnitureSectorTaxonomy-v1.8_BIBA.owl";

	@Ignore
	@Test
	public void testcreateSPARQLForAllDomainSpecificProperties() {
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(null,
				null, null);
		String sparql = deriviation.createSPARQLForAllDomainSpecificProperties();
		System.out.println(sparql);

	}

	@Ignore
	@Test
	public void testgetPropertyValuesForOrangeGroup() {
		File file = new File("./src/main/resources/sqpConfiguration.xml");
		if (file.exists()) {
			Reader reader = new Reader();
			reader.setModeToLocal();
			reader.loadOntologyModel(C_ONTOLOGY_FURNITURE_TAXONOMY_V1_4_BIBA_OWL);

			SQPDerivationService sqpDerivationService = new SQPDerivationService(null,
					"./src/main/resources/sqpConfiguration.xml");
			NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
					reader, sqpDerivationService, null);
			String command = "companyName";
			String concept = "http://www.aidimme.es/FurnitureSectorOntology.owl#HighChair";
			eu.nimble.service.catalog.search.impl.dao.output.OutputForPropertyValuesFromOrangeGroup result = deriviation
					.getPropertyValuesForOrangeGroup(command, concept,false);
			System.out.println(result.getAllValues());

		} else {
			fail("No configuration file found");
		}
	}

	//@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.nimble-project.org/resource/eclass/22292803",
				"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2#Name", PropertySource.DIRECT_PROPERTIES);
		System.out.println(result);
	}

	
	@Test
	public void testgetAllDifferentValuesForAProperty_DomainSpecific() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.nimble-project.org/resource/eclass/22292803",
				"http://www.aidimme.es/FurnitureSectorOntology.owl#hasColour", PropertySource.DOMAIN_SPECIFIC_PROPERTY);
		System.out.println(result);
	}
	
	
	@Test
	public void testdetectNimbleSpecificMeaningFromAKeyword() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<Entity> result = deriviation.detectNimbleSpecificMeaningFromAKeyword("fruit", "http://www.w3.org/2004/02/skos/core#prefLabel", Language.ENGLISH);
		System.out.println(result);
	}
	
	@Test
	public void testdetectNimbleSpecificMeaningFromAKeyword_MDFBOARD() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<Entity> result = deriviation.detectNimbleSpecificMeaningFromAKeyword("MDF laminated", "http://www.w3.org/2004/02/skos/core#prefLabel", Language.ENGLISH);
		System.out.println(result);
	}
	
	@Ignore
	@Test
	public void testdetectNimbleSpecificMeaningFromAKeywordRefrringInstance() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<Entity> result = deriviation.detectNimbleSpecificMeaningFromAKeywordReferringToInstances("MDF laminated", "http://www.w3.org/2004/02/skos/core#prefLabel", Language.ENGLISH,false);
		System.out.println(result);
	}
	
	@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty_Dimension() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.nimble-project.org/resource/eclass/22292803",
				"custom_dimension", PropertySource.DIMENSION);
		System.out.println(result);
	}
	@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty_Custom() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.nimble-project.org/resource/eclass/22292803",
				"custom_prop", PropertySource.CUSTOM_STRING);
		System.out.println(result);
	}
	
	@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty_Custom_DECIMAL() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.aidimme.es/FurnitureSectorOntology.owl#Varnish",

				"drying time", PropertySource.CUSTOM_DECIMAL);
		System.out.println(result);
	}
	@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty_MANUFACTURERPARTY() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.nimble-project.org/resource/eclass/22292803",
				"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2#ManufacturerParty", PropertySource.DIRECT_PROPERTIES);

		System.out.println(result);
	}
	@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty_MANUFACTURERPARTYIDENTIFICATION() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);



		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.aidimme.es/FurnitureSectorOntology.owl#Varnish",
				"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2#ManufacturersItemIdentification", PropertySource.DIRECT_PROPERTIES);
		System.out.println(result);
	}


	@Ignore
	@Test
	public void testgetAllDifferentValuesForAProperty_Description() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);

		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAllDifferentValuesForAProperty(
				"http://www.aidimme.es/FurnitureSectorOntology.owl#Varnish",
				"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2#Description", PropertySource.DIRECT_PROPERTIES);
		System.out.println(result);
	}
	
	
	
	@Test
	public void testgetAdditionalPropertiesWhichAreDerivedFromAbox() {
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		List<String> result = deriviation.getAdditionalPropertiesWhichAreDerivedFromAbox(
				"http://www.nimble-project.org/resource/eclass/22292803");
		System.out.println(result);
	}
	

	

	@Test
	public void testgetAllAvailableCustomPropertiesWhichAreDerivedFromAbox (){
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);

		List<CustomPropertyInformation> result = deriviation.getAllAvailableCustomPropertiesWhichAreDerivedFromAbox("http://www.nimble-project.org/resource/eclass/22292803");
		System.out.println(result);
	
	}
	

	@Test
	public void testgetAllAvailableDimensionsPropertiesWhichAreDerivedFromAbox (){
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);

		List<String> result = deriviation.getAllAvailableDimensionsWhichAreDerivedFromAbox("http://www.nimble-project.org/resource/eclass/22292803");
		System.out.println(result);
	
	}
	
	@Ignore
	@Test
	public void testgetAllAvailableEClassOrDomainProperties(){
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);

		List<String> result = deriviation.getAllAvailableEClassOrDomainPropertiesFromAbox("http://www.nimble-project.org/resource/eclass/22292803");

		System.out.println(result);
	}
	@Ignore
	@Test
	public void testgetAllPropertiesIncludingEverything(){
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		OutputForPropertiesFromConcept result = deriviation.getAllPropertiesIncludingEverything("http://www.aidimme.es/FurnitureSectorOntology.owl#Varnish");
		System.out.println(result.getOutputForPropertiesFromConcept());
	}

	@Ignore
	@Test
	public void testgetAllPropertiesIncludingEverythingEClass(){
		MarmottaReader reader = new MarmottaReader("https://nimble-platform.salzburgresearch.at/marmotta");
		MediatorSPARQLDerivationAndExecution mediatorSPARQLDerivationAndExecution = null;
		SQPDerivationService sqpDerivationService = new SQPDerivationService(mediatorSPARQLDerivationAndExecution,
				"./src/main/resources/sqpConfiguration.xml");
		mediatorSPARQLDerivationAndExecution = new MediatorSPARQLDerivationAndExecution(
				"https://nimble-platform.salzburgresearch.at/marmotta", true, sqpDerivationService);
		NimbleSpecificSPARQLDeriviationAndExecution deriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
				reader, sqpDerivationService, mediatorSPARQLDerivationAndExecution);
		OutputForPropertiesFromConcept result = deriviation.getAllPropertiesIncludingEverything("http://www.nimble-project.org/resource/eclass/22292803");
		System.out.println(result.getOutputForPropertiesFromConcept());
	}

}
