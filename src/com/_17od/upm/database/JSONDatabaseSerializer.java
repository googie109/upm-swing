package com._17od.upm.database;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com._17od.upm.crypto.CryptoException;
import com._17od.upm.crypto.EncryptionService;
import com._17od.upm.util.Preferences;

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
		String pass = Preferences.password;
    	if(pass == null || pass == ""){
    		pass = Preferences.promptForPassword();
    	}
    	
		if(pass != null && pass != ""){
			JSONObject accountInfo = 	new JSONObject();
			accountInfo.put("accountName", ai.getAccountName());
	    	accountInfo.put("accountID", ai.getUserId());
	    	try {
	            char[] password = pass.toCharArray();
	            EncryptionService encryptionService = new EncryptionService(password);
	            byte[] accountPassword = encryptionService.encrypt(ai.getPassword().getBytes());
	            String encryptedPassword = Arrays.toString(accountPassword);
	            accountInfo.put("accountPassword", encryptedPassword);
			} catch (CryptoException e) {
				e.printStackTrace();
			}
	    	accountInfo.put("accountURL", ai.getUrl());
	    	accountInfo.put("accountNotes", ai.getNotes());
	    	accounts.put(accountInfo);
		}
	}
	
	public static JSONObject compileJSON(String username, String password){
		data.put("username", username);
		data.put("password", password);
		data.put("userAccounts", accounts);
		return data;
	}
	
	public static AccountInformation[] deserialize(JSONArray jArr){
		AccountInformation[] ais = new AccountInformation[jArr.length()];
		String password = Preferences.password;
    	if(password == null || password == ""){
    		password = Preferences.promptForPassword();
    	}
    	
		if(password != null && password != ""){
			for(int x = 0; x < jArr.length(); x++){
	            JSONObject obj = (JSONObject) jArr.get(x);
	            AccountInformation accountInfo = new AccountInformation();
				accountInfo.setAccountName(obj.optString("account", ""));
				accountInfo.setUserId(obj.optString("userId", ""));
				try {
	                EncryptionService encrypt = new EncryptionService(password.toCharArray());
	                String encryptedPassword = obj.optString("password", "");
	                byte[] pBytes = getActualBytes(encryptedPassword);
					byte[] pass = encrypt.decrypt(pBytes);
					accountInfo.setPassword(new String(pass, "UTF-8"));
				} catch (CryptoException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
	            accountInfo.setUrl(obj.optString("url", ""));
				accountInfo.setNotes(obj.optString("notes", ""));
	            ais[x] = accountInfo;
			}
		}
		
		return ais;
	}

    // parse out and get the real byte values!!
    public static byte[] getActualBytes(String foo) {
        String str = foo.substring(1, foo.lastIndexOf(']'));
        String[] values = str.split(",");

        byte[] actualBytes = new byte[values.length];
        for (int j = 0; j < values.length; ++j) {
            String s = values[j].replaceAll(" ", "");
            Integer i = Integer.parseInt(s);
            byte b = i.byteValue();
            actualBytes[j] = b;
        }
        return actualBytes;
    }

}
