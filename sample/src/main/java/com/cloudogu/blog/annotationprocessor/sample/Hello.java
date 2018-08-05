package com.cloudogu.blog.annotationprocessor.sample;

import com.cloudogu.blog.annotationprocessor.log.Log;

public class Hello {

    @Log
    public static void main(String[] args) {
        System.out.println(System.getProperty("idThread"));
        print();
    }

    @Log
    public static void print() {
        System.out.println("Hello");
    }
}