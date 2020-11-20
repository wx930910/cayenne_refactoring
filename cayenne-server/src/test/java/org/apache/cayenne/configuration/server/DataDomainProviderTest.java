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
package org.apache.cayenne.configuration.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.DataChannel;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.DataRowStoreFactory;
import org.apache.cayenne.access.DefaultDataRowStoreFactory;
import org.apache.cayenne.access.dbsync.DefaultSchemaUpdateStrategyFactory;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategyFactory;
import org.apache.cayenne.access.dbsync.SkipSchemaUpdateStrategy;
import org.apache.cayenne.access.dbsync.ThrowOnPartialOrCreateSchemaStrategy;
import org.apache.cayenne.access.jdbc.SQLTemplateProcessor;
import org.apache.cayenne.access.jdbc.reader.RowReaderFactory;
import org.apache.cayenne.access.translator.batch.BatchTranslatorFactory;
import org.apache.cayenne.access.translator.batch.DefaultBatchTranslatorFactory;
import org.apache.cayenne.access.translator.select.DefaultSelectTranslatorFactory;
import org.apache.cayenne.access.translator.select.SelectTranslatorFactory;
import org.apache.cayenne.access.types.DefaultValueObjectTypeRegistry;
import org.apache.cayenne.access.types.ValueObjectTypeRegistry;
import org.apache.cayenne.annotation.PostLoad;
import org.apache.cayenne.ashwood.AshwoodEntitySorter;
import org.apache.cayenne.cache.QueryCache;
import org.apache.cayenne.configuration.ConfigurationNameMapper;
import org.apache.cayenne.configuration.ConfigurationTree;
import org.apache.cayenne.configuration.Constants;
import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.configuration.DataChannelDescriptorLoader;
import org.apache.cayenne.configuration.DataChannelDescriptorMerger;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.DefaultConfigurationNameMapper;
import org.apache.cayenne.configuration.DefaultDataChannelDescriptorMerger;
import org.apache.cayenne.configuration.DefaultRuntimeProperties;
import org.apache.cayenne.configuration.RuntimeProperties;
import org.apache.cayenne.configuration.mock.MockDataSourceFactory;
import org.apache.cayenne.dba.JdbcPkGenerator;
import org.apache.cayenne.dba.PkGenerator;
import org.apache.cayenne.dba.db2.DB2Adapter;
import org.apache.cayenne.dba.db2.DB2PkGenerator;
import org.apache.cayenne.dba.db2.DB2Sniffer;
import org.apache.cayenne.dba.derby.DerbyAdapter;
import org.apache.cayenne.dba.derby.DerbyPkGenerator;
import org.apache.cayenne.dba.derby.DerbySniffer;
import org.apache.cayenne.dba.firebird.FirebirdSniffer;
import org.apache.cayenne.dba.frontbase.FrontBaseAdapter;
import org.apache.cayenne.dba.frontbase.FrontBasePkGenerator;
import org.apache.cayenne.dba.frontbase.FrontBaseSniffer;
import org.apache.cayenne.dba.h2.H2Adapter;
import org.apache.cayenne.dba.h2.H2PkGenerator;
import org.apache.cayenne.dba.h2.H2Sniffer;
import org.apache.cayenne.dba.hsqldb.HSQLDBSniffer;
import org.apache.cayenne.dba.ingres.IngresAdapter;
import org.apache.cayenne.dba.ingres.IngresPkGenerator;
import org.apache.cayenne.dba.ingres.IngresSniffer;
import org.apache.cayenne.dba.mariadb.MariaDBSniffer;
import org.apache.cayenne.dba.mysql.MySQLAdapter;
import org.apache.cayenne.dba.mysql.MySQLPkGenerator;
import org.apache.cayenne.dba.mysql.MySQLSniffer;
import org.apache.cayenne.dba.openbase.OpenBaseAdapter;
import org.apache.cayenne.dba.openbase.OpenBasePkGenerator;
import org.apache.cayenne.dba.openbase.OpenBaseSniffer;
import org.apache.cayenne.dba.oracle.Oracle8Adapter;
import org.apache.cayenne.dba.oracle.OracleAdapter;
import org.apache.cayenne.dba.oracle.OraclePkGenerator;
import org.apache.cayenne.dba.oracle.OracleSniffer;
import org.apache.cayenne.dba.postgres.PostgresAdapter;
import org.apache.cayenne.dba.postgres.PostgresPkGenerator;
import org.apache.cayenne.dba.postgres.PostgresSniffer;
import org.apache.cayenne.dba.sqlite.SQLiteSniffer;
import org.apache.cayenne.dba.sqlserver.SQLServerAdapter;
import org.apache.cayenne.dba.sqlserver.SQLServerSniffer;
import org.apache.cayenne.dba.sybase.SybaseAdapter;
import org.apache.cayenne.dba.sybase.SybasePkGenerator;
import org.apache.cayenne.dba.sybase.SybaseSniffer;
import org.apache.cayenne.di.AdhocObjectFactory;
import org.apache.cayenne.di.ClassLoaderManager;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.di.spi.DefaultAdhocObjectFactory;
import org.apache.cayenne.di.spi.DefaultClassLoaderManager;
import org.apache.cayenne.event.EventBridge;
import org.apache.cayenne.event.EventManager;
import org.apache.cayenne.event.NoopEventBridgeProvider;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.log.Slf4jJdbcEventLogger;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntitySorter;
import org.apache.cayenne.map.LifecycleEvent;
import org.apache.cayenne.resource.ClassLoaderResourceLocator;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.junit.Test;
import org.mockito.Mockito;

