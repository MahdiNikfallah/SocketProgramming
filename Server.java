import java.io.*;
import java.math.MathContext;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	int port;
	ServerSocket server = null;
	Socket client = null;
	Socket client2 = null;
	ExecutorService pool = null;
	int clientcount = 0;
	static ArrayList<ServerThread> clients;
	static ArrayList<Socket> clientsSocket;
	static DatagramSocket datagramSocket;
	static DatagramPacket datagramPacket;
	static byte[] receiveData;
	static HashMap<String, ArrayList<String>> mailBox;
	static HashMap<String, ByteArrayOutputStream> audioBox2;
	static ByteArrayOutputStream byteArray;

	static HashMap<String,  ArrayList<String>> matches;

	public static void main(String[] args) throws IOException {
		audioBox2 = new HashMap<>();
		mailBox = new HashMap<>();
		datagramSocket = new DatagramSocket(4000, InetAddress.getByName("127.0.0.1"));
		receiveData = new byte[10000];
		clients = new ArrayList<>();
		clientsSocket = new ArrayList<>();
		Server serverobj = new Server(5000);
		serverobj.startServer();
	}

	Server(int port) {
		this.port = port;
		pool = Executors.newCachedThreadPool();
	}

	public void startServer() throws IOException {
		server = new ServerSocket(5000);
		System.out.println("Server Booted");
		System.out.println("Any client can stop the server by sending -1");
		while (true) {
			client = server.accept();
			client2 = server.accept();
			clientcount++;
			ServerThread runnable = new ServerThread(client,client2, clientcount, this);
			clients.add(runnable);
			// clientss.add(e)
			clientsSocket.add(client);
			pool.execute(runnable);
		}
	}
	@SuppressWarnings("unused")
	private static class ServerThread implements Runnable {

		boolean exit;
		Server server = null;
		Socket client = null,client2=null;
		BufferedReader cin,cinR;
		ObjectOutputStream objectOutputStream;
		PrintStream cout,coutR;
		Scanner sc = new Scanner(System.in);
		int id;
		String s;
		String name;
		int port;
		ArrayList<String> reqnames;

		ServerThread(Socket client,Socket client2, int count, Server server) throws IOException {
			reqnames = new ArrayList<>();
			matches = new HashMap<>();
			objectOutputStream = new ObjectOutputStream(client.getOutputStream());
			exit = false;
			this.client = client;
			this.client2 = client2;
			this.server = server;
			this.id = count;
			cin = new BufferedReader(new InputStreamReader(client.getInputStream()));
			cout = new PrintStream(client.getOutputStream());
			cinR = new BufferedReader(new InputStreamReader(client2.getInputStream()));
			coutR = new PrintStream(client2.getOutputStream());
			cout.println("Acknowledge");
			name = cin.readLine();
			port = Integer.parseInt(cin.readLine());
			System.out.println("client " + name + " established with server from port: " + client.getPort());
		}

		@Override
		public void run() {
			while (!exit) {
				try {
					s = cin.readLine();
					switch (s) {
					case "Disconnect":
						cout.println("GoodBye!");
						cin.close();
						client.close();
						cout.close();
						clients.remove(this);
						clientsSocket.remove(this.client);
						exit = true;
						break;
					case "List":
						ArrayList<String> x = new ArrayList<String>();
						for (int i = 0; i < clients.size(); i++) {
							x.add(clients.get(i).name);
						}
						objectOutputStream.writeObject(x);
						break;
					case "Request":
						cout.println("to who?");
						s = cin.readLine();
						boolean isFind = false;
						for (int i = 0; i < clients.size(); i++) {
							if (s.equals(clients.get(i).name)) {
								clients.get(i).reqnames.add(name);
								if (mailBox.containsKey(clients.get(i).name)) {
									mailBox.replace(clients.get(i).name, clients.get(i).reqnames);
								} else {
									mailBox.put(clients.get(i).name, clients.get(i).reqnames);
								}
								isFind = true;
								cout.println("Sent");
							}
						}
						if (isFind == false) {
							cout.println("Client not find!");
						}
						break;
					case "ReqList":
						System.out.println(mailBox.toString());
						if (mailBox.containsKey(name)) {
							cout.println("YES");
							int size = mailBox.get(name).size();
							cout.println(mailBox.get(name).size());
							System.out.println(mailBox.get(name).size());
							for (int i = 0; i < size; i++) {
								cout.println(mailBox.get(name).get(i));
								s = cin.readLine();
								if (s.equals("yes")) {
									ArrayList<String> tmp = matches.get(name);
									if(tmp==null){
										tmp= new ArrayList<>();
									}
									tmp.add(mailBox.get(name).get(i));
									if(matches.containsKey(name))
										matches.remove(name);
									matches.put(name, tmp);//(name, mailBox.get(name).get(i));
									tmp = matches.get(mailBox.get(name).get(i));
									if(tmp==null){
										tmp= new ArrayList<>();
									}
									tmp.add(name);
									if(matches.containsKey(mailBox.get(name).get(i)))
										matches.remove(mailBox.get(name).get(i));
									matches.put(mailBox.get(name).get(i), tmp);
									cout.println("You Successfully Match With Client");
									mailBox.get(name).remove(i);
									break;
								} else {
									cout.println("You Reject "+mailBox.get(name).get(i)+" Request");
									mailBox.get(name).remove(i);
									break;

								}

							}
						} else {
							cout.println("NO");
						}
						break;
					case "Send":
						DatagramPacket recievedpck=null;

						if(matches.get(name).size()>0){
							byteArray = new ByteArrayOutputStream();
							recievedpck = new DatagramPacket(receiveData, receiveData.length);
							int numOfPck = Integer.parseInt(cin.readLine());
							for (int j = 0; j < numOfPck; j++) {
								datagramSocket.receive(recievedpck);
								byteArray.write(recievedpck.getData(), 0,recievedpck.getLength());
							}
							for(String st : matches.get(name)){
								for(int i=0 ; i < clients.size() ; i++){
									if (st.equals(clients.get(i).name)) {
										clients.get(i).coutR.println("Get");
										audioBox2.put(clients.get(i).name, byteArray);
										byte send[] = byteArray.toByteArray(); // ={12,16];
										clients.get(i).coutR.println(send.length / 10000);
										System.out.println(send.length / 10000);
										for (int j = 0; j < send.length / 10000; j++) {
											byte[] tmp = new byte[10000];
											for (int p = 0; p < 10000; p++) {
												tmp[p] = send[j * 10000 + p];
											}
											DatagramPacket sendpck = new DatagramPacket(tmp, tmp.length);
											sendpck.setAddress(InetAddress.getByName("127.0.0.1"));
											sendpck.setPort(clients.get(i).port);
											datagramSocket.send(sendpck);
										}
									}
								}
							}
						}
/*
						for (int i = 0; i < clients.size(); i++) {

							if (matches.get(name).equals(clients.get(i).name)) {
								recievedpck = new DatagramPacket(receiveData, receiveData.length);
								audioBox2.put(clients.get(i).name, new ByteArrayOutputStream());
								int numOfPck = Integer.parseInt(cin.readLine());
								for (int j = 0; j < numOfPck; j++) {
									datagramSocket.receive(recievedpck);
									audioBox2.get(clients.get(i).name).write(recievedpck.getData(), 0,recievedpck.getLength());
								}
								clients.get(i).coutR.println("Get");
								byte send[] = audioBox2.get(clients.get(i).name).toByteArray(); // ={12,16];
								clients.get(i).coutR.println(send.length / 10000);
								System.out.println(send.length / 10000);
								for (int j = 0; j < send.length / 10000; j++) {
									byte[] tmp = new byte[10000];
									for (int p = 0; p < 10000; p++) {
										tmp[p] = send[j * 10000 + p];
									}
									DatagramPacket sendpck = new DatagramPacket(tmp, tmp.length);
									sendpck.setAddress(InetAddress.getByName("127.0.0.1"));
									sendpck.setPort(clients.get(i).port);
									datagramSocket.send(sendpck);
								}
							}
						}*/
						break;
					}
				} catch (IOException e) {
					try {
						cin.close();
						cinR.close();
						coutR.close();
						client.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					cout.close();
					clients.remove(this);
					clientsSocket.remove(this.client);
					exit = true;
					exit=true;
				}
			}
		}
	}

}