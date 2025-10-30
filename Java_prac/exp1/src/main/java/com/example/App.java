package com.example;

import com.example.controller.SystemController;

/**
 * Students Score Management System
 * A simple Java application to manage and track student scores in terminal.
 * Using MVC architecture.
 *
 * @author morethan987
 */
public class App {

    public static void main(String[] args) {
        SystemController systemController = new SystemController();
        systemController.run();
    }
}
