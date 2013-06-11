/*
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
package org.universAAL.ri.gateway.communicator.service.impl;

import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.*;

public enum SecurityUtils {

	Instance;
	
	private static PaddedBufferedBlockCipher cipher;
    private static KeyParameter key;

    public synchronized void initialize(String encryptionKey){
    	if (null == cipher){
    		cipher = new PaddedBufferedBlockCipher(
                    new CBCBlockCipher(
                    new BlowfishEngine() ) );
    	}
        key = new KeyParameter( encryptionKey.getBytes() );
    }
	
    
    private static byte[] callCipher( byte[] data )
    throws CryptoException {
        int    size =
                cipher.getOutputSize( data.length );
        byte[] result = new byte[ size ];
        int    olen = cipher.processBytes( data, 0,
                data.length, result, 0 );
        olen += cipher.doFinal( result, olen );
        
        if( olen < size ){
            byte[] tmp = new byte[ olen ];
            System.arraycopy(
                    result, 0, tmp, 0, olen );
            result = tmp;
        }
        
        return result;
    }
    
    public synchronized byte[] encrypt( byte[] data )
    throws CryptoException {
        if( data == null || data.length == 0 ){
            return new byte[0];
        }
        
        cipher.init( true, key );
        return callCipher( data );
    }
    
    public synchronized byte[] decrypt( byte[] data )
    throws CryptoException {
        if( data == null || data.length == 0 ){
            return new byte[0];
        }
        
        cipher.init( false, key );
        return callCipher( data );
    }
    
}
