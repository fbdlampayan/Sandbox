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
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * read: http://blog.trifork.com/2009/11/10/securing-connections-with-tls/
 * @author FBDL
 */
public class HttpsClient {
    private String host = "127.0.0.1";
    private int port = 9999;
    
    public static void main(String[] args) {
        HttpsClient client = new HttpsClient();
        client.run();
    }

    HttpsClient() {
    }
    
    HttpsClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    private SSLContext createSSLContext() {
        try {
            //load this client's keystore that has the private key
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\client\\clientKeyStore.jks"), "123456".toCharArray());
            
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\client\\clientTrustStore.jks"), "123456".toCharArray());
            
            //create the key manager for this client's key
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, "123456".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            
            //create the trust manager for this client to trust
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
            
            //Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(km, tm, null);
            
            return sslContext;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void run() {
        SSLContext sslContext = this.createSSLContext();
        
        try {
            //create socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            
            //create socket
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(this.host, this.port);
            
            System.out.println("SSL client started");
            new ClientThread(sslSocket).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //thread handling the socket to server
    static class ClientThread extends Thread {
        private SSLSocket sslSocket = null;
        
        ClientThread(SSLSocket sslSocket) {
            this.sslSocket = sslSocket;
        }
        
        public void run() {
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
            
            try {
                //start the handshake
                sslSocket.startHandshake();
                
                //get session after the connection is established
                SSLSession sslSession = sslSocket.getSession();
                
                System.out.println("SSLSession: ");
                System.out.println("\tProtocol: " + sslSession.getProtocol());
                System.out.println("\tCipher Suite: " + sslSession.getCipherSuite());
                
                //start handling application content
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                
                //write data
                printWriter.println("Hello server from client");
                printWriter.println();
                printWriter.flush();
                
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    System.out.println("Input: " + line);
                    
                    if(line.trim().equals("HTTP/1.1 200\r\n")){
                        break;
                    }
                }
                
                sslSocket.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
