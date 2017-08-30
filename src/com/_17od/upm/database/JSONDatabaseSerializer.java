package com._17od.upm.database;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
	
	public AccountInformation[] deserialize(JSONObject obj){
		JSONObject[] jArr = (JSONObject[])obj.get("userAccounts");
		AccountInformation[] ais = new AccountInformation[jArr.length];
		for(int x = 0; x < jArr.length; x++){
			ais[x].setAccountName(jArr[x].getString("accountName"));
			ais[x].setUserId(jArr[x].getString("accountID"));
			try {
				EncryptionService encrypt = new EncryptionService(jArr[x].getString("accountPassword").toCharArray());
				byte[] pBytes = jArr[x].getString("accountPassword").getBytes(StandardCharsets.UTF_8);
				byte[] pass = encrypt.decrypt(pBytes);
				ais[x].setPassword(Arrays.toString(pass));
			} catch (CryptoException e) {
				e.printStackTrace();
			}
			ais[x].setUrl(jArr[x].getString("accountURL"));
			ais[x].setNotes(jArr[x].getString("accountNotes"));
		}
		
		return ais;
	}
	
}
