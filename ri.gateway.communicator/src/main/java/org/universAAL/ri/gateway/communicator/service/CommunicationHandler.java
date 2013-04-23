package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;
import java.net.URL;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.ri.gateway.communicator.service.impl.MessageWrapper;

public interface CommunicationHandler {
    public MessageWrapper sendMessage(MessageWrapper toSend, URL target) throws IOException, ClassNotFoundException, CryptoException;
    public void start() throws Exception;
    public void stop();
}
