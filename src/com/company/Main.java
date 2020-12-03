package com.company;
/** NoxSnipe version 0.6 by Nox -- 12/3/2020
 **/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.*;

public class Main {
static String token;
static accountData data;
static List<String> tokens = new ArrayList<>();
static List<String> accounts = new ArrayList<>();
static String target;
static accountData dataNs;
static List<ChangeAttempt> tasks = new ArrayList<>();

    public static void main(String[] args) throws Exception{
        System.out.println("    _   __           _____       _                     ____   _____\n" +
                "   / | / /___  _  __/ ___/____  (_)___  ___     _   __/ __ \\ / ___/\n" +
                "  /  |/ / __ \\| |/_/\\__ \\/ __ \\/ / __ \\/ _ \\   | | / / / / // __ \\ \n" +
                " / /|  / /_/ />  < ___/ / / / / / /_/ /  __/   | |/ / /_/ // /_/ / \n" +
                "/_/ |_/\\____/_/|_|/____/_/ /_/_/ .___/\\___/    |___/\\____(_)____/  \n" +
                "                              /_/                                  ");

        System.out.print("Target name: ");
        Scanner input = new Scanner(System.in);
        target = input.nextLine();

        long dropTime = getDropTime(target);
        System.out.println("Will attempt to authenticate accounts in: " + (dropTime - System.currentTimeMillis() - 35000));
        dropTime = getDropTime(target);
        if (dropTime - System.currentTimeMillis() - 35000 > 0){
            Thread.sleep(dropTime- System.currentTimeMillis() - 35000);

            auth();

            for (int y = 0; y < Main.accounts.size(); y++) {
                String email = Main.accounts.get(y);
                String array2[] = email.split(" ");
                dataNs = new accountData(array2[0], array2[1], array2[2]);

                for (int x = 0; x < 20; x++){
                    tasks.add(new ChangeAttempt(dataNs, y, target));
                }
            }

            ExecutorService executorService = Executors.newFixedThreadPool(accounts.size()*20);
            dropTime = getDropTime(target);
            System.out.println("Will attempt to snipe \"" + target + "\" in: " + (dropTime - System.currentTimeMillis() - 80));
            dropTime = getDropTime(target);
            Thread.sleep(dropTime- System.currentTimeMillis() - 80);

            for (int z = 0; z < accounts.size()*20; z++) {
                executorService.execute(tasks.get(z));
                Thread.sleep(40);
            }
            executorService.shutdown();
        }
        else {System.out.println("Name unavailable");}

    }

    static void printResponse(String description, HttpURLConnection this_http) throws IOException{
        StringBuilder builder = new StringBuilder();
        builder.append(this_http.getResponseCode())
                .append(" ")
                .append(this_http.getResponseMessage());
        System.out.println(description + " " + builder + " || " +System.currentTimeMillis() );
    }

    static long getDropTime(String targetName) throws IOException {
        String uuid;

        /** make GET to /profiles **/
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + targetName + "?at=" + ((System.currentTimeMillis() - 3196800000L)/1000));
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("GET");
        http.connect();
        printResponse("Name2uuid API response = ", http);

        /** parse /auth response and extract bearer token **/
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder outTwo = new StringBuilder();
        Reader in = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8);
        int charsRead;
        while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
            outTwo.append(buffer, 0, charsRead);
        }
        JSONObject jsonBody = new JSONObject(outTwo.toString());
        uuid = (String)jsonBody.get("id");

        /** use uuid in payload of this GET for the timestamp **/
        URL turl = new URL("https://api.mojang.com/user/profiles/" + uuid + "/names");
        URLConnection tcon = turl.openConnection();
        HttpURLConnection thttp = (HttpURLConnection)tcon;
        thttp.setRequestMethod("GET");
        thttp.connect();

        /**  parse json form /auth response and extract bearer token**/
        final int tbufferSize = 1024;
        final char[] tbuffer = new char[tbufferSize];
        final StringBuilder out = new StringBuilder();
        Reader tin = new InputStreamReader(thttp.getInputStream(), StandardCharsets.UTF_8);
        int tcharsRead;
        while((tcharsRead = tin.read(tbuffer, 0, tbuffer.length)) > 0) {
            out.append(tbuffer, 0, tcharsRead);
        }
        JSONArray jsonArrayT = new JSONArray(out.toString());
        JSONObject mostRecentName = jsonArrayT.getJSONObject(jsonArrayT.length() - 1);
        return mostRecentName.getLong("changedToAt") + 3196800000L;
    }

    public static void auth() throws IOException{

        /**Read accounts file into ArrayList **/
        InputStream is = Main.class.getResourceAsStream("/resources/accounts.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            accounts.add(line);
        }
        reader.close();

        System.out.println("Logging in to: " + accounts.size() + " accounts");

        /** POST to /authenticate, GET to /challenges, and then POST to /validate **/
        for(int x = 0; x < accounts.size(); x++) {
            String email = accounts.get(x);
            String array1[] = email.split(" ");

            data = new accountData(array1[0], array1[1], array1[2]);

            URL url = new URL("https://authserver.mojang.com/authenticate");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            byte[] out = ("{\r\n    \"agent\": {\r\n        \"name\": \"Minecraft\",\r\n        \"version\": 1\r\n    },\r\n    \"username\": \""+ data.email+"\",\r\n    \"password\": \"" +data.password+"\",\r\n    \"requestUser\": true\r\n}" ).getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();

            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            printResponse("Authenticate Response = ", http);

            /**  parse json from /auth response and extract bearer token**/
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder outTwo = new StringBuilder();
            Reader in = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8);
            int charsRead;
            while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                outTwo.append(buffer, 0, charsRead);
            }
            JSONObject jsonBody = new JSONObject(outTwo.toString());
            token = (String)jsonBody.get("accessToken");

            /** /challenges GET **/
            URL curl = new URL("https://api.mojang.com/user/security/challenges");
            URLConnection ccon = curl.openConnection();
            HttpURLConnection chttp = (HttpURLConnection)ccon;
            chttp.setRequestMethod("GET");
            chttp.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            chttp.setRequestProperty("Authorization", "Bearer " + token);
            chttp.connect();
            printResponse("Challenge Response = ", chttp);

            /** /validate POST **/
            URL vurl = new URL("https://authserver.mojang.com/validate");
            URLConnection vcon = vurl.openConnection();
            HttpURLConnection vhttp = (HttpURLConnection)vcon;
            vhttp.setRequestMethod("POST");
            vhttp.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            vhttp.setRequestProperty("Authorization", "Bearer " + token);
            vhttp.setDoOutput(true);
            byte[] outV = ("{\"accessToken\": \"" + token + "\"}").getBytes(StandardCharsets.UTF_8);
            int lengthV = outV.length;
            vhttp.setFixedLengthStreamingMode(lengthV);
            vhttp.connect();
            try(OutputStream osV = vhttp.getOutputStream()) {
                osV.write(outV);
            }
            printResponse("Validate Response = ", vhttp);

            tokens.add(token);
        }
    }
}