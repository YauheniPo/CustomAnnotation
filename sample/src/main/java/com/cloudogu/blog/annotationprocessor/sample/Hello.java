package com.cloudogu.blog.annotationprocessor.sample;

import com.cloudogu.blog.annotationprocessor.log.Log;

public class Hello {

    @Log
    public static void main(String[] args) {
        System.out.println("main");
        print();
    }

//    @Log
    public static void print() {
        System.out.println("Hello");
        System.out.println(HelloAutogenerate.class.getName());
    }
}