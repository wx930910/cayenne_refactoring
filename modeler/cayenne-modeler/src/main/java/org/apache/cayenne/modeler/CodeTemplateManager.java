/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.modeler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.gen.ClientClassGenerationAction;
import org.apache.cayenne.modeler.pref.FSPath;
import org.apache.cayenne.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages code generation templates.
 */
public class CodeTemplateManager {

	public static final String STANDARD_SERVER_SUPERCLASS = "Standard Server Superclass";
	public static final String STANDARD_SERVER_SUBCLASS = "Standard Server Subclass";
	public static final String SINGLE_SERVER_CLASS = "Single Server class";
	static final String STANDARD_CLIENT_SUPERCLASS = "Standard Client Superclass";
	static final String STANDARD_CLIENT_SUBCLASS = "Standard Client Subclass";

	private static final String STANDART_EMBEDDABLE_SUPERCLASS = "Standart Embeddable Superclass";
	private static final String STANDART_EMBEDDABLE_SUBCLASS = "Standart Embeddable Subclass";
	private static final String SINGLE_EMBEDDABLE_CLASS = "Single Embeddable class";

	private static final String STANDART_DATAMAP_SUPERCLASS = "Standart DataMap Superclass";
	private static final String STANDART_DATAMAP_SUBCLASS = "Standart DataMap Subclass";
	private static final String SINGLE_DATAMAP_CLASS = "Single DataMap class";

	public static final String NODE_NAME = "codeTemplateManager";

	private List<String> standardSubclassTemplates;
	private List<String> standardSuperclassTemplates;
	private List<String> standardClientSubclassTemplates;
	private List<String> standardClientSuperclassTemplates;
	private Map<String, String> customTemplates;
	private Map<String, String> reverseCustomTemplate;
	private Map<String, String> standardTemplates;

	private List<String> standartEmbeddableTemplates;
	private List<String> standartEmbeddableSuperclassTemplates;

	private List<String> standartDataMapTemplates;
	private List<String> standartDataMapSuperclassTemplates;

	private Map<String, String> reverseStandartTemplates;

	private static Logger logger = LoggerFactory.getLogger(CodeTemplateManager.class);

	public Preferences getTemplatePreferences(Application application) {
		return application.getPreferencesNode(this.getClass(), NODE_NAME);
	}

	public CodeTemplateManager(Application application) {
		standardSuperclassTemplates = new ArrayList<>(2);
		standardSuperclassTemplates.add(STANDARD_SERVER_SUPERCLASS);

		standardClientSuperclassTemplates = new ArrayList<>();
		standardClientSuperclassTemplates.add(STANDARD_CLIENT_SUPERCLASS);

		standardSubclassTemplates = new ArrayList<>(2);
		standardSubclassTemplates.add(SINGLE_SERVER_CLASS);
		standardSubclassTemplates.add(STANDARD_SERVER_SUBCLASS);

		standardClientSubclassTemplates = new ArrayList<>();
		standardClientSubclassTemplates.add(STANDARD_CLIENT_SUBCLASS);

		standartEmbeddableTemplates = new ArrayList<>();
		standartEmbeddableTemplates.add(SINGLE_EMBEDDABLE_CLASS);
		standartEmbeddableTemplates.add(STANDART_EMBEDDABLE_SUBCLASS);

		standartEmbeddableSuperclassTemplates = new ArrayList<>();
		standartEmbeddableSuperclassTemplates.add(STANDART_EMBEDDABLE_SUPERCLASS);

		standartDataMapTemplates = new ArrayList<>();
		standartDataMapTemplates.add(STANDART_DATAMAP_SUBCLASS);
		standartDataMapTemplates.add(SINGLE_DATAMAP_CLASS);

		standartDataMapSuperclassTemplates = new ArrayList<>();
		standartDataMapSuperclassTemplates.add(STANDART_DATAMAP_SUPERCLASS);

		updateCustomTemplates(getTemplatePreferences(application));
		reverseCustomTemplate = new HashMap<>();
		for(Map.Entry<String, String> entry : customTemplates.entrySet()){
			reverseCustomTemplate.put(entry.getValue(), entry.getKey());
		}

		standardTemplates = new HashMap<>();
		standardTemplates.put(STANDARD_SERVER_SUPERCLASS, ClassGenerationAction.SUPERCLASS_TEMPLATE);
		standardTemplates.put(STANDARD_CLIENT_SUPERCLASS, ClientClassGenerationAction.SUPERCLASS_TEMPLATE);
		standardTemplates.put(STANDARD_SERVER_SUBCLASS, ClassGenerationAction.SUBCLASS_TEMPLATE);
		standardTemplates.put(STANDARD_CLIENT_SUBCLASS, ClientClassGenerationAction.SUBCLASS_TEMPLATE);
		standardTemplates.put(SINGLE_SERVER_CLASS, ClassGenerationAction.SINGLE_CLASS_TEMPLATE);

		standardTemplates.put(STANDART_EMBEDDABLE_SUPERCLASS, ClassGenerationAction.EMBEDDABLE_SUPERCLASS_TEMPLATE);
		standardTemplates.put(STANDART_EMBEDDABLE_SUBCLASS, ClassGenerationAction.EMBEDDABLE_SUBCLASS_TEMPLATE);
		standardTemplates.put(SINGLE_EMBEDDABLE_CLASS, ClassGenerationAction.EMBEDDABLE_SINGLE_CLASS_TEMPLATE);

		standardTemplates.put(STANDART_DATAMAP_SUBCLASS, ClassGenerationAction.DATAMAP_SUBCLASS_TEMPLATE);
		standardTemplates.put(SINGLE_DATAMAP_CLASS, ClassGenerationAction.DATAMAP_SINGLE_CLASS_TEMPLATE);
		standardTemplates.put(STANDART_DATAMAP_SUPERCLASS, ClassGenerationAction.DATAMAP_SUPERCLASS_TEMPLATE);

		reverseStandartTemplates = new HashMap<>();
		reverseStandartTemplates.put(ClassGenerationAction.SUBCLASS_TEMPLATE, STANDARD_SERVER_SUBCLASS);
		reverseStandartTemplates.put(ClientClassGenerationAction.SUBCLASS_TEMPLATE, STANDARD_CLIENT_SUBCLASS);
		reverseStandartTemplates.put(ClassGenerationAction.SINGLE_CLASS_TEMPLATE, SINGLE_SERVER_CLASS);
		reverseStandartTemplates.put(ClientClassGenerationAction.SUPERCLASS_TEMPLATE, STANDARD_CLIENT_SUPERCLASS);
		reverseStandartTemplates.put(ClassGenerationAction.SUPERCLASS_TEMPLATE, STANDARD_SERVER_SUPERCLASS);

		reverseStandartTemplates.put(ClassGenerationAction.EMBEDDABLE_SUPERCLASS_TEMPLATE, STANDART_EMBEDDABLE_SUPERCLASS);
		reverseStandartTemplates.put(ClassGenerationAction.EMBEDDABLE_SUBCLASS_TEMPLATE, STANDART_EMBEDDABLE_SUBCLASS);
		reverseStandartTemplates.put(ClassGenerationAction.EMBEDDABLE_SINGLE_CLASS_TEMPLATE, SINGLE_EMBEDDABLE_CLASS);

		reverseStandartTemplates.put(ClassGenerationAction.DATAMAP_SUBCLASS_TEMPLATE, STANDART_DATAMAP_SUBCLASS);
		reverseStandartTemplates.put(ClassGenerationAction.DATAMAP_SINGLE_CLASS_TEMPLATE, SINGLE_DATAMAP_CLASS);
		reverseStandartTemplates.put(ClassGenerationAction.DATAMAP_SUPERCLASS_TEMPLATE, STANDART_DATAMAP_SUPERCLASS);
	}

