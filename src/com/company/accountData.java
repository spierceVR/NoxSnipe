package com.company;

public class accountData{
    String email;
    String password;
    String uuid;

    accountData(String email, String password, String uuid){
        this.email = email;
        this.password = password;
        this.uuid = uuid;
    }

    @Override
    public String toString() {

        return "email: " + email + " password: " + password + " uuid: " + uuid;
    }
}