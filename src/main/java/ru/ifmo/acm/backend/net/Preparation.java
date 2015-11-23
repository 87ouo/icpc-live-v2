package ru.ifmo.acm.backend.net;

import javax.net.ssl.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.PCMS.PCMSEventsLoader;
import ru.ifmo.acm.datapassing.DataLoader;

/**
 * Created by aksenov on 15.04.2015.
 */
public class Preparation {
    public static EventsLoader eventsLoader;

    public static void prepareEventsLoader() {
        eventsLoader = PCMSEventsLoader.getInstance();
    }

    public static DataLoader dataLoader;

    public static void prepareDataLoader() {
        dataLoader = new DataLoader();
    }

    public static void prepareNetwork(final String login, final String password) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }


        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        Authenticator.setDefault(
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

//        System.setProperty("javax.net.ssl.keyStore", "C:/work/icpc-live/resources/key.jks");
//        System.setProperty("javax.net.ssl.trustStore", "C:/work/icpc-live/resources/key.jks");
    }
}
