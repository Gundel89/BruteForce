package com.mycompany.huneiko;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class RunApp {

	public static void main(String[] args) {
		PasswordGenerator pg = new PasswordGenerator();
		try {
			pg.attackTheSystem();
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			System.out.println(e.getMessage());
		}
	}

}
