package eu.nimble.service.catalog.search.mediator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.sparql.function.library.uuid;

import de.biba.triple.store.access.IPropertyValuesCrawler;
import de.biba.triple.store.access.IReader;
import de.biba.triple.store.access.dmo.Entity;
import de.biba.triple.store.access.enums.Language;
import de.biba.triple.store.access.enums.PropertyType;
import de.biba.triple.store.access.jena.PropertyValuesCrawler;
import de.biba.triple.store.access.jena.Reader;
import de.biba.triple.store.access.marmotta.MarmottaPropertyValuesCrawler;
import de.biba.triple.store.access.marmotta.MarmottaReader;
import eu.nimble.service.catalog.search.factories.ValueGroupingFactory;
import eu.nimble.service.catalog.search.impl.dao.DataPoint;
import eu.nimble.service.catalog.search.impl.dao.Filter;
import eu.nimble.service.catalog.search.impl.dao.Group;
import eu.nimble.service.catalog.search.impl.dao.LocalOntologyView;
import eu.nimble.service.catalog.search.impl.dao.enums.PropertySource;
import eu.nimble.service.catalog.search.impl.dao.input.InputParamaterForExecuteOptionalSelect;
import eu.nimble.service.catalog.search.impl.dao.input.InputParamaterForExecuteSelect;
import eu.nimble.service.catalog.search.impl.dao.input.InputParameterForGetReferencesFromAConcept;
import eu.nimble.service.catalog.search.impl.dao.input.InputParameterForPropertyValuesFromOrangeGroup;
import eu.nimble.service.catalog.search.impl.dao.input.InputParameterdetectMeaningLanguageSpecific;
import eu.nimble.service.catalog.search.impl.dao.input.Parameter;
import eu.nimble.service.catalog.search.impl.dao.input.Tuple;
import eu.nimble.service.catalog.search.impl.dao.output.OutputForExecuteSelect;
import eu.nimble.service.catalog.search.impl.dao.output.OutputForPropertiesFromConcept;
import eu.nimble.service.catalog.search.impl.dao.output.OutputForPropertyFromConcept;
import eu.nimble.service.catalog.search.impl.dao.output.OutputForPropertyValuesFromOrangeGroup;
import eu.nimble.service.catalog.search.impl.dao.output.TranslationResult;
import eu.nimble.service.catalog.search.impl.dao.sqp.SQPConfiguration;
import eu.nimble.service.catalog.search.services.SQPDerivationService;

public class MediatorSPARQLDerivationAndExecution {

	public static final String FURNITURE2_OWL = "furniture2.owl";
	private IReader reader = null;
	private IPropertyValuesCrawler propertyValuesCrawler = null;
	private String languagelabel = null;
	private NimbleSpecificSPARQLDeriviationAndExecution nimbleSpecificSPARQLDeriviation = null;
	private SQPDerivationService sqpDerivationService = null;

	public MediatorSPARQLDerivationAndExecution() {
		File f = new File(FURNITURE2_OWL);
		if (f.exists()) {
			initForSpecificOntology(FURNITURE2_OWL);
		} else {
			Logger.getAnonymousLogger().log(Level.WARNING, "Cannot load default ontology: " + FURNITURE2_OWL);
		}
	}

	public MediatorSPARQLDerivationAndExecution(String pntologyFile) {
		initForSpecificOntology(pntologyFile);

	}

	public void updatesqpDerivationService(SQPDerivationService sqpDerivationService) {
		this.nimbleSpecificSPARQLDeriviation = new NimbleSpecificSPARQLDeriviationAndExecution(reader,
				sqpDerivationService, this);
		this.sqpDerivationService = sqpDerivationService;
	}

	public MediatorSPARQLDerivationAndExecution(String uri, boolean remote, SQPDerivationService sqpDerivationService) {
		if (!remote) {
			initForSpecificOntology(uri);
			updatesqpDerivationService(sqpDerivationService);

		} else {
			reader = new MarmottaReader(uri);
			propertyValuesCrawler = new MarmottaPropertyValuesCrawler(uri);
			reader.setLanguageLabel(languagelabel);
			this.nimbleSpecificSPARQLDeriviation = new NimbleSpecificSPARQLDeriviationAndExecution(
					(MarmottaReader) reader, sqpDerivationService, this);
			this.sqpDerivationService = sqpDerivationService;
		}

	}

	public void initForMarmotta(String url, SQPDerivationService sqpDerivationService) {
		reader = new MarmottaReader(url);
		reader.setModeToRemote();
		reader.setLanguageLabel(languagelabel);
		this.nimbleSpecificSPARQLDeriviation = new NimbleSpecificSPARQLDeriviationAndExecution((MarmottaReader) reader,
				sqpDerivationService, this);
		this.sqpDerivationService = sqpDerivationService;
	}

	public void initForSpecificOntology(String pntologyFile) {

		reader = new Reader();
		reader.setModeToLocal();
		reader.loadOntologyModel(pntologyFile);
		reader.setLanguageLabel(languagelabel);

		propertyValuesCrawler = new PropertyValuesCrawler();
		propertyValuesCrawler.setModeToLocal();
		propertyValuesCrawler.loadOntologyModel(pntologyFile);

		this.nimbleSpecificSPARQLDeriviation = new NimbleSpecificSPARQLDeriviationAndExecution(reader,
				sqpDerivationService, this);
		this.sqpDerivationService = sqpDerivationService;
	}

