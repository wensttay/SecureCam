package br.edu.ifpb.ads.mywebcamserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Wensttay de Sousa Alencar <yattsnew@gmail.com>
 * @date 11/05/2017, 04:34:34
 */
public class SocketK {
    public final static String CAM_SERVER_COMMAND_TAG = "CAM_SERVER_COMMAND_TAG";
    public final static String CAM_SERVER_COMMAND_RECORD_A_MINUT = "CAM_SERVER_COMMAND_RECORD_A_MINUT";
    public final static String CAM_SERVER_COMMAND_STREAM = "CAM_SERVER_COMMAND_STREAM";
    public final static String CAM_SERVER_COMMAND_FLASHLIGHT = "CAM_SERVER_COMMAND_FLASHLIGHT";
    public final static String CAM_SERVER_COMMAND_FAIL = "CAM_SERVER_COMMAND_FAIL";

    public static void main(String[] args) throws IOException {
        System.out.println("Starting a socketk ...");
        Socket socket = new Socket("localhost", 10990);

        System.out.println("Seding a mensage ...");
        OutputStream outputStream = socket.getOutputStream();

        String menssage = CAM_SERVER_COMMAND_TAG + ";"
                + "CAM F6J70T-00" + ";"
                + CAM_SERVER_COMMAND_RECORD_A_MINUT + ";";
        outputStream.write(menssage.getBytes());

        File file = new File(System.getProperty("user.dir") + File.separator + "eoq.mp4");
        
        if (file.exists()) {
            file.delete();
        }
        
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        InputStream inputS = socket.getInputStream();

        byte[] buffer = new byte[1024 * 100];    //If you handle larger data use a bigger buffer size
        int read = -1;
        long totalSize = 0;
        
        String code = "</Wensttay>";
        while (true) {
            read = inputS.read(buffer);
            totalSize += read;
            
            System.out.println("Enviando " + totalSize + " Bytes.");
            String data = new String(buffer).trim();
            
            if (!data.contains(code)) {
                fileOutputStream.write(buffer, 0, read);
            }else{
                fileOutputStream.write(buffer, 0, read - code.length());
                break;
            }
        }
        
        System.out.println("SAIU NO WHILE");
        fileOutputStream.close();
        System.out.println("FILE SIZE: " + totalSize);
    }

    public static String fileToBuffer(InputStream is, StringBuffer strBuffer) throws IOException {
        StringBuilder sb = new StringBuilder(strBuffer);
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(is))) {
            for (int c; (c = rdr.read()) != -1;) {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
}
