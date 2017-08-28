package com._17od.upm.database;

import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import com._17od.upm.crypto.CryptoException;
import com._17od.upm.crypto.EncryptionService;

public class JSONDatabaseSerializer {
	
	private static JSONObject data;
	private static JSONArray accounts;
	
	private JSONDatabaseSerializer(){
		
	}
	
	public static void clear(){
		data = new JSONObject();
		accounts = new JSONArray();
	}
	
	public static void addAccountInformation(AccountInformation ai){
		JSONObject accountInfo = 	new JSONObject();
		accountInfo.put("accountName", ai.getAccountName());
    	accountInfo.put("accountID", ai.getUserId());
    	try {
			EncryptionService encrypt = new EncryptionService(ai.getPassword().toCharArray());
			byte[] pBytes = ai.getPassword().getBytes(StandardCharsets.UTF_8);
			pBytes = encrypt.encrypt(pBytes);
			accountInfo.put("accountPassword", pBytes);
		} catch (CryptoException e) {
			e.printStackTrace();
		}
    	accountInfo.put("accountURL", ai.getUrl());
    	accountInfo.put("accountNotes", ai.getNotes());
    	accounts.put(accountInfo);
	}
	
	public static JSONObject compileJSON(String username, String password){
		data.put("username", username);
		data.put("password", password);
		data.put("userAccounts", accounts);
		return data;
	}
	
	
}