	public OutputForExecuteSelect createSPARQLAndExecuteIT(
			InputParamaterForExecuteSelect inputParamaterForExecuteSelect) {

		extendRequestedPropertiesWithRespectToOrangeCommands(inputParamaterForExecuteSelect);

		if (!(reader instanceof MarmottaReader)) {

			String sparql = createSparql(inputParamaterForExecuteSelect);
			Logger.getAnonymousLogger().log(Level.INFO, sparql);

			Object ouObject = reader.query(sparql);
			// This is necessary to get the uuid for each instance
			inputParamaterForExecuteSelect.getParameters().add(0, "instance");
			String[] params = new String[inputParamaterForExecuteSelect.getParameters().size()];
			inputParamaterForExecuteSelect.getParameters().toArray(params);
			reduceEachParamToItsName(params);

			for (int i = 0; i < params.length; i++) {
				if (params[i].contains("#")) {
					params[i] = params[i].substring(params[i].indexOf("#") + 1);
				}
			}

			List<String[]> resultList = reader.createResultListArray(ouObject, params);
			OutputForExecuteSelect outputForExecuteSelect = new OutputForExecuteSelect();
			outputForExecuteSelect.setInput(inputParamaterForExecuteSelect);

			inputParamaterForExecuteSelect.getParameters().remove(0); // Must be
																		// removed
																		// the
																		// uuid
																		// is
																		// not
																		// part
																		// of
																		// the
																		// result
			createLanguageSpecificHeader(inputParamaterForExecuteSelect, outputForExecuteSelect);

			// add uuid to result data structures
			for (String[] row : resultList) {
				outputForExecuteSelect.getUuids().add(row[0]);
			}

			addRowsToOutputForExecuteSelect(resultList, outputForExecuteSelect, 1); // NO
																					// uuid
																					// should
																					// be
																					// inserted

			return outputForExecuteSelect;
		} else {
			if (reader instanceof MarmottaReader) {
				NimbleSpecificSPARQLFactory sparqlFactory = new NimbleSpecificSPARQLFactory(this, sqpDerivationService);
				List<String> queries = sparqlFactory.createSparql(inputParamaterForExecuteSelect, reader, true, true);
				Map<String, List<DataPoint>> intermediateResult = new HashMap<String, List<DataPoint>>();
				String[] params = new String[] { "instance", "property", "hasValue" };
				//List<String> columns = new ArrayList<String>();

				for (String sparql : queries) {
					Object ouObject = reader.query(sparql);
					List<String[]> resultList = reader.createResultListArray(ouObject, params);
					for (String[] instance : resultList) {
						String uuid = instance[0];
						String property = instance[1];
//						if (!columns.contains(property)) {
//							columns.add(property);
//						}
						String value = instance[2];
						if (inputParamaterForExecuteSelect.getParametersURL().contains(property)){
						DataPoint dataPoint = new DataPoint();
						dataPoint.setPropertyURL(property);
						dataPoint.setValue(value);
						if (intermediateResult.containsKey(uuid)) {
							intermediateResult.get(uuid).add(dataPoint);
						} else {
							List<DataPoint> datapoints = new ArrayList<DataPoint>();
							datapoints.add(dataPoint);
							intermediateResult.put(uuid, datapoints);
						}
						}

					}

				}
				OutputForExecuteSelect outputForExecuteSelect = new OutputForExecuteSelect();
				outputForExecuteSelect.setInput(inputParamaterForExecuteSelect);
//				for (int i = 0; i < columns.size(); i++) {
//					String c = columns.get(i);
//					c = c.substring(c.indexOf("#") + 1);
//					columns.set(i, c);
//				}
				//outputForExecuteSelect.setColumns(columns);
				for (String key : intermediateResult.keySet()) {
					outputForExecuteSelect.getUuids().add(key);
					List<DataPoint> dataPoints = intermediateResult.get(key);
					ArrayList<String> data = new ArrayList<String>();
					Map<String, String> propertyValues = new HashMap<String, String>();
					for (DataPoint dataPoint : dataPoints) {
						String type = dataPoint.getPropertyURL();
						String value = dataPoint.getValue();
						value = value.substring(value.lastIndexOf("^") + 1);
						if (propertyValues.containsKey(type)) {
							String finalValue = propertyValues.get(type);
							finalValue = finalValue + ";" + value;
							propertyValues.put(type, finalValue);
						} else {
							propertyValues.put(type, value);
						}

					}
					
					for (String keyMap : propertyValues.keySet()) {
						data.add(propertyValues.get(keyMap));
					}

					if (propertyValues.keySet().size()== inputParamaterForExecuteSelect.getParametersURL().size()){
						if (outputForExecuteSelect.getColumns().size()==0){
						List<String> columns = new ArrayList<String>();
						for (String keyMap : propertyValues.keySet()) {
							String c = keyMap;
							c = c.substring(c.indexOf("#") + 1);
							columns.add(c);
						}
						outputForExecuteSelect.getColumns().addAll(columns);
						}
						
						
					}
					outputForExecuteSelect.getRows().add(data);


				}
				// cleanUP
				int index = 0;
				List<Integer> rowsToBeDeleted = new ArrayList<Integer>();
				for (List<String> oneRow : outputForExecuteSelect.getRows()){
					if (oneRow.size() < inputParamaterForExecuteSelect.getParametersURL().size()){
						rowsToBeDeleted.add(index);
					}
					index++;
				}
				int deletedCount =0;
				for (int indexToBeDelted : rowsToBeDeleted){
					int updatedIndexToBeDeleted = indexToBeDelted-deletedCount;
					outputForExecuteSelect.getRows().remove(updatedIndexToBeDeleted);
					outputForExecuteSelect.getUuids().remove(updatedIndexToBeDeleted);
					deletedCount++;
				}
				
				
				return outputForExecuteSelect;
			}
			return new OutputForExecuteSelect();
		}
	}

