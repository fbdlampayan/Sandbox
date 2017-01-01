/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.sandbox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * http://www.pixelstech.net/article/1445603357-A-HTTPS-client-and-HTTPS-server-demo-in-Java
 * @author FBDL
 */
public class HttpsServer {
    private int port = 9999;
    private boolean isServerDone = false;
    
    public static void main(String[] args) {
        System.out.println("Running server");
        
        HttpsServer server = new HttpsServer();
        server.run();
    }
    
    //default constructor
    HttpsServer(){
    }
    
    //constructor with port parameters
    HttpsServer(int port) {
        this.port = port;
    }
    
    //Creating and initializing the SSLContext
    private SSLContext createSSLContext() {
        try {
            //Loading this server's keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\server\\server.jks"), "123456".toCharArray());
            
            //Creating key manager... not sure sa X509
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, "123456".toCharArray());//
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            
            //Create trust manager... not sure sa X509
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
            
            //Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            //sslContext.init(km, tm, null);
            
            return sslContext;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //start to run the server
    public void run() {
        SSLContext sslContext = this.createSSLContext();
        
        try {
            //Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            
            //Create server socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(this.port);
            sslServerSocket.setNeedClientAuth(false);
            System.out.println("SSL server started");
            
            while(!isServerDone) {
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                
                //start the server thread
                new ServerThread(sslSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //Thread handling the socket from client
    static class ServerThread extends Thread {
        private SSLSocket sslSocket = null;
        
        ServerThread(SSLSocket sslSocket) {
            this.sslSocket = sslSocket;
        }
        
        public void run() {
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
            
            try {
                //start handshake
                sslSocket.startHandshake();
                
                //get session after the connection is established
                SSLSession sslSession = sslSocket.getSession();
                
                System.out.println("SSLSession: ");
                System.out.println("\tProtocol: " + sslSession.getProtocol());
                System.out.println("\tCipher suite: " + sslSession.getCipherSuite());
                
                //Start handling application content
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    System.out.println("Input: " + line);
                    
                    if(line.trim().isEmpty()){
                        break;
                    }
                }
                
                printWriter.print("HTTP/1.1 200\r\n");
                printWriter.flush();
                
                sslSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
