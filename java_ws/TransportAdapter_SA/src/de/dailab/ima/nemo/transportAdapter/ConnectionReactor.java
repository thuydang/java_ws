package de.dailab.ima.nemo.transportAdapter;
// package de.dailab.ima.nemo.network.transportadapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;



/**
 * A Server side connection listener which implements Reactor pattern.
 * All clients' connections are handled here and incoming data 
 * is dispatched to registered Concrete Event Handlers.
 */
public class ConnectionReactor extends Thread {

	protected int _port;
	protected volatile boolean _running = true; 

	/**
	 * Returns the listening port
	 * @return the lsitening port of the ConnectionReactor
	 */
	public int getPort() {
		return _port;
	}

	/**
	 * Create a new Reactor
	 * @param port the port on which the Reactor listen to incoming connections
	 * @throws IOException 
	 * Best performance is achieved when handling Accept in this thread and 
	 * handling Read/Write in other thread(s).
	 * http://jfarcand.wordpress.com/?s=nio+
	 */
	public ConnectionReactor(int port) throws IOException {
		this._port = port;
	}

	public void run() {
		try {
			
			// Create a non-blocking connections listener
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(_port));

			// Create the selector
			Selector selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT, new ConnectionAcceptor(selector, ssc));

			while (_running) {
				// listenning 
				selector.select();

				//
				Iterator it = selector.selectedKeys().iterator();

				while (it.hasNext()) {
					SelectionKey sKey = (SelectionKey)it.next();

					it.remove();

					// new connection
					if (sKey.isValid() && sKey.isAcceptable()) {
						ConnectionAcceptor connectionAcceptor = (ConnectionAcceptor)sKey.attachment();
						connectionAcceptor.accept();
					}

					// inbound data from ongonging connection
					if (sKey.isValid() && sKey.isReadable()) {
						ConnectionReader connectionReader = (ConnectionReader)sKey.attachment();
						connectionReader.read();
					}
				}
			}
		} catch (IOException e) {
		  e.printStackTrace(System.err);
		}
		// destroy ConnectionReactor? notifies Message Handler?
		destroyConnectionReactor();
	}

	/**
	 * Stop the ConnectionReactor
	 */
	public void destroyConnectionReactor() {
		_running = false;
		//TODO: notify Mesage Handlers?
	}

	public static void main(String args[]) {
		int port = 8989;
		if (args.length != 1) {
			System.err.println("Warning: no port specified! using port 8989");
			System.err.println("Usage: java ConnectionReactor <port>");
			
		} else {
			port = Integer.parseInt(args[0]);
		}
		
		try {
			ConnectionReactor cReactor = new ConnectionReactor(port);
			cReactor.start();
			System.out.println("ConnectionReactor is listening on port " + cReactor.getPort());
			cReactor.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	} // end main

}