import com.mockrunner.mock.jdbc.MockDataSource;

public class DataDomainProviderTest {

	@Test
	public void testGet() {

		// create dependencies
		final String testConfigName = "testConfig";
		final DataChannelDescriptor testDescriptor = new DataChannelDescriptor();

		DataMap map1 = new DataMap("map1");
		testDescriptor.getDataMaps().add(map1);

		DataMap map2 = new DataMap("map2");
		testDescriptor.getDataMaps().add(map2);

		DataNodeDescriptor nodeDescriptor1 = new DataNodeDescriptor();
		nodeDescriptor1.setName("node1");
		nodeDescriptor1.getDataMapNames().add("map1");
		nodeDescriptor1.setAdapterType(OracleAdapter.class.getName());
		nodeDescriptor1.setDataSourceFactoryType(MockDataSourceFactory.class.getName());
		nodeDescriptor1.setParameters("jdbc/testDataNode1");
		nodeDescriptor1.setSchemaUpdateStrategyType(ThrowOnPartialOrCreateSchemaStrategy.class.getName());
		testDescriptor.getNodeDescriptors().add(nodeDescriptor1);

		DataNodeDescriptor nodeDescriptor2 = new DataNodeDescriptor();
		nodeDescriptor2.setName("node2");
		nodeDescriptor2.getDataMapNames().add("map2");
		nodeDescriptor2.setParameters("testDataNode2.driver.xml");
		testDescriptor.getNodeDescriptors().add(nodeDescriptor2);

		final DataChannelDescriptorLoader testLoader = new DataChannelDescriptorLoader() {

			@Override
			public ConfigurationTree<DataChannelDescriptor> load(Resource configurationResource)
					throws ConfigurationException {
				return new ConfigurationTree<>(testDescriptor, null);
			}
		};

		final EventManager eventManager = Mockito.mock(EventManager.class);
		final TestListener mockListener = new TestListener();

		Module testModule = binder -> {
			final ClassLoaderManager classLoaderManager = new DefaultClassLoaderManager();
			binder.bind(ClassLoaderManager.class).toInstance(classLoaderManager);
			binder.bind(AdhocObjectFactory.class).to(DefaultAdhocObjectFactory.class);

			ServerModule.contributeProperties(binder);

			ServerModule.contributeAdapterDetectors(binder).add(FirebirdSniffer.class).add(OpenBaseSniffer.class)
					.add(FrontBaseSniffer.class).add(IngresSniffer.class).add(SQLiteSniffer.class).add(DB2Sniffer.class)
					.add(H2Sniffer.class).add(HSQLDBSniffer.class).add(SybaseSniffer.class).add(DerbySniffer.class)
					.add(SQLServerSniffer.class).add(OracleSniffer.class).add(PostgresSniffer.class)
					.add(MySQLSniffer.class).add(MariaDBSniffer.class);
			ServerModule.contributeDomainFilters(binder);
			ServerModule.contributeDomainQueryFilters(binder);
			ServerModule.contributeDomainSyncFilters(binder);
			ServerModule.contributeDomainListeners(binder).add(mockListener);
			ServerModule.contributeProjectLocations(binder).add(testConfigName);

			binder.bind(PkGenerator.class).to(JdbcPkGenerator.class);
			binder.bind(PkGeneratorFactoryProvider.class).to(PkGeneratorFactoryProvider.class);
			ServerModule.contributePkGenerators(binder).put(DB2Adapter.class.getName(), DB2PkGenerator.class)
					.put(DerbyAdapter.class.getName(), DerbyPkGenerator.class)
					.put(FrontBaseAdapter.class.getName(), FrontBasePkGenerator.class)
					.put(H2Adapter.class.getName(), H2PkGenerator.class)
					.put(IngresAdapter.class.getName(), IngresPkGenerator.class)
					.put(MySQLAdapter.class.getName(), MySQLPkGenerator.class)
					.put(OpenBaseAdapter.class.getName(), OpenBasePkGenerator.class)
					.put(OracleAdapter.class.getName(), OraclePkGenerator.class)
					.put(Oracle8Adapter.class.getName(), OraclePkGenerator.class)
					.put(PostgresAdapter.class.getName(), PostgresPkGenerator.class)
					.put(SQLServerAdapter.class.getName(), SybasePkGenerator.class)
					.put(SybaseAdapter.class.getName(), SybasePkGenerator.class);

			// configure extended types
			ServerModule.contributeDefaultTypes(binder);
			ServerModule.contributeUserTypes(binder);
			ServerModule.contributeTypeFactories(binder);

			binder.bind(EventManager.class).toInstance(eventManager);
			binder.bind(EntitySorter.class).toInstance(new AshwoodEntitySorter());
			binder.bind(SchemaUpdateStrategyFactory.class).to(DefaultSchemaUpdateStrategyFactory.class);

			final ResourceLocator locator = new ClassLoaderResourceLocator(classLoaderManager) {

				public Collection<Resource> findResources(String name) {
					// ResourceLocator also used by JdbcAdapter to locate
					// types.xml...
					// if this is the request we are getting, just let it go
					// through..
					if (name.endsWith("types.xml")) {
						return super.findResources(name);
					}
					Resource resource = Mockito.mock(Resource.class);
					assertEquals(testConfigName, name);
					return Collections.<Resource> singleton(resource);
				}
			};

			binder.bind(ResourceLocator.class).toInstance(locator);
			binder.bind(Key.get(ResourceLocator.class, Constants.SERVER_RESOURCE_LOCATOR)).toInstance(locator);
			binder.bind(ConfigurationNameMapper.class).to(DefaultConfigurationNameMapper.class);
			binder.bind(DataChannelDescriptorMerger.class).to(DefaultDataChannelDescriptorMerger.class);
			binder.bind(DataChannelDescriptorLoader.class).toInstance(testLoader);
			binder.bind(DbAdapterFactory.class).to(DefaultDbAdapterFactory.class);
			binder.bind(RuntimeProperties.class).to(DefaultRuntimeProperties.class);
			binder.bind(BatchTranslatorFactory.class).to(DefaultBatchTranslatorFactory.class);
			binder.bind(SelectTranslatorFactory.class).to(DefaultSelectTranslatorFactory.class);
			DataSourceFactory factory = Mockito.mock(DataSourceFactory.class);
			try {
				Mockito.when(factory.getDataSource(Mockito.any())).thenReturn(new MockDataSource());
			} catch (Exception e) {
				e.printStackTrace();
			}
			binder.bind(DataSourceFactory.class).toInstance(factory);
			binder.bind(JdbcEventLogger.class).to(Slf4jJdbcEventLogger.class);
			binder.bind(QueryCache.class).toInstance(mock(QueryCache.class));
			binder.bind(RowReaderFactory.class).toInstance(mock(RowReaderFactory.class));
			binder.bind(DataNodeFactory.class).to(DefaultDataNodeFactory.class);
			binder.bind(SQLTemplateProcessor.class).toInstance(mock(SQLTemplateProcessor.class));

			binder.bind(EventBridge.class).toProvider(NoopEventBridgeProvider.class);
			binder.bind(DataRowStoreFactory.class).to(DefaultDataRowStoreFactory.class);

			ServerModule.contributeValueObjectTypes(binder);
			binder.bind(ValueObjectTypeRegistry.class).to(DefaultValueObjectTypeRegistry.class);
		};

		Injector injector = DIBootstrap.createInjector(testModule);

		// create and initialize provide instance to test
		DataDomainProvider provider = new DataDomainProvider();
		injector.injectMembers(provider);

		DataChannel channel = provider.get();
		assertNotNull(channel);

		assertTrue(channel instanceof DataDomain);

		DataDomain domain = (DataDomain) channel;
		assertSame(eventManager, domain.getEventManager());
		assertEquals(2, domain.getDataMaps().size());
		assertTrue(domain.getDataMaps().contains(map1));
		assertTrue(domain.getDataMaps().contains(map2));

		assertEquals(2, domain.getDataNodes().size());
		DataNode node1 = domain.getDataNode("node1");
		assertNotNull(node1);
		assertEquals(1, node1.getDataMaps().size());
		assertSame(map1, node1.getDataMaps().iterator().next());
		assertSame(node1, domain.lookupDataNode(map1));
		assertEquals(nodeDescriptor1.getDataSourceFactoryType(), node1.getDataSourceFactory());
		assertNotNull(node1.getDataSource());

		assertNotNull(node1.getSchemaUpdateStrategy());
		assertEquals(nodeDescriptor1.getSchemaUpdateStrategyType(),
				node1.getSchemaUpdateStrategy().getClass().getName());

		assertNotNull(node1.getAdapter());
		assertEquals(OracleAdapter.class, node1.getAdapter().getClass());

		DataNode node2 = domain.getDataNode("node2");
		assertNotNull(node2);
		assertEquals(1, node2.getDataMaps().size());
		assertSame(map2, node2.getDataMaps().iterator().next());
		assertSame(node2, domain.lookupDataNode(map2));
		assertNull(node2.getDataSourceFactory());
		assertNotNull(node2.getDataSource());
		assertNotNull(node2.getSchemaUpdateStrategy());
		assertEquals(SkipSchemaUpdateStrategy.class.getName(), node2.getSchemaUpdateStrategy().getClass().getName());

		assertNotNull(node2.getAdapter());

		// check that we have mock listener passed correctly
		Persistent mockPersistent = mock(Persistent.class);
		ObjectId mockObjectId = mock(ObjectId.class);
		when(mockObjectId.getEntityName()).thenReturn("mock-entity-name");
		when(mockPersistent.getObjectId()).thenReturn(mockObjectId);
		domain.getEntityResolver().getCallbackRegistry().performCallbacks(LifecycleEvent.POST_LOAD, mockPersistent);

		assertEquals("Should call postLoadCallback() method", 1, TestListener.counter.get());
	}

	static class TestListener {

		static private AtomicInteger counter = new AtomicInteger();

		@PostLoad
		public void postLoadCallback(Object object) {
			counter.incrementAndGet();
		}
	}
}
