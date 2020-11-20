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
package org.apache.cayenne.crypto.transformer.value;

import org.apache.cayenne.crypto.transformer.bytes.BytesEncryptor;

/**
 * @since 4.0
 */
class DefaultValueEncryptor implements ValueEncryptor {

    private BytesConverter preConverter;
    private BytesConverter postConverter;

    public DefaultValueEncryptor(BytesConverter preConverter, BytesConverter postConverter) {
        this.preConverter = preConverter;
        this.postConverter = postConverter;
    }

    BytesConverter getPreConverter() {
        return preConverter;
    }

    BytesConverter getPostConverter() {
        return postConverter;
    }

    @Override
    public Object encrypt(BytesEncryptor encryptor, Object value) {

        // TODO: should we encrypt nulls as well to hide NULL from attackers?
        if (value == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        byte[] bytes = preConverter.toBytes(value);
        byte[] transformed = encryptor.encrypt(bytes, 0, new byte[1]);
        return postConverter.fromBytes(transformed);
    }

}
