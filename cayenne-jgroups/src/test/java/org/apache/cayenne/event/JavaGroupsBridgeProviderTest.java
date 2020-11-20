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

package org.apache.cayenne.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.configuration.Constants;
import org.apache.cayenne.configuration.DefaultRuntimeProperties;
import org.apache.cayenne.configuration.RuntimeProperties;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.log.Slf4jJdbcEventLogger;
import org.apache.cayenne.tx.DefaultTransactionFactory;
import org.apache.cayenne.tx.DefaultTransactionManager;
import org.apache.cayenne.tx.TransactionFactory;
import org.apache.cayenne.tx.TransactionManager;
import org.junit.Test;
import org.mockito.Mockito;

public class JavaGroupsBridgeProviderTest {

	private static final DataDomain DOMAIN = new DataDomain("test");
	private static final EventManager EVENT_MANAGER = new DefaultEventManager();
	protected static final String MCAST_ADDRESS_TEST = "192.168.0.0";
	protected static final String MCAST_PORT_TEST = "1521";
	protected static final String CONFIG_URL_TEST = "somehost.com";

	@Test
	public void testGetJavaGroupsBridge() throws Exception {
		Module defaultBinding = Mockito.mock(Module.class);
		Mockito.doAnswer(invocation -> {
			Binder binder = invocation.getArgument(0);
			binder.bindMap(String.class, Constants.PROPERTIES_MAP);
			binder.bind(DataDomain.class).toInstance(DOMAIN);
			binder.bind(EventManager.class).toInstance(EVENT_MANAGER);
			binder.bind(TransactionManager.class).to(DefaultTransactionManager.class);
			binder.bind(TransactionFactory.class).to(DefaultTransactionFactory.class);
			binder.bind(JdbcEventLogger.class).to(Slf4jJdbcEventLogger.class);
			binder.bind(RuntimeProperties.class).to(DefaultRuntimeProperties.class);
			return null;
		}).when(defaultBinding).configure(Mockito.any());
		Injector injector = DIBootstrap.createInjector(defaultBinding, new JGroupsModule());
		EventBridge bridge = injector.getInstance(EventBridge.class);

		assertNotNull(bridge);
		assertTrue(bridge instanceof JavaGroupsBridge);
	}

	@Test
	public void testUseProperties() throws Exception {
		Module module = binder -> {
			JGroupsModule.contributeMulticastAddress(binder, MCAST_ADDRESS_TEST);
			JGroupsModule.contributeMulticastPort(binder, Integer.parseInt(MCAST_PORT_TEST));
			JGroupsModule.contributeConfigUrl(binder, CONFIG_URL_TEST);
		};
		Module defaultBinding = Mockito.mock(Module.class);
		Mockito.doAnswer(invocation -> {
			Binder binder = invocation.getArgument(0);
			binder.bindMap(String.class, Constants.PROPERTIES_MAP);
			binder.bind(DataDomain.class).toInstance(DOMAIN);
			binder.bind(EventManager.class).toInstance(EVENT_MANAGER);
			binder.bind(TransactionManager.class).to(DefaultTransactionManager.class);
			binder.bind(TransactionFactory.class).to(DefaultTransactionFactory.class);
			binder.bind(JdbcEventLogger.class).to(Slf4jJdbcEventLogger.class);
			binder.bind(RuntimeProperties.class).to(DefaultRuntimeProperties.class);
			return null;
		}).when(defaultBinding).configure(Mockito.any());
		Injector injector = DIBootstrap.createInjector(defaultBinding, new JGroupsModule(), module);
		JavaGroupsBridge bridge = (JavaGroupsBridge) injector.getInstance(EventBridge.class);

		assertEquals(MCAST_ADDRESS_TEST, bridge.getMulticastAddress());
		assertEquals(MCAST_PORT_TEST, bridge.getMulticastPort());
		assertEquals(CONFIG_URL_TEST, bridge.getConfigURL());
	}

	@Test
	public void testUseDefaultProperties() throws Exception {
		Module defaultBinding = Mockito.mock(Module.class);
		Mockito.doAnswer(invocation -> {
			Binder binder = invocation.getArgument(0);
			binder.bindMap(String.class, Constants.PROPERTIES_MAP);
			binder.bind(DataDomain.class).toInstance(DOMAIN);
			binder.bind(EventManager.class).toInstance(EVENT_MANAGER);
			binder.bind(TransactionManager.class).to(DefaultTransactionManager.class);
			binder.bind(TransactionFactory.class).to(DefaultTransactionFactory.class);
			binder.bind(JdbcEventLogger.class).to(Slf4jJdbcEventLogger.class);
			binder.bind(RuntimeProperties.class).to(DefaultRuntimeProperties.class);
			return null;
		}).when(defaultBinding).configure(Mockito.any());
		Injector injector = DIBootstrap.createInjector(defaultBinding, new JGroupsModule());
		JavaGroupsBridge bridge = (JavaGroupsBridge) injector.getInstance(EventBridge.class);

		assertEquals(JavaGroupsBridge.MCAST_ADDRESS_DEFAULT, bridge.getMulticastAddress());
		assertEquals(JavaGroupsBridge.MCAST_PORT_DEFAULT, bridge.getMulticastPort());
		assertNull(bridge.getConfigURL());
	}

}
