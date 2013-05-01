package de.dailab.ima.nemo.transportAdapter;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.SocketAddress;

/**
 * Handles messages from clients
 */
public class ConnectionReader {
	public static final int BUFFER_SIZE = 256;
	
	protected SocketChannel _sc;
	protected SelectionKey _key;
	protected String _inboundData = "";

	/**
	 * Create a new ConnectionReader
	 * @param key the SocketChannel of the client
	 */
	public ConnectionReader(SelectionKey key) {
		this._key = key;
	}

	/**
	 * Read data from client.
	 * @throws IOException
	 */
	public void read() throws IOException {
		_sc = (SocketChannel) _key.channel();
		SocketAddress address = _sc.socket().getRemoteSocketAddress();
		System.out.println("Reading from " + address);

		ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
		
		// Read all data from socket.
		while (true) {
			buf.clear();
			int numBytesRead = _sc.read(buf);

			// Channel closed
			if (numBytesRead == -1) {
				System.out.println("client on " + address + " has disconnected");
				//_sc.close(); TD: not needed, client closed the channel.
				_key.cancel();
				break;
			}

			// Data available
			if (numBytesRead > 0) {
				buf.flip();
				String str = new String(buf.array(), 0, numBytesRead);
				_inboundData += str;
			}

			// Channel buffer empty
			if (numBytesRead < BUFFER_SIZE) {
				break;
			}
		}// while
		
		// do something with data
		System.out.println("From " + address + ": " + _inboundData);
		_inboundData = "";

		// TD: should check for OP_WRITE before write; no need to check if _sc is connected and avoid this wierd problem:
		// still valid right after channel is closed???
		if (_sc.isConnected()) { 
			ByteBuffer outBuf = ByteBuffer.allocate(BUFFER_SIZE);
			outBuf.clear();
			outBuf.put("_inboundData".getBytes(), 0, "_inboundData".length()); // put more than BUFFER_LENGTH causes exception.
			outBuf.flip();
			while (outBuf.hasRemaining()) {
				_sc.write(outBuf);
			}
		}
	}
} //.
 

