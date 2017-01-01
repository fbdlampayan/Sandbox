/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.sandbox;

import java.io.*;
import java.security.*;
import javax.net.ssl.*;
 
public class PlainClient {
    public static void main(String [] args) {
        SSLSocket socket = null;
        BufferedReader in = null;
        try {
            //load client private key
            KeyStore clientKeys = KeyStore.getInstance("JKS");
//            clientKeys.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\version2\\plainclient.jks"),"password".toCharArray());
            clientKeys.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\client\\clientKeyStore.jks"),"123456".toCharArray());
            KeyManagerFactory clientKeyManager = KeyManagerFactory.getInstance("SunX509");
//            clientKeyManager.init(clientKeys,"password".toCharArray());
            clientKeyManager.init(clientKeys,"123456".toCharArray());
 
            //load server public key
            KeyStore serverPub = KeyStore.getInstance("JKS");
//            serverPub.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\version2\\serverpub.jks"),"password".toCharArray());
            serverPub.load(new FileInputStream("C:\\Users\\FBDL\\Desktop\\keys\\client\\clientTrustStore.jks"),"123456".toCharArray());
            TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
            trustManager.init(serverPub);
 
            //use keys to create SSLSoket
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(clientKeyManager.getKeyManagers(), trustManager.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
            socket = (SSLSocket)ssl.getSocketFactory().createSocket("localhost", 8889);
            socket.startHandshake();
 
            System.out.println("Client session details");
            System.out.println("Protocol: " + socket.getSession().getProtocol());
            System.out.println("Cipher: " + socket.getSession().getCipherSuite());
            
            //receive data
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String data;
            while((data = in.readLine())!=null) {
                System.out.println(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(in!=null) 
                    in.close();
                if(socket!=null) 
                    socket.close();
                if(socket!=null) 
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}