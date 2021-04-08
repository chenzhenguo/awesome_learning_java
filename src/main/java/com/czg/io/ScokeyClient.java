package com.czg.io;

import java.io.*;
import java.net.Socket;

public class ScokeyClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1",9090);
            OutputStream outputStream = socket.getOutputStream();
            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while (true){
                outputStream.write(  reader.readLine().getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
