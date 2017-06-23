package com.mycompany.huneiko;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * класс посылает запрос с переданным ему в конструкторе паролем
 * на сервер и проверяет ответ
 * @author Слава
 *
 */
public class RequestThread implements Runnable {

	public static final String URL = "http://www.rollshop.co.il/test.php";
    public static final String PARAM_CONTENT_TYPE = "Content-Type";
    public static final String VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String REQUEST_METHOD = "POST";
    public static final String REQUEST_PARAM = "code=";
	
    private String password;
    private PasswordGenerator generator;
    
    public RequestThread(String password, PasswordGenerator generator) {
    	this.password = password;
    	this.generator = generator;
    }
    
	@Override
	public void run() {
        try {
            URL url = new URL(null, URL, new sun.net.www.protocol.https.Handler());
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            con.setRequestMethod(REQUEST_METHOD);
            con.setRequestProperty(PARAM_CONTENT_TYPE, VALUE_CONTENT_TYPE);
            con.setDoOutput(true);
            
            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())){
            	out.writeBytes(REQUEST_PARAM + password);
            	out.flush();
            }

            int responseCode = con.getResponseCode();

            if(responseCode != 200) {
            	throw new IOException("ERROR while trying pass " + password
                		+ "\nResponse code " + responseCode);
            }
            StringBuilder response = new StringBuilder();
            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String row;
                while (null != (row = in.readLine())) {
                    response.append(row);
                }
            }
            String respStr = response.toString().toUpperCase();
            //System.out.println("Password: " + password + "\nResponse: " + respStr);
            if(!respStr.contains("WRONG =(")) {
            	System.out.println("Correct password: " + password + "\nResponse: " + respStr);
            	generator.terminateGeneration();
            }
        } catch (IOException e) {
        	System.out.println(e.getMessage());
        	generator.addRequestForRepeat(this);
        }
	}

}