	private void extendRequestedPropertiesWithRespectToOrangeCommands(
			InputParamaterForExecuteSelect inputParamaterForExecuteSelect) {
		int index = 0;
		for (String parameter : inputParamaterForExecuteSelect.getParameters()) {
			if (sqpDerivationService.isITSAQPCommand(parameter)) {
				SQPConfiguration sqpConfiguration = sqpDerivationService.getSpecificSQPConfiguration(parameter);
				String[] pathTokens = sqpConfiguration.getSQPMapping().getTarget().getTargetPathFromSource().split(";");
				String targetProperty = sqpConfiguration.getSQPMapping().getTarget().getTargetProperty();
				Parameter parameter2 = inputParamaterForExecuteSelect.getParametersIncludingPath().get(index);
				inputParamaterForExecuteSelect.getParameters().set(index, targetProperty);
				parameter2.setUrlOfProperty(targetProperty);
				parameter2.getPath().clear();

				Tuple t1 = new Tuple();
				t1.setConcept(inputParamaterForExecuteSelect.getConcept());
				t1.setUrlOfProperty(null);
				parameter2.getPath().add(t1);

				for (String token : pathTokens) {
					Tuple tuple = new Tuple();
					parameter2.getPath().add(tuple);
					tuple.setUrlOfProperty(token);
				}

			} else {
				Logger.getAnonymousLogger().log(Level.WARNING, "Cannot find orange command: " + parameter);
			}
			index++;
		}

		// Adapt to speciifc orange commands
		for (String name : inputParamaterForExecuteSelect.getOrangeCommandSelected().getNames()) {
			if (sqpDerivationService.isITSAQPCommand(name)) {
				SQPConfiguration sqpConfiguration = sqpDerivationService.getSpecificSQPConfiguration(name);
				String[] pathTokens = sqpConfiguration.getSQPMapping().getTarget().getTargetPathFromSource().split(";");
				String targetProperty = sqpConfiguration.getSQPMapping().getTarget().getTargetProperty();
				Parameter parameter2 = new Parameter();
				inputParamaterForExecuteSelect.getParametersIncludingPath().add(parameter2);
				inputParamaterForExecuteSelect.getParameters().add(targetProperty);
				parameter2.setUrlOfProperty(targetProperty);
				parameter2.getPath().clear();

				Tuple t1 = new Tuple();
				t1.setConcept(inputParamaterForExecuteSelect.getConcept());
				t1.setUrlOfProperty(null);
				parameter2.getPath().add(t1);

				for (String token : pathTokens) {
					Tuple tuple = new Tuple();
					parameter2.getPath().add(tuple);
					tuple.setUrlOfProperty(token);
				}
			} else {
				Logger.getAnonymousLogger().log(Level.WARNING, "Cannot find orange command: " + name);
			}
		}

	}

	public void reduceEachParamToItsName(String[] params) {
		for (int i = 0; i < params.length; i++) {
			String param = params[i];
			param = extractNameOfURL(param);
			params[i] = param;
		}
	}

	public void createLanguageSpecificHeader(InputParamaterForExecuteSelect inputParamaterForExecuteSelect,
			OutputForExecuteSelect outputForExecuteSelect) {
		for (String prop : inputParamaterForExecuteSelect.getParameters()) {
			if (!prop.contains("#")) {
				prop = getURIOfProperty(prop);
			}
			if (inputParamaterForExecuteSelect.getLanguage() != null) {
				String label = translateConcept(prop, inputParamaterForExecuteSelect.getLanguage(), this.languagelabel)
						.getTranslation();
				outputForExecuteSelect.getColumns().add(label);
			} else {
				Logger.getAnonymousLogger().log(Level.WARNING,
						"No language set for input: " + inputParamaterForExecuteSelect);
				String label = extractNameOfURL(prop);
				outputForExecuteSelect.getColumns().add(label);
			}

		}
		// outputForExecuteSelect.getColumns().addAll(inputParamaterForExecuteSelect.getParameters());
	}

	public OutputForExecuteSelect createOPtionalSPARQLAndExecuteIT(
			InputParamaterForExecuteOptionalSelect inputParamaterForExecuteOptionalSelect) {

		// This is/ necessary to get the uuid for each instance
		Map<String, String> result = reader
				.getPropertyValuesOfAIndividium(inputParamaterForExecuteOptionalSelect.getUuid());

		OutputForExecuteSelect outputForExecuteSelect = new OutputForExecuteSelect();

		// If the NImble specific Marmotta structure is applied, a additional
		// postprocessing is required
		if (needANimbleSpecificAdapation()) {
			result = nimbleSpecificSPARQLDeriviation.resolveOPtionalSPARQLAndExecuteITToFinalPropertyValues(result,
					inputParamaterForExecuteOptionalSelect.getUuid());
		}

		ArrayList<String> row = new ArrayList<String>();
		for (String key : result.keySet()) {
			Language language = inputParamaterForExecuteOptionalSelect.getLanguage();
			String column = translateProperty(key, language, this.languagelabel).getTranslation();
			outputForExecuteSelect.getColumns().add(column);

			String value = result.get(key);
			if (value != null && value.length() > 0) {
				int index = -1;
				index = value.indexOf("^^");
				if (index > -1) {
					value = value.substring(0, index);
				}

				row.add(value);
			} else {
				row.add(" ");
			}

		}

		// Have to extend the result with the orange stuff
		if (inputParamaterForExecuteOptionalSelect.getOrangeCommandSelected().getNames().size() > 0) {
			String sparql = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> select distinct ";
			String[] columns = new String[inputParamaterForExecuteOptionalSelect.getOrangeCommandSelected().getNames()
					.size()];
			int index = 0;
			for (String name : inputParamaterForExecuteOptionalSelect.getOrangeCommandSelected().getNames()) {
				sparql += " ?" + name + " ";
				columns[index] = name;
				outputForExecuteSelect.getColumns().add(name);
				index++;
			}
			sparql += " where {";
			for (String name : inputParamaterForExecuteOptionalSelect.getOrangeCommandSelected().getNames()) {
				String lastSubject = "<" + inputParamaterForExecuteOptionalSelect.getUuid() + ">";

				SQPConfiguration configuration = sqpDerivationService.getSpecificSQPConfiguration(name);
				String[] tokens = sqpDerivationService.splitTargetConfiguration(configuration);
				for (String token : tokens) {
					String shortToken = token.substring(token.indexOf("#") + 1);
					sparql += lastSubject + "<" + token + "> ?" + shortToken + " .";
					lastSubject = "?" + shortToken;
				}
				sparql += lastSubject + "<" + configuration.getSQPMapping().getTarget().getTargetProperty() + ">" + "?"
						+ name + ".";
			}
			sparql += "}";
			Object resultSet = reader.query(sparql);
			List<String[]> data = reader.createResultListArray(resultSet, columns);
			if (data != null && data.size() > 0) {
				String[] firstrow = data.get(0);
				for (String str : firstrow) {
					row.add(str);
				}
			}
		}
		outputForExecuteSelect.getRows().add(row);
		return outputForExecuteSelect;
	}

