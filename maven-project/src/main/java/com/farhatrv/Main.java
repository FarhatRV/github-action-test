package com.farhatrv;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        // sample
        String message = createMessage("New message");
        System.out.println(message);
    }

    public static String createMessage(String message){
        return "hello. " + message;
    }
}