package br.edu.ifpb.ads.mywebcamserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wensttay de Sousa Alencar <yattsnew@gmail.com>
 * @date 11/05/2017, 03:23:50
 */
public class RegisterSocket {

    private static List<Cam> cams = new ArrayList<>();

    public final static String CAM_SERVER_REQUEST_CODE = "CAM_SERVER_REQUEST_CODE";
    public final static String CAM_SERVER_FAIL_RESPONSE = "CAM_SERVER_FAIL_RESPONSE";
    public final static String CAM_SERVER_ALREADY_REGISTERED_RESPONSE = "CAM_SERVER_ALREADY_REGISTERED_RESPONSE";
    public final static String CAM_SERVER_PING_ASK = "OK?";
    public final static String CAM_SERVER_PING_ANSWER = "OK";

    public final static String CAM_SERVER_COMMAND_TAG = "CAM_SERVER_COMMAND_TAG";
    public final static String CAM_SERVER_COMMAND_RECORD_A_MINUT = "CAM_SERVER_COMMAND_RECORD_A_MINUT";
    public final static String CAM_SERVER_COMMAND_STREAM = "CAM_SERVER_COMMAND_STREAM";
    public final static String CAM_SERVER_COMMAND_FLASHLIGHT = "CAM_SERVER_COMMAND_FLASHLIGHT";
    public final static String CAM_SERVER_COMMAND_FAIL = "CAM_SERVER_COMMAND_FAIL";

    public static void main(String[] args) throws IOException {

        System.out.println("Starting server ...");
        System.setProperty("java.rmi.server.hostname", "192.168.1.101");
        ServerSocket serverSocket = new ServerSocket(10990);

        while (true) {
            System.out.println("Waiting a client ...");
            Socket webCamSocket = serverSocket.accept();
            OutputStream outputStream = webCamSocket.getOutputStream();
            InputStream inputStream = webCamSocket.getInputStream();

            byte[] b = new byte[1024];
            inputStream.read(b);
            String code = new String(b).trim();

            System.out.println("CODE RECEIVED: " + code);
            switch (code) {
                case CAM_SERVER_REQUEST_CODE:
                    System.out.println("REQUEST: Requesting a new CAM-CODE");
                    Cam cam = gerateCam(webCamSocket);
                    System.out.println("Registring new Cam:" + cam.getCode());
                    cams.add(cam);
                    outputStream.write(cam.getCode().getBytes());
                    outputStream.flush();
                    break;
                case CAM_SERVER_PING_ASK:
                    System.out.println("REQUEST: Testing a PING");
                    outputStream.write(CAM_SERVER_PING_ANSWER.getBytes());
                    outputStream.flush();
                    break;
                default:
                    if (code.contains(CAM_SERVER_COMMAND_TAG)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                controllCamCommand(code, webCamSocket);
                            }
                        }).start();
                        break;
                    } else {
                        System.out.println("REQUEST: Search for a persisted Cam");
                        Cam findAndReturn = findAndReturn(code);
                        if (findAndReturn != null) {
                            findAndReturn.setSocket(webCamSocket);
                            System.out.println("SOCCESS: A persisted Cam has finded");
                            outputStream.write(CAM_SERVER_ALREADY_REGISTERED_RESPONSE.getBytes());
                        } else {
                            System.out.println("FAIL: Not finded a peristed Cam");
                            outputStream.write(CAM_SERVER_FAIL_RESPONSE.getBytes());
                        }
                        outputStream.flush();
                    }
            }
        }
    }

    public static Cam findAndReturn(String code) {
        for (Cam c : cams) {
            if (c.getCode().equals(code)) {
                return c;
            }
        }
        return null;
    }

    private static Cam gerateCam(Socket socket) {
        while (true) {
            Cam aux = new Cam(gerateCamCode());
            if (!cams.contains(aux)) {
                aux.setSocket(socket);
                return aux;
            }
        }
    }

    private static String gerateCamCode() {
        SecureRandom random = new SecureRandom();
        String prefix = new RandomString(6).nextString().toUpperCase();
        String sufix = new BigInteger(6, random).toString();
        return "CAM " + prefix + "-" + (sufix.length() == 1 ? ("0" + sufix) : sufix);
    }

    private static void controllCamCommand(String code, Socket webCamSocket) {

        String[] codes = code.split(";");
        String camCode = codes[1];
        String command = codes[2];

        Cam camTo = findAndReturn(camCode);
        if (camTo != null) {
            try {
                System.out.println("Command: " + command + " | To: " + camTo.getCode());

                OutputStream camOutPut = camTo.getSocket().getOutputStream();
                camOutPut.write(command.getBytes());
                camOutPut.flush();

                InputStream inputStream = camTo.getSocket().getInputStream();
                OutputStream requestOutPut = webCamSocket.getOutputStream();

                byte[] buffer = new byte[1024 * 100];
                int read = -1;

                int totalSize = 0;
                
                String c = "</Wensttay>";
                
                while (true) {
                    read = inputStream.read(buffer);
                    totalSize += read;
                    System.out.println("Enviando " + read + " Bytes.");
                    String data = new String(buffer).trim();

                    if (!data.contains(c)) {
                        requestOutPut.write(buffer, 0, read);
                    } else {
                        requestOutPut.write(buffer, 0, read);
                        break;
                    }
                }
                System.out.println("FILE SIZE: " + totalSize);
                
            } catch (IOException ex) {
                Logger.getLogger(RegisterSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
