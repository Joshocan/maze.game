package Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOBuffer {
	private final ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
	private ByteBuffer dataByteBuffer = null;
	private boolean readLength = true;
	
	public ByteBuffer getDataByteBuffer() {
		return dataByteBuffer;
	}

	public void setDataByteBuffer(ByteBuffer dataByteBuffer) {
		this.dataByteBuffer = dataByteBuffer;
	}

	public boolean isReadLength() {
		return readLength;
	}

	public void setReadLength(boolean readLength) {
		this.readLength = readLength;
	}

	public ByteBuffer getLengthByteBuffer() {
		return lengthByteBuffer;
	}

	public static <T> void send(SocketChannel socket,  T data) throws IOException {
		System.out.println("3");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    for(int i=0;i<4;i++) baos.write(0);
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(data);
	    oos.close();
	    final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
	    wrap.putInt(0, baos.size()-4);
	    socket.write(wrap);
	    System.out.println("4");
	}

	public <T> T recv(SocketChannel socket, Class<T> clazz) throws IOException, ClassNotFoundException {
	    if (readLength) {
	        socket.read(lengthByteBuffer);
	        if (lengthByteBuffer.remaining() == 0) {
	            readLength = false;
	            dataByteBuffer = ByteBuffer.allocate(lengthByteBuffer.getInt(0));
	            lengthByteBuffer.clear();
	        }
	    } else {
	        socket.read(dataByteBuffer);
	        if (dataByteBuffer.remaining() == 0) {
	            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
	            @SuppressWarnings("unchecked")
				final T ret =  (T) ois.readObject();
	            // clean up
	            dataByteBuffer = null;
	            readLength = true;
	            return ret;
	        }
	    }
	    return null;
	}

	@Override
	public String toString() {
		return "NIOBuffer  readLength=" + readLength + "]";
	}
	
	
}
