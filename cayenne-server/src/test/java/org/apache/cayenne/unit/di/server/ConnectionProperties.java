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

package org.apache.cayenne.unit.di.server;

import org.apache.cayenne.conn.DataSourceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ConnectionProperties handles a set of DataSourceInfo objects using
 * information stored in $HOME/.cayenne/connection.properties. As of now this is
 * purely a utility class. Its features are not used in deployment.
 */
class ConnectionProperties {

	static final int MIN_CONNECTIONS = 1;
	static final int MAX_CONNECTIONS = 2;

	private static final String ADAPTER_KEY = "adapter";
	private static final String ADAPTER20_KEY = "cayenne.adapter";
	private static final String USER_NAME_KEY = "jdbc.username";
	private static final String PASSWORD_KEY = "jdbc.password";
	private static final String URL_KEY = "jdbc.url";
	private static final String DRIVER_KEY = "jdbc.driver";

	private Map<String, DataSourceInfo> connectionInfos;

	/**
	 * Constructor for ConnectionProperties.
	 */
	ConnectionProperties(Map<String, String> props) {
		connectionInfos = new HashMap<>();
		for (String name : extractNames(props)) {
			DataSourceInfo dsi = buildDataSourceInfo(
					props.entrySet().stream()
							.filter(e -> e.getKey().startsWith(name + '.'))
							.collect(Collectors.toMap(
									e -> {
										String key = e.getKey();
										return key.substring(key.indexOf('.') + 1);
									},
									Map.Entry::getValue
							))
			);
			connectionInfos.put(name, dsi);
		}
	}

	int size() {
		return connectionInfos.size();
	}

	/**
	 * Returns DataSourceInfo object for a symbolic name. If name does not match
	 * an existing object, returns null.
	 */
	DataSourceInfo getConnection(String name) {
		return connectionInfos.get(name);
	}

	/**
	 * Creates a DataSourceInfo object from a set of properties.
	 */
	private DataSourceInfo buildDataSourceInfo(Map<String, String> props) {
		DataSourceInfo dsi = new DataSourceInfo();

		String adapter = props.get(ADAPTER_KEY);

		// try legacy adapter key
		if (adapter == null) {
			adapter = props.get(ADAPTER20_KEY);
		}

		dsi.setAdapterClassName(adapter);
		dsi.setUserName(props.get(USER_NAME_KEY));
		dsi.setPassword(props.get(PASSWORD_KEY));
		dsi.setDataSourceUrl(props.get(URL_KEY));
		dsi.setJdbcDriver(props.get(DRIVER_KEY));
		dsi.setMinConnections(MIN_CONNECTIONS);
		dsi.setMaxConnections(MAX_CONNECTIONS);

		return dsi;
	}

	/**
	 * Returns a list of connection names configured in the properties object.
	 */
	private List<String> extractNames(Map<String, String> props) {
		Iterator<?> it = props.keySet().iterator();
		List<String> list = new ArrayList<>();

		while (it.hasNext()) {
			String key = (String) it.next();

			int dotInd = key.indexOf('.');
			if (dotInd <= 0 || dotInd >= key.length()) {
				continue;
			}

			String name = key.substring(0, dotInd);
			if (!list.contains(name)) {
				list.add(name);
			}
		}

		return list;
	}
}
