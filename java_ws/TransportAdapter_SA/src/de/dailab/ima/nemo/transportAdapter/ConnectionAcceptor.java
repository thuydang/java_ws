package de.dailab.ima.nemo.transportAdapter;
//

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;



/**
 * Handles client connections.
 */
public class ConnectionAcceptor {
	protected Selector _selector;
	protected ServerSocketChannel _ssc;

	/** 
	 * Creates a new ConnectionAcceptor
	 * @param selector 
	 * @param ssc the ServerSocketChannel
	 */
	 public ConnectionAcceptor(Selector selector, ServerSocketChannel ssc) {
		 this._selector = selector;
		 this._ssc = ssc;
	 }

	 /**
		* Accept a connection.
		*/
	 public void accept() throws IOException {
		 // 
		 SocketChannel sc = _ssc.accept();

		 // For non-blocking server socket, sc might be null if no connection 
		 // attempt from client. 
		 if (sc != null) {
			 SocketAddress address = sc.socket().getRemoteSocketAddress();
			 System.out.println("Accepting connection from " + address);
			 sc.configureBlocking(false);
			 SelectionKey key = sc.register(_selector, SelectionKey.OP_READ);
			 key.attach(new ConnectionReader(key));
			 // greeting client
			 ByteBuffer buf = ByteBuffer.allocate(128); // pos=mark=0 lim=cap=128
			 buf.clear(); // buf reset
			 buf.put("welcome to the jungle!\n".getBytes()); // mark=0 pos='wel...' lim=cap=128
			 buf.flip(); // mark=0 pos=0 lim='wel...' cap=128
			 sc.write(buf); // write from pos to lim
		 }
	
	 }
}
