package org.universAAL.samples;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Assert;
import org.junit.Test;

public class InteruptedSocketReadTest {

    private class InputReader implements Runnable {

	InputStream in;

	public InputReader(final InputStream in) {
	    this.in = in;
	}

	public void run() {
	    while (true) {
		try {
		    System.out.print("Waiting for data: ");
		    System.out.println(in.read());
		} catch (final Exception e) {
		    e.printStackTrace();
		    return;
		}
	    }
	}

    }

    private class OutputWriter implements Runnable {

	private final OutputStream out;
	private boolean stop;

	public OutputWriter(final OutputStream out) {
	    this.out = out;
	}

	public void stop() {
	    this.stop = true;
	}

	public void run() {
	    final byte[] req = request.getBytes();
	    for (int i = 0; i < req.length; i++) {
		try {
		    out.write(req[i]);
		} catch (final Exception e) {
		    e.printStackTrace();
		    return;
		}
	    }
	}

    }

    @Test
    public void testInteruptableSocketRead() {
	Socket socket = null;
	InputStream in = null;
	try {
	    socket = new Socket("www.google.it", 80);
	    in = socket.getInputStream();
	} catch (final Exception e) {
	    Assert.fail("Unable to create the socket");
	}
	final InputReader runner = new InputReader(in);
	final Thread thread = new Thread(runner, "ReadingThread");

	System.out.println("Waiting for thread to read all the data");
	thread.start();

	try {
	    Thread.sleep(2 * 1000);
	} catch (final InterruptedException e) {
	    e.printStackTrace();
	}

	if (thread.isAlive()) {
	    System.out.println("Interrupting the reading thread");
	    try {
		in.close();
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	    thread.interrupt();
	}
	try {
	    thread.join(1000);
	} catch (final InterruptedException e) {
	    e.printStackTrace();
	}
	Assert.assertFalse(thread.isAlive());
    }

    String request = "GET / HTTP/1.1"
	    + "\r\n"
	    + "Accept:	text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
	    + "\r\n"
	    + "Accept-Encoding:	gzip, deflate"
	    + "\r\n"
	    + "Accept-Language:	it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3"
	    + "\r\n"
	    + "Connection:	keep-alive"
	    + "\r\n"
	    + "Host:	www.google.it"
	    + "\r\n"
	    + "User-Agent:	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"
	    + "\r\n";

    @Test
    public void testInteruptableSocketWrite() {
	Socket socket = null;
	OutputStream out = null;
	try {
	    socket = new Socket("www.google.it", 80);
	    out = socket.getOutputStream();
	} catch (final Exception e) {
	    Assert.fail("Unable to create the socket");
	}
	final OutputWriter runner = new OutputWriter(out);
	final Thread thread = new Thread(runner, "WritingThread");

	System.out.println("Waiting for thread to write all the data");
	thread.start();

	if (thread.isAlive()) {
	    System.out.println("Interrupting the writing thread");
	    try {
		out.close();
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	    runner.stop();
	    thread.interrupt();
	}
	try {
	    thread.join(1000);
	} catch (final InterruptedException e) {
	    e.printStackTrace();
	}
	Assert.assertFalse(thread.isAlive());
    }
}