	public void addRowsToOutputForExecuteSelect(List<String[]> resultList,
			OutputForExecuteSelect outputForExecuteSelect, int startIndex) {
		for (int i = 0; i < resultList.size(); i++) {
			ArrayList<String> row = new ArrayList<String>();
			for (int a = startIndex; a < resultList.get(i).length; a++) {
				String value = resultList.get(i)[a];
				if (value != null && value.length() > 0) {
					int index = -1;
					index = value.indexOf("^^");
					if (index > -1) {
						value = value.substring(0, index);
					}
				}
				row.add(value);
			}
			outputForExecuteSelect.getRows().add(row);
		}
	}

	/**
	 * The query creates a sparql select for the given concept and properties.
	 * The method should work in two cases, if the name of the property or the
	 * overall url is given.
	 * 
	 * @param inputParamaterForExecuteSelect
	 *            includes the URL of cocnept and set of properties (name from
	 *            the url or url)
	 * @return working sparql query
	 */
	// protected String createSparql(InputParamaterForExecuteSelect
	// inputParamaterForExecuteSelect) {
	// // TODO Auto-generated method stub
	// String concept =
	// getURIOfConcept(inputParamaterForExecuteSelect.getConcept());
	//
	// Map<String, String> resolvedProperties = new HashMap<String, String>();
	// for (String param : inputParamaterForExecuteSelect.getParameters()) {
	// String parameter = getURIOfProperty(param);
	//
	// param = extractNameOfURL(param);
	//
	// resolvedProperties.put(param, parameter);
	// }
	//
	// String sparql = "PREFIX rdf:
	// <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX owl:
	// <http://www.w3.org/2002/07/owl#> PREFIX rdfs:
	// <http://www.w3.org/2000/01/rdf-schema#> PREFIX xsd:
	// <http://www.w3.org/2001/XMLSchema#> select distinct ?instance ";
	// for (String param : inputParamaterForExecuteSelect.getParameters()) {
	//
	// param = extractNameOfURL(param);
	// sparql += " ?" + param;
	// }
	//
	// sparql += " where{";
	//
	// // add cocnept mapping:
	// sparql += "?x rdfs:subClassOf* <" + concept + ">. ";
	// sparql += "?instance a ?x.";
	// sparql = addProperties(inputParamaterForExecuteSelect,
	// resolvedProperties, sparql);
	// sparql = addFilters(inputParamaterForExecuteSelect, resolvedProperties,
	// sparql);
	// sparql += "}";
	// return sparql;
	// }

	/**
	 * just for test purpose
	 * 
	 */
	protected String createSparql(InputParamaterForExecuteSelect inputParamaterForExecuteSelect, Reader reader) {
		this.reader = reader;
		return createSparql(inputParamaterForExecuteSelect);
	}

	protected String createSparql(InputParamaterForExecuteSelect inputParamaterForExecuteSelect) {
		// TODO Auto-generated method stub
		String concept = getURIOfConcept(inputParamaterForExecuteSelect.getConcept());

		Map<String, String> resolvedProperties = new HashMap<String, String>();
		int counter = 0;
		for (String param : inputParamaterForExecuteSelect.getParameters()) {
			String parameter = "";
			if (inputParamaterForExecuteSelect.getParametersURL().size() > counter) {
				parameter = inputParamaterForExecuteSelect.getParametersURL().get(counter);
			} else {
				parameter = getURIOfProperty(param);
			}

			param = extractNameOfURL(param);

			resolvedProperties.put(param, parameter);
			counter++;
		}

		String sparql = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> select distinct ?instance ";
		for (String param : inputParamaterForExecuteSelect.getParameters()) {

			param = extractNameOfURL(param);
			sparql += " ?" + param;
		}

		sparql += " where{";

		if (!(reader instanceof MarmottaReader)) {
			// add cocnept mapping:
			sparql += "?x rdfs:subClassOf*  <" + concept + ">. ";
			sparql += "?instance a ?x.";
			sparql = addProperties(inputParamaterForExecuteSelect, resolvedProperties, sparql);
			sparql = addFilters(inputParamaterForExecuteSelect, resolvedProperties, sparql);
			sparql += "}";
		}

		if (reader instanceof MarmottaReader) {
			// sparql += "?instance (rdf:type | rdfs:subClassOf*/rdfs:subClassOf
			// )" + "<" + concept + ">.";
			// sparql += "?instance a" + "<" + concept + ">.";

			List<String> allRelfexivAndSubClasses = reader.getAllTransitiveSubConcepts(concept);
			if (!allRelfexivAndSubClasses.contains(concept)) {
				allRelfexivAndSubClasses.add(concept);
			}
			// apply the query provided by Ditmar
			String preQuiery = " VALUES ?classes {";
			String classes = "";
			for (String myClass : allRelfexivAndSubClasses) {
				classes += "<" + myClass + "> ";
			}
			sparql += preQuiery + classes + "}";

			sparql = addProperties(inputParamaterForExecuteSelect, resolvedProperties, sparql);
			sparql = addFilters(inputParamaterForExecuteSelect, resolvedProperties, sparql);
			sparql += "}";
		}

		return sparql;
	}