	/**
	 * Updates custom templates from preferences.
	 */
	public void updateCustomTemplates(Preferences preference) {
		String[] keys = {};
		try {
			keys = preference.childrenNames();
		} catch (BackingStoreException e) {
			logger.warn("Error reading preferences");
		}
		this.customTemplates = new HashMap<>(keys.length, 1);
		for (String key : keys) {
			FSPath path = new FSPath(preference.node(key));
			customTemplates.put(key, path.getPath());
		}
	}

	// TODO: andrus, 12/5/2007 - this should also take a "pairs" parameter to
	// correctly
	// assign standard templates
	public String getTemplatePath(String name, Resource rootPath) {
		Object value = customTemplates.get(name);
		if (value != null) {
			if(rootPath != null) {
				Path path = Paths.get(rootPath.getURL().getPath()).getParent();
				value = path.relativize(Paths.get((String) value));
			}
			return value.toString();
		}

		value = standardTemplates.get(name);
		return value != null ? value.toString() : null;
	}

	public String getNameByPath(String name, Path rootPath) {
		String fullPath = rootPath.resolve(Paths.get(name)).normalize().toString();
		if(reverseCustomTemplate.containsKey(fullPath)){
			return reverseCustomTemplate.get(fullPath);
		} else {
			Object value = reverseStandartTemplates.get(name);
			return value != null ? value.toString() : null;
		}
	}

	public Map<String, String> getCustomTemplates() {
		return customTemplates;
	}

	public List<String> getStandardSubclassTemplates() {
		return standardSubclassTemplates;
	}

	public List<String> getStandardClientSubclassTemplates() {
		return standardClientSubclassTemplates;
	}

	public List<String> getStandardSuperclassTemplates() {
		return standardSuperclassTemplates;
	}

	public List<String> getStandardClientSuperclassTemplates() {
		return standardClientSuperclassTemplates;
	}

	public List<String> getStandartEmbeddableTemplates() {
		return standartEmbeddableTemplates;
	}

	public List<String> getStandartEmbeddableSuperclassTemplates() {
		return standartEmbeddableSuperclassTemplates;
	}

	public List<String> getStandartDataMapTemplates() { return standartDataMapTemplates; }

	public List<String> getStandartDataMapSuperclassTemplates() { return standartDataMapSuperclassTemplates; }
}
