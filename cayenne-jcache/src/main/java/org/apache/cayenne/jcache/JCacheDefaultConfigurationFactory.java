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

package org.apache.cayenne.jcache;

import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * <p>
 *     Default JCache configuration factory.
 * </p>
 * <p>
 *     Parameters:
 *     <ul>
 *         <li>store-by-reference</li>
 *         <li>expire in 10 minutes</li>
 *     </ul>
 * </p>
 *
 * @since 4.0
 */
public class JCacheDefaultConfigurationFactory implements JCacheConfigurationFactory {

    private final Configuration<Object, Object> configuration = new MutableConfiguration<>()
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES));

    /**
     * @param cacheGroup is unused by default configuration factory
     * @return cache configuration
     */
    public Configuration<Object, Object> create(String cacheGroup) {
        return configuration;
    }
}