	public String extractNameOfURL(String param) {
		if (param.contains("#")) {
			param = param.substring(param.indexOf("#") + 1);
		}
		return param;
	}

	/**
	 * Generate a filter which is useable for decimal values
	 * 
	 * @param inputParamaterForExecuteSelect
	 * @param resolvedProperties
	 * @param sparql
	 * @return
	 */
	private String addFilters(InputParamaterForExecuteSelect inputParamaterForExecuteSelect,
			Map<String, String> resolvedProperties, String sparql) {
		String filter = "";
		for (Filter fil : inputParamaterForExecuteSelect.getFilters()) {
			String filterText = "";
			String shortName = fil.getProperty();
			if (shortName == null) {
				Logger.getAnonymousLogger().log(Level.WARNING,
						"Filter cannot be applied without property name: " + fil);
				continue;
			}

			shortName = extractNameOfURL(shortName);
			if (fil.isHasMaxBeenSet() && fil.isHasMinBeenSet()) {
				filterText += "FILTER ( xsd:decimal(?" + shortName + ") <=  xsd:decimal(" + fil.getMax() + ")).";
				filterText += "FILTER ( xsd:decimal(?" + shortName + ") >=  xsd:decimal(" + fil.getMin() + ")).";
			} else {
				// FILTER (?name="South"^^xsd:string)
				if (fil.getExactValue().matches("[0-9]+\\.*[0-9]*")) {
					filterText += "FILTER(?" + shortName + "= " + fil.getExactValue() + ")";
				} else {

					filterText += "FILTER(str(?" + shortName + ")= \"" + fil.getExactValue() + "\" ^^xsd:string)";
				}
			}
			filter += filterText;
		}

		return sparql + filter;
	}

	public String addProperties(InputParamaterForExecuteSelect inputParamaterForExecuteSelect,
			Map<String, String> resolvedProperties, String sparql) {
		for (int i = 0; i < inputParamaterForExecuteSelect.getParameters().size(); i++) {

			String param = inputParamaterForExecuteSelect.getParameters().get(i);

			param = extractNameOfURL(param);

			String property = resolvedProperties.get(param);

			if (isJustADirectDatatypePropertyOfRoot(
					inputParamaterForExecuteSelect.getParametersIncludingPath().get(i))) {

				sparql += "?instance " + "<" + property + "> " + "?" + param + ".";
			} else {
				String lastVariable = null;
				for (Tuple tuple : inputParamaterForExecuteSelect.getParametersIncludingPath().get(i).getPath()) {

					if (tuple.getUrlOfProperty() != null) {
						String prop = tuple.getUrlOfProperty();
						String shortProp = prop.substring(prop.lastIndexOf("#") + 1);
						shortProp = "?" + shortProp;
						String toBeUsed = "?instance";
						if (lastVariable != null) {
							toBeUsed = lastVariable;
						}
						sparql += toBeUsed + "<" + tuple.getUrlOfProperty() + "> " + shortProp + ".";
						lastVariable = shortProp;
					}
				}
				// Have to add it for the dataproperty
				sparql += lastVariable + "<" + property + "> " + "?" + param + ".";
			}
		}
		return sparql;
	}

	public boolean isJustADirectDatatypePropertyOfRoot(Parameter parameter) {
		return (parameter.getPath().size() == 1) ? true : false;

	}

	// InputParameterdetectMeaningLanguageSpecific
	// inputParameterdetectMeaningLanguageSpecific
	public List<String> detectPossibleConcepts(
			InputParameterdetectMeaningLanguageSpecific inputParameterdetectMeaningLanguageSpecific) {
		Logger.getAnonymousLogger().log(Level.INFO, "Apply reader: " + reader.getClass().toString());
		return reader.getAllConcepts(inputParameterdetectMeaningLanguageSpecific.getKeyword());
	}

	/**
	 * Translation Concept: Returns the translation if available as String.
	 * Otherwise it delivers NA
	 * 
	 * @param uri
	 * @param language
	 * @param languagelabel
	 *            The label in which the translations are codified. if no label
	 *            property available, please set to null
	 * @return Returns the translation if available as String. Otherwise it
	 *         delivers uri
	 */
	public TranslationResult translateConcept(String uri, Language language, String languageLabel) {
		return translateEntity(uri, language, languageLabel);

	}

	/**
	 * Translation Property: Returns the translation if available as String.
	 * Otherwise it delivers uri
	 * 
	 * @param uri
	 * @param language
	 * @param languagelabel
	 *            The label in which the translations are codified. if no label
	 *            property available, please set to null
	 * @return Returns the translation if available as String. Otherwise it
	 *         delivers NA
	 */
	public TranslationResult translateProperty(String uri, Language language, String languageLabel) {
		return translateEntity(uri, language, languageLabel);
	}

