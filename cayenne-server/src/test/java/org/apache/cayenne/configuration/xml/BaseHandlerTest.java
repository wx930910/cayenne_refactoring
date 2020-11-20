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

package org.apache.cayenne.configuration.xml;

import java.io.InputStream;

import org.apache.cayenne.util.Util;
import org.mockito.Mockito;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @since 4.1
 */
public abstract class BaseHandlerTest {

	protected void parse(String tag, HandlerFactory factory) throws Exception {
		try (InputStream in = BaseHandlerTest.class.getResource(getClass().getSimpleName() + ".xml").openStream()) {
			XMLReader parser = Util.createXmlReader();
			// DefaultHandler handler = new TestRootHandler(parser, tag,
			// factory);
			NamespaceAwareNestedTagHandler handler = Mockito.mock(NamespaceAwareNestedTagHandler.class,
					Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS)
							.useConstructor(new LoaderContext(parser, new DefaultHandlerFactory())));
			handler.setTargetNamespace("");
			final String rootTag = tag;
			Mockito.when(handler.processElement(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
					.thenReturn(false);
			Mockito.when(handler.createChildTagHandler(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
					Mockito.any())).thenAnswer(invo -> {
						String namespaceURI = invo.getArgument(0);
						String localName = invo.getArgument(1);
						String qName = invo.getArgument(2);
						Attributes attributes = invo.getArgument(3);
						if (localName.equals(rootTag)) {
							return factory.createHandler(handler);
						}
						return invo.callRealMethod();
					});
			parser.setContentHandler(handler);
			parser.parse(new InputSource(in));
		}
	}

	public interface HandlerFactory {
		NamespaceAwareNestedTagHandler createHandler(NamespaceAwareNestedTagHandler parent);
	}

	public static class TestRootHandler extends NamespaceAwareNestedTagHandler {

		private String rootTag;
		private BaseHandlerTest.HandlerFactory factory;

		public TestRootHandler(XMLReader parser, String rootTag, BaseHandlerTest.HandlerFactory factory) {
			super(new LoaderContext(parser, new DefaultHandlerFactory()));
			setTargetNamespace("");
			this.rootTag = rootTag;
			this.factory = factory;
		}

		@Override
		protected boolean processElement(String namespaceURI, String localName, Attributes attributes)
				throws SAXException {
			return false;
		}

		@Override
		protected ContentHandler createChildTagHandler(String namespaceURI, String localName, String qName,
				Attributes attributes) {
			if (localName.equals(rootTag)) {
				return factory.createHandler(this);
			}
			return super.createChildTagHandler(namespaceURI, localName, qName, attributes);
		}
	}

}
