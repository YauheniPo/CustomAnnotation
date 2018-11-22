package com.popovich.example;

import epam.popovich.annotation.time.TrackTime;

public class Hello {

    @TrackTime
    public static void main(String[] args) {
        System.out.println("main");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}