	private TranslationResult translateEntity(String uri, Language language, String languageLabel) {
		TranslationResult translationResult = new TranslationResult();
		translationResult.setSuccess(false);
		translationResult.setOriginal(uri);
		// String namespace = uri.substring(0, uri.indexOf("#"));
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>SELECT   ?subject ?object WHERE { <"
				+ uri + "> <" + languageLabel + "> ?object.}";
		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init fails");
		} else {

			if (uri.contains("http") || (uri.contains("https"))) {

				Object result = reader.query(query);
				List<String> translations = reader.createResultList(result, "object");
				if (translations.size() > 0) {
					String postfix = Language.toOntologyPostfix(language);
					if (postfix != null) {
						for (String lang : translations) {
							if (lang.contains(postfix)) {

								translationResult.setTranslation(lang.substring(0, lang.indexOf(postfix)));
								translationResult.setSuccess(true);
								return translationResult;
							}
						}
					}
				}
			}
			translationResult.setTranslation(uri.substring(uri.indexOf("#") + 1));
		}
		return translationResult;

	}

	public List<String> detectPossibleConcepts(String regex) {
		Logger.getAnonymousLogger().log(Level.INFO, "Apply reader: " + reader.getClass().toString());
		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init fails");
		} else {
			return reader.getAllConcepts(regex);
		}
		return Collections.emptyList();
	}
	
	

	public List<Entity> detectPossibleConceptsLanguageSpecific(String regex, Language language, String translationLabel,
			boolean useSimplifiedSPARQL) {
		Logger.getAnonymousLogger().log(Level.INFO, "Apply reader: " + reader.getClass().toString());
		Logger.getAnonymousLogger().log(Level.INFO, "Language specific serach for:  " + language.toString());
		reader.setLanguageLabel(translationLabel);
		List<de.biba.triple.store.access.enums.Language> langaues = reader.getNativeSupportedLangauges();

		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init failed");
		} else {

			List<Entity> concepts = new ArrayList<Entity>();

			if (needANimbleSpecificAdapation()) {

				concepts.clear();
				concepts.addAll(nimbleSpecificSPARQLDeriviation
						.detectNimbleSpecificMeaningFromAKeywordReferringToInstances(regex,

								translationLabel, language, useSimplifiedSPARQL));
				nimbleSpecificSPARQLDeriviation.removeInternalConceptsToHideItForTheUser(concepts);
			} else {
				if (langaues.contains(language)) {
					Logger.getAnonymousLogger().log(Level.INFO, "Apply language specific serach: " + language);
					concepts = reader.getAllConceptsLanguageSpecific(regex, language);
				} else {
					Logger.getAnonymousLogger().log(Level.INFO,
							"Apply language UNspecific serach: " + Language.UNKNOWN);
					concepts = reader.getAllConceptsFocusOnlyOnURI(regex);
				}
			}
			return concepts;
		}
		return Collections.emptyList();
	}

	public List<String> detectPossibleProperties(String regex) {

		return reader.getAllDirectProperties(regex);
		// return reader.getAllProperties(regex);
	}

	/*
	 * public LocalOntologyView getViewForOneStepRange(String concept,
	 * LocalOntologyView instance, Language language) { LocalOntologyView
	 * localOntologyView = null;
	 * 
	 * if (instance == null) { localOntologyView = new LocalOntologyView(); }
	 * else { localOntologyView = instance; }
	 * 
	 * String conceptAsUri = getURIOfConcept(concept);
	 * Logger.getAnonymousLogger().log(Level.INFO, "Request properties from: " +
	 * conceptAsUri); List<String> properties =
	 * reader.getAllPropertiesIncludingEverything(conceptAsUri); for (String
	 * proeprty : properties) { PropertyType pType =
	 * reader.getPropertyType(proeprty); if (pType ==
	 * PropertyType.DATATYPEPROPERTY) { String translatedName =
	 * reduceURIJustToName(proeprty, language); Entity entity = new Entity();
	 * entity.setUrl(proeprty); entity.setTranslatedURL(translatedName);
	 * 
	 * localOntologyView.addDataproperties(entity); } else { // It is a object
	 * property which means I must return the name of // the concept
	 * List<String> ranges = reader.getRangeOfProperty(proeprty); for (int i =
	 * 0; i < ranges.size(); i++) { String range = ranges.get(i); String
	 * rangeReduced = reduceURIJustToName(range, language); LocalOntologyView
	 * localOntologyView2 = new LocalOntologyView();
	 * 
	 * Entity conceptRange = new Entity(); conceptRange.setUrl(range); String
	 * label = translateConcept(range, language,
	 * this.languagelabel).getTranslation();
	 * conceptRange.setTranslatedURL(rangeReduced);
	 * 
	 * localOntologyView2.setConcept(conceptRange);
	 * localOntologyView.getObjectproperties().put(range, localOntologyView2); }
	 * } }
	 * 
	 * return localOntologyView;
	 * 
	 * }
	 */

	public LocalOntologyView getViewForOneStepRange(String concept, LocalOntologyView instance,
			LocalOntologyView parentInstance, Language language) {
		LocalOntologyView localOntologyView = null;

		if (instance == null) {
			localOntologyView = new LocalOntologyView();
		} else {
			localOntologyView = instance;
		}

		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init failed");
		} else {

			String conceptAsUri = getURIOfConcept(concept);
			Logger.getAnonymousLogger().log(Level.INFO, "Request properties from: " + conceptAsUri);
			List<String> properties = new ArrayList<>();

			if (needANimbleSpecificAdapation()) {
				OutputForPropertiesFromConcept outputForPropertiesFromConcept = nimbleSpecificSPARQLDeriviation
						.getAllPropertiesIncludingEverything(concept);
				List<OutputForPropertyFromConcept> props = outputForPropertiesFromConcept
						.getOutputForPropertiesFromConcept();
				for (OutputForPropertyFromConcept property : props) {
					PropertyType pType = reader.getPropertyType(property.getPropertyURL());
					if (pType == PropertyType.DATATYPEPROPERTY || pType == PropertyType.UNKNOWN) {
						String translatedName = reduceURIJustToName(property.getPropertyURL(), language);
						eu.nimble.service.catalog.search.impl.dao.Entity entity = new eu.nimble.service.catalog.search.impl.dao.Entity();
						entity.setUrl(property.getPropertyURL());
						entity.setTranslatedURL(translatedName);
						entity.setPropertySource(property.getPropertySource());
						localOntologyView.addDataproperties(entity);
					} else {
						addObjectPropertyToLogicalView(instance, language, localOntologyView,
								property.getPropertyURL());
					}
				}

			} else {
				properties = reader.getAllPropertiesIncludingEverything(conceptAsUri);
				for (String proeprty : properties) {
					PropertyType pType = reader.getPropertyType(proeprty);
					if (pType == PropertyType.DATATYPEPROPERTY) {
						String translatedName = reduceURIJustToName(proeprty, language);
						eu.nimble.service.catalog.search.impl.dao.Entity entity = new eu.nimble.service.catalog.search.impl.dao.Entity();
						entity.setUrl(proeprty);
						entity.setTranslatedURL(translatedName);
						entity.setPropertySource(PropertySource.DOMAIN_SPECIFIC_PROPERTY);

						localOntologyView.addDataproperties(entity);
					} else {
						// It is a object property which means I must return the
						// name of
						// the concept
						addObjectPropertyToLogicalView(instance, language, localOntologyView, proeprty);
					}

				}
			}

			// Hidden the parent concepts as well as the other elements of the
			// parent concepts when the distance to frozen concept is larger
			// than 1
			if (!properties.isEmpty() && localOntologyView.getDistanceToFrozenConcept() > 1 && parentInstance != null) {
				localOntologyView.hiddenElementsOfParentView(parentInstance);
			}

		}
		return localOntologyView;

	}

	public void addObjectPropertyToLogicalView(LocalOntologyView instance, Language language,
			LocalOntologyView localOntologyView, String proeprty) {
		List<String> ranges = reader.getRangeOfProperty(proeprty);
		for (int i = 0; i < ranges.size(); i++) {
			String range = ranges.get(i);
			String rangeReduced = reduceURIJustToName(range, language);
			LocalOntologyView localOntologyView2 = new LocalOntologyView();

			eu.nimble.service.catalog.search.impl.dao.Entity conceptRange = new eu.nimble.service.catalog.search.impl.dao.Entity();
			conceptRange.setUrl(range);
			String label = translateConcept(range, language, this.languagelabel).getTranslation();
			conceptRange.setTranslatedURL(rangeReduced);
			// conceptRan

			localOntologyView2.setConcept(conceptRange);
			localOntologyView2.setObjectPropertySource(proeprty);
			localOntologyView2.setFrozenConcept(instance.getFrozenConcept());
			localOntologyView2.setDistanceToFrozenConcept(instance.getDistanceToFrozenConcept() + 1);
			List<String> newPaht = new ArrayList<String>(localOntologyView.getConceptURIPath());
			newPaht.add(range);
			localOntologyView2.setConceptURIPath(newPaht);
			localOntologyView.getObjectproperties().put(range, localOntologyView2);
		}
	}

	private String reduceURIJustToName(String uri, Language language) {
		TranslationResult range = translateConcept(uri, language, languagelabel);
		// range = range.substring(range.indexOf("#") + 1);
		return range.getTranslation();
	}

	public String getURIOfConcept(String concept) {

		if (concept.contains("#") || (concept.contains("http://www.nimble-project.org/resource/eclass"))) {
			return concept;
		}

		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init fails");
		} else {
			List<String> allPossibleConcepts = reader.getAllConcepts(concept);
			for (String conceptURI : allPossibleConcepts) {
				String conceptURIShortened = conceptURI.substring(conceptURI.indexOf("#") + 1);
				if (conceptURIShortened.equals(concept)) {
					return conceptURI;
				}
			}
			Logger.getAnonymousLogger().log(Level.WARNING, "Couldn't find right concept in ontology: " + concept);
			return concept;
		}
		return null;
	}

	public String getURIOfProperty(String property) {
		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init fails");
		} else {
			List<String> allPossibleProperties = reader.getAllProperties(property);
			for (String propertyURI : allPossibleProperties) {
				String propertyURIShortened = propertyURI.substring(propertyURI.indexOf("#") + 1);
				if (propertyURIShortened.equals(property)) {
					return propertyURI;
				}
			}
			Logger.getAnonymousLogger().log(Level.WARNING, "Couldn't find right concept in ontology: " + property);
			return property;
		}
		return null;
	}

	/**
	 * Die Methode nimmt den REader/Search um das zu machen
	 * 
	 * @param amountOfGroups
	 * @param concept
	 * @param property
	 * @return
	 */
	public Map<String, List<Group>> generateGroup(int amountOfGroups, String concept, String property,
			PropertySource propertySource) {
		concept = getURIOfConcept(concept);
		String shortPropertyName = property;
		property = getURIOfProperty(property);
		List<String> values = getAllValuesForAGivenProperty(concept, property, propertySource);
		for (int i = 0; i < values.size(); i++) {
			String str = values.get(i);
			int index = str.lastIndexOf("^");
			if (index > -1) {
				str = str.substring(0, index - 1);
			}
			str = str.replace(",", ".");
			values.set(i, str);
		}
		if (values != null && values.size() > 0) {
			try {
				ValueGroupingFactory valueGroupingFactory = new ValueGroupingFactory();
				return valueGroupingFactory.generateGrouping(amountOfGroups, values, shortPropertyName);
				// Map<String, List<Group>> result = new HashMap<String,
				// List<Group>>();
				// float min = getMinOfData(values);
				// float max = getMaxOfData(values);
				// float stepRate = (max - min) / (float) amountOfGroups;
				// List<Group> discreditedGroups = new ArrayList<Group>();
				// for (int i = 0; i < amountOfGroups; i++) {
				// Group group = new Group();
				// float newMin = min + (stepRate * i);
				// float newMax = min + (stepRate * (i + 1));
				// if (newMin > 2) {
				// newMin = round(newMin);
				// newMax = round(newMax);
				// }
				// group.setDescription("From: " + newMin + " to " + newMax);
				// group.setMin(newMin);
				// group.setMax(newMax);
				// group.setProperty(shortPropertyName);
				// discreditedGroups.add(group);
				// }
				// result.put(shortPropertyName, discreditedGroups);
				// return result;
			} catch (Exception e) {
				Logger.getAnonymousLogger().log(Level.WARNING,
						"Cannot transform data from " + property + " into floats");
			}
		} else {
			return new HashMap<String, List<Group>>();
		}
		return new HashMap<String, List<Group>>();
	}

	// private float round(float value) {
	// int n = (int) value * 100;
	// return n / 100f;
	// }

	public List<String> getAllValuesForAGivenProperty(String concept, String property, PropertySource propertySource) {
		List<String> values = null;
		if (!needANimbleSpecificAdapation()) {
			values = propertyValuesCrawler.getAllDifferentValuesForAProperty(concept, property);
		} else {
			values = nimbleSpecificSPARQLDeriviation.getAllDifferentValuesForAProperty(concept, property,
					propertySource);
		}
		return values;
	}

	/**
	 * The NIMBLE platform uses a specific ontology which requires manual
	 * inclusion or exclusion of properties and other stuff
	 * 
	 * @return true if Marmotta is set as main data source of the search
	 */
	private boolean needANimbleSpecificAdapation() {
		return (reader instanceof MarmottaReader) ? true : false;
	}

	// private float getMinOfData(List<String> values) {
	// float min = 999999;
	// for (String value : values) {
	// float number = Float.valueOf(value);
	// if (number < min) {
	// min = number;
	// }
	// }
	//
	// return min;
	// }

	// private float getMaxOfData(List<String> values) {
	// float max = -999999;
	// for (String value : values) {
	// float number = Float.valueOf(value);
	// if (number > max) {
	// max = number;
	// }
	// }
	//
	// return max;
	// }

	public String getLanguagelabel() {
		return languagelabel;
	}

	public void setLanguagelabel(String languagelabel) {
		this.languagelabel = languagelabel;
		if (reader != null) {
			reader.setLanguageLabel(languagelabel);
		}
	}

	public List<String> getSupportedLanguages() {
		if (reader == null) {
			Logger.getAnonymousLogger().log(Level.WARNING, "Ontology Reader is null. The init fails");
		} else {
			List<Language> languages = reader.getNativeSupportedLangauges();
			List<String> result = new ArrayList<String>();
			for (Language l : languages) {
				String label = Language.toOntologyPostfix(l).substring(1);
				result.add(label);
			}
			return result;
		}
		return Collections.emptyList();
	}

	/**
	 * This method shall derive all parent classes for a concept.- That means
	 * that a highchair would be also a product a furniture etc.
	 * 
	 * @param concept
	 * @return a list of parent concepts
	 */
	public List<String> getAllDerivedConcepts(String concept) {

		List<String> result = new ArrayList<String>();
		result.add(concept);

		return result;
	}

	public List<String[]> getAllObjectPropertiesIncludingEverythingAndReturnItsRange(
			InputParameterForGetReferencesFromAConcept input) {
		return reader.getAllObjectPropertiesIncludingEverythingAndReturnItsRange(input.getConceptURL());
	}

	public OutputForPropertiesFromConcept getAllTransitiveProperties(String concept) {
		concept = getURIOfConcept(concept);
		if (needANimbleSpecificAdapation()) {
			return nimbleSpecificSPARQLDeriviation.getAllPropertiesIncludingEverything(concept);
		} else {
			OutputForPropertiesFromConcept result = new OutputForPropertiesFromConcept();

			List<String> properties = reader.getAllPropertiesIncludingEverything(concept);
			for (String urlOfProperty : properties) {
				PropertyType propertyType = reader.getPropertyType(urlOfProperty);
				OutputForPropertyFromConcept outputForPropertyFromConcept = new OutputForPropertyFromConcept();
				outputForPropertyFromConcept.setPropertyURL(urlOfProperty);
				outputForPropertyFromConcept.setPropertySource(PropertySource.DOMAIN_SPECIFIC_PROPERTY);
				if (propertyType == PropertyType.DATATYPEPROPERTY) {
					outputForPropertyFromConcept.setDatatypeProperty(true);
					outputForPropertyFromConcept.setObjectProperty(false);
				} else {
					outputForPropertyFromConcept.setDatatypeProperty(false);
					outputForPropertyFromConcept.setObjectProperty(true);
				}
				result.getOutputForPropertiesFromConcept().add(outputForPropertyFromConcept);
			}

			return result;
		}
	}

	public OutputForPropertyValuesFromOrangeGroup getPropertyValuesFromOrangeGroup(
			InputParameterForPropertyValuesFromOrangeGroup valuesFromOrangeGroup) {
		String command = valuesFromOrangeGroup.getOrangeCommand();
		String concept = valuesFromOrangeGroup.getConceptURL();
		return nimbleSpecificSPARQLDeriviation.getPropertyValuesForOrangeGroup(command, concept,
				needANimbleSpecificAdapation());
	}
}
