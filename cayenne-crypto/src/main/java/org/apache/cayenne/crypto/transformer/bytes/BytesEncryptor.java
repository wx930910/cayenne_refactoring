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
package org.apache.cayenne.crypto.transformer.bytes;

/**
 * @since 4.0
 */
public interface BytesEncryptor {

    /**
     * Transform input bytes using default encryption key.
     * 
     * @param input
     *            a buffer with unencrypted bytes.
     * @param outputOffset
     *            how much empty space to leave in the beginning of the returned
     *            output array. This would allow the caller to prepend extra
     *            data to the encrypted array.
     * @param flags
     *            a byte[1] that allows nested encryptors to manipulate header
     *            flags.
     */
    byte[] encrypt(byte[] input, int outputOffset, byte[] flags);

}
