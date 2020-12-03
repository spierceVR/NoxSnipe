package com.company;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class ChangeAttempt implements Runnable{
    private accountData dataNs;
    private int y;
    private String target;
    ChangeAttempt(accountData dataN, int y, String theTarget){
        this.dataNs = dataN;
        this.y = y;
        this.target = theTarget;
    }

    public void run(){
        try{
            URL nurl = new URL("https://api.mojang.com/user/profile/" + dataNs.uuid + "/name");
            URLConnection ncon = nurl.openConnection();
            HttpURLConnection nhttp = (HttpURLConnection) ncon;
            nhttp.setRequestMethod("POST");
            nhttp.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            nhttp.setRequestProperty("Authorization", "Bearer " + Main.tokens.get(y));
            nhttp.setDoOutput(true);
            byte[] outN = ("{\"name\": \"" + target + "\", \"password\":\"" + dataNs.password + "\"}").getBytes(StandardCharsets.UTF_8);
            int lengthN = outN.length;
            nhttp.setFixedLengthStreamingMode(lengthN);
            nhttp.connect();
            try (OutputStream osN = nhttp.getOutputStream()) {
                osN.write(outN);
            }

            StringBuilder builder = new StringBuilder();
            builder.append(nhttp.getResponseCode())
                    .append(" ")
                    .append(nhttp.getResponseMessage());
            if (!(nhttp.getResponseCode() == 204)) { System.out.println("Attempting:  " + builder + " || " + System.currentTimeMillis()); }
            else { System.out.println("SUCCESS: Name sniped! @" + System.currentTimeMillis() ); }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}