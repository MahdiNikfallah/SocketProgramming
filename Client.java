import java.awt.Color;
import java.lang.Object;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 *
 * @author SHUBHAM
 */
@SuppressWarnings("serial")
public class Client extends JFrame {

	boolean stopaudioCapture = false;
	static ByteArrayOutputStream byteOutputStream;
	AudioFormat adFormat;
	TargetDataLine targetDataLine;
	AudioInputStream InputStream;
	SourceDataLine sourceLine;
	Graphics g;

	boolean allowGet=false;
	boolean allowSend=false;
	private static Scanner scanner;
	@SuppressWarnings("unused")
	private static String WTD;
	@SuppressWarnings("unused")
	private static BufferedReader sin, stdin,getR;
	@SuppressWarnings("unused")
	private static PrintStream sout,sendR;
	public static Socket sk,sk2;
	public static DatagramSocket udpSocket;
	public static int port;
	private static String s;
	private static String name;
	private static ObjectInputStream objectInputStream;
	final static JTextField serverMassage = new JTextField(25);

	public Client() {
		final JButton capture = new JButton("Capture");
		final JButton stop = new JButton("Stop");
		final JButton play = new JButton("Playback");
		final JButton connect = new JButton("Coonect To server");
		final JButton list = new JButton("List Of connected Client");
		final JButton req = new JButton("Request To Client");
		final JButton reqList = new JButton("Get Request List");
		final JTextField jtext = new JTextField(15);
		final JButton send = new JButton("Send");
		final JButton accept = new JButton("Accept");
		final JTextArea text = new JTextArea("Server Massages: ");
		final JTextArea text2 = new JTextArea("Your Massages: ");
		final JButton disconnect = new JButton("         Disconnect        ");
		final JButton sendpck = new JButton("     Send Audio     ");
		final JButton reject = new JButton("Reject");

		byteOutputStream = new ByteArrayOutputStream();
		capture.setEnabled(true);
		stop.setEnabled(false);
		play.setEnabled(true);

		capture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				capture.setEnabled(false);
				stop.setEnabled(true);
				play.setEnabled(false);
				captureAudio();
			}
		});

		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				capture.setEnabled(true);
				stop.setEnabled(false);
				play.setEnabled(true);
				stopaudioCapture = true;
				targetDataLine.close();
			}
		});

		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playAudio();
			}
		});

		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connectToServer();
			}
		});

		list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!(sk.isClosed())) {
					sout.println("List");
					Object x = new Object();
					try {
						x = objectInputStream.readObject();
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					serverMassage.setText(x.toString());
				} else {
					serverMassage.setText("Socket is Closed");
				}

			}
		});

		req.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!(sk.isClosed())) {
						sout.println("Request");
						serverMassage.setText(sin.readLine());
						allowSend=true;
					} else {
						serverMassage.setText("Server: Socket is closed");
					}
				} catch (Exception e2) {
					System.out.print("Error\n");
				}
			}
		});

		reqList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int size;
					sout.println("ReqList");
					if (sin.readLine().equals("YES")) {
						size = Integer.parseInt(sin.readLine());
						//serverMassage.setText(size + " Requests");
						//Thread.sleep(1000);
						for (int i = 0; i < size; i++) {
							s = sin.readLine();
							serverMassage.setText("You Have a Request from " + s + " Accept?");
							allowGet=true;
							break;
						}
					} else {
						serverMassage.setText("Request Boxis Empty");
					}
				} catch (Exception e3) {
					System.out.println("Error");
				}

			}
		});

		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(allowSend){
					try{
						s = jtext.getText();
						sout.println(s);

						serverMassage.setText(sin.readLine());
						allowSend=false;
					} catch(Exception e9){

					}
				}
			}
		});

		accept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(allowGet){
					try {
						//s = jtext.getText();
						sout.println("yes");
						/*s = sin.readLine();
						byteOutputStream = new ByteArrayOutputStream();
						for (int j = 0; j < Integer.parseInt(s); j++) {
							byte[] receiveData = new byte[100000];
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							udpSocket.receive(receivePacket);
							receiveData = receivePacket.getData();
							byteOutputStream.write(receivePacket.getData(), 0, receivePacket.getLength());
						}*/
						serverMassage.setText(sin.readLine());
						allowGet=false;
					} catch (Exception e4) {

					}
				}


			}
		});

		reject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(allowGet){
					try {

						sout.println("No");

						serverMassage.setText(sin.readLine());
						allowGet=false;
					} catch (Exception e4) {

					}
				}


			}
		});


		disconnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					if (!(sk.isClosed())) {
						sout.println("Disconnect");
						s = sin.readLine();
						serverMassage.setText("Server: " + s);
						sk.close();
					} else {
						serverMassage.setText("Server: Socket is closed");
					}
				} catch (Exception e6) {
					System.out.println("Error");
				}

			}
		});

		sendpck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					serverMassage.setText("Your Audio Sent");
					sout.println("Send");
					byte send[] = byteOutputStream.toByteArray();
					sout.println(send.length / 10000);
					for (int j = 0; j < send.length / 10000; j++) {
						byte[] tmp = new byte[10000];
						for (int p = 0; p < 10000; p++) {
							tmp[p] = send[j * 10000 + p];
						}
						DatagramPacket sendpck = new DatagramPacket(tmp, tmp.length, InetAddress.getByName("127.0.0.1"),4000);
						udpSocket.send(sendpck);
					}
				}catch(Exception e22){

				}
			}
		});

		getContentPane().add(capture);
		getContentPane().add(stop);
		getContentPane().add(play);
		getContentPane().add(connect);
		getContentPane().add(req);
		getContentPane().add(list);
		getContentPane().add(reqList);
		getContentPane().add(text);
		getContentPane().add(serverMassage);
		getContentPane().add(text2);
		getContentPane().add(jtext);
		getContentPane().add(send);
		getContentPane().add(accept);
		getContentPane().add(sendpck);
		getContentPane().add(disconnect);
		getContentPane().add(reject);

		getContentPane().setLayout(new FlowLayout());
		setTitle("Shareing Audio " + name);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 200);
		getContentPane().setBackground(Color.white);
		setVisible(true);
		g = (Graphics) this.getGraphics();
	}

	@SuppressWarnings("unused")
	public static void main(String args[]) throws Exception {

		initialize();
		System.out.print("Your name: ");
		name = scanner.nextLine();
		System.out.print("Your port: ");
		port = scanner.nextInt();
		Client client = new Client();
		client.allowGet=false;

		System.out.println("Welcome to audio sharing system\n-----------------------------------\nsend ? for help");

		Runnable get = new Runnable() {
			public void run() {
				while(true){
					try{
						//String s=getR.readLine();
						String s=getR.readLine();
						System.out.println(s);
						if(s.equals("Get")){
							//System.out.print("g");
							s = getR.readLine();
							byteOutputStream = new ByteArrayOutputStream();
							System.out.print(s);
							for (int j = 0; j < Integer.parseInt(s); j++) {
								byte[] receiveData = new byte[10000];
								DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
								udpSocket.receive(receivePacket);
								receiveData = receivePacket.getData();
								byteOutputStream.write(receiveData, 0, receiveData.length);
							}
							serverMassage.setText("You Get the Audio");
						}
					}catch(Exception e){

					}
				}
			}
		};

		Runnable waitForMessage = new Runnable() {
			public void run() {

			}
		};
		Thread t1 = new Thread(get);
		t1.start();
		Thread t2 = new Thread(waitForMessage);
;
	}

	private static void connectToServer() {
		try {
			sk = new Socket("127.0.0.1", 5000);
			sk2 = new Socket("127.0.0.1", 5000);
			udpSocket = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
			objectInputStream = new ObjectInputStream(sk.getInputStream());
			sin = new BufferedReader(new InputStreamReader(sk.getInputStream()));
			sout = new PrintStream(sk.getOutputStream());
			getR = new BufferedReader(new InputStreamReader(sk2.getInputStream()));
			sendR = new PrintStream(sk2.getOutputStream());
			stdin = new BufferedReader(new InputStreamReader(System.in));
			s = sin.readLine();
			sout.println(name);
			sout.println(port);
			serverMassage.setText(s);
		} catch (IOException e) {
			serverMassage.setText("Can't Connect to Server");
		}
	}

	private void captureAudio() {
		try {
			adFormat = getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(adFormat);
			targetDataLine.start();

			Thread captureThread = new Thread(new CaptureThread());
			captureThread.start();
		} catch (Exception e) {
			StackTraceElement stackEle[] = e.getStackTrace();
			for (StackTraceElement val : stackEle) {
				System.out.println(val);
			}
			System.exit(0);
		}
	}

	private static AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F;
		int sampleInbits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
	}

	class CaptureThread extends Thread {

		byte tempBuffer[] = new byte[10000];

		public void run() {
			byteOutputStream = new ByteArrayOutputStream();
			stopaudioCapture = false;
			try {
				while (!stopaudioCapture) {
					int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					if (cnt > 0) {
						byteOutputStream.write(tempBuffer, 0, cnt);
					}
				}
				byteOutputStream.close();
			} catch (Exception e) {
				System.out.println("CaptureThread::run()" + e);
				System.exit(0);
			}
		}
	}

	public void playAudio() {
		try {

			/*FileOutputStream file = new FileOutputStream("myfile.wav");
			byteOutputStream.writeTo(file);

			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("myfile.wav"));
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        clip.start();*/

			byte audioData[] = byteOutputStream.toByteArray();
			InputStream byteInputStream = new ByteArrayInputStream(audioData);
			AudioFormat adFormat = getAudioFormat();
			InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
			sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			sourceLine.open(adFormat);
			sourceLine.start();
			Thread playThread = new Thread(new PlayThread());
			playThread.start();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}

	class PlayThread extends Thread {

		byte tempBuffer[] = new byte[10000];

		public void run() {
			try {
				int cnt;
				while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
					if (cnt > 0) {
						sourceLine.write(tempBuffer, 0, cnt);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}
		}
	}

	private static void initialize() {
		scanner = new Scanner(System.in);
		WTD = null;
	}

}