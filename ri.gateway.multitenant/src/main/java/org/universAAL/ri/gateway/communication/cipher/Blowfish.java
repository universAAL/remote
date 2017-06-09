/*
 Copyright 2014 UPM, http://www.upm.es
 Life Supporting Technologies, TFB, Universidad Politécnica de Madrid
 
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

See the NOTICE file distributed with this work for additional
information regarding copyright ownership

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.universAAL.ri.gateway.communication.cipher;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * A BlowFish Cipher wrapper, using a symmetrical key encryption.
 * 
 * @author amedrano
 * 
 */
public class Blowfish implements Cipher {

	private final PaddedBufferedBlockCipher cipher;
	private final KeyParameter key;

	public Blowfish(final String encryptionKey) {
		cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new BlowfishEngine()));
		key = new KeyParameter(encryptionKey.getBytes());
	}

	private byte[] callCipher(final byte[] data) throws CryptoException {
		final int size = cipher.getOutputSize(data.length);
		byte[] result = new byte[size];
		int olen = cipher.processBytes(data, 0, data.length, result, 0);
		olen += cipher.doFinal(result, olen);

		if (olen < size) {
			final byte[] tmp = new byte[olen];
			System.arraycopy(result, 0, tmp, 0, olen);
			result = tmp;
		}

		return result;
	}

	public synchronized byte[] encrypt(final byte[] data) throws CryptoException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}

		cipher.init(true, key);
		return callCipher(data);
	}

	public synchronized byte[] decrypt(final byte[] data) throws CryptoException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}

		cipher.init(false, key);
		return callCipher(data);
	}

}
