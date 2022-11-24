import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class Server extends Node {
	static final int DEFAULT_SRC_PORT = 50001;
	static final String WORKER1_DST_NODE = "worker1";
	static final int WORKER1_DST_PORT = 50002;
	static final String WORKER2_DST_NODE = "worker2";
	static final int WORKER2_DST_PORT = 50003;
	static final String WORKER3_DST_NODE = "worker3";
	static final int WORKER3_DST_PORT = 50004;
	SocketAddress clientDstAddress;
	InetSocketAddress workerDstAddress;
	static int workerChoice = 1;
	/*
	 *
	 */
	Server(int srcPort) {
		try {
			socket = new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	/**
	 *
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			System.out.println("Received packet");

			PacketContent content= PacketContent.fromDatagramPacket(packet);

			if (content.getType()==PacketContent.FILENAME) {

				DatagramPacket response;
				response= new AckPacketContent("OK - Received this").toDatagramPacket();
				response.setSocketAddress(packet.getSocketAddress());
				socket.send(response);
				clientDstAddress = packet.getSocketAddress();

				System.out.println("File name: " + ((FileName)content).getFileName());

				if (workerChoice == 1) {
					workerDstAddress = new InetSocketAddress(WORKER1_DST_NODE, WORKER1_DST_PORT);
					System.out.println("Sending to worker 1 ");
				}
				else if (workerChoice == 2) {
					workerDstAddress = new InetSocketAddress(WORKER2_DST_NODE, WORKER2_DST_PORT);
					System.out.println("Sending to worker 2 ");
				}
				else if (workerChoice == 3) {
					workerDstAddress = new InetSocketAddress(WORKER3_DST_NODE, WORKER3_DST_PORT);
					System.out.println("Sending to worker 3 ");
				}
				workerChoice ++;
				if (workerChoice > 3) {
					workerChoice = 1;
				}

				packet.setSocketAddress(workerDstAddress);
				socket.send(packet);
				System.out.println("Packet sent to worker");
			}

			if (content.getType() == PacketContent.FILEINFO) {
				String fileName = ((FileInfoContent)content).getFileName();
				System.out.println("File name: \"" + fileName + "\"");
				System.out.println("File size: " + ((FileInfoContent)content).getFileSize());
				System.out.println("File buffer: " + new String(((FileInfoContent)content).getFileBuffer()));

				DatagramPacket response;
				response= new AckPacketContent("OK - Server Received this packet from client").toDatagramPacket();
				response.setSocketAddress(packet.getSocketAddress());
				socket.send(response);

				packet.setSocketAddress(clientDstAddress);
				socket.send(packet);
				System.out.println("Packet sent to Client");
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}

	/*
	 *
	 */
	public static void main(String[] args) {
		try {
			(new Server(DEFAULT_SRC_PORT)).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
