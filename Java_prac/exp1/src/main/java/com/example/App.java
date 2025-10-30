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
        String banner =
            "\n  ,ad8888ba,                                    88                ,ad8888ba,                                                     88\n" +
            " d8\"'    `\"8b                                   88               d8\"'    `\"8b                                                    88\n" +
            "d8'                                             88              d8'                                                              88\n" +
            "88             8b,dPPYba,  ,adPPYYba,   ,adPPYb,88   ,adPPYba,  88              ,adPPYba,   8b,dPPYba,   ,adPPYba,   ,adPPYba,   88   ,adPPYba,\n" +
            "88      88888  88P'   \"Y8  \"\"     `Y8  a8\"    `Y88  a8P_____88  88             a8\"     \"8a  88P'   `\"8a  I8[    \"\"  a8\"     \"8a  88  a8P_____88\n" +
            "Y8,        88  88          ,adPPPPP88  8b       88  8PP\"\"\"\"\"\"\"  Y8,            8b       d8  88       88   `\"Y8ba,   8b       d8  88  8PP\"\"\"\"\"\"\"\n" +
            " Y8a.    .a88  88          88,    ,88  \"8a,   ,d88  \"8b,   ,aa   Y8a.    .a8P  \"8a,   ,a8\"  88       88  aa    ]8I  \"8a,   ,a8\"  88  \"8b,   ,aa\n" +
            "  `\"Y88888P\"   88          `\"8bbdP\"Y8   `\"8bbdP\"Y8   `\"Ybbd8\"'    `\"Y8888Y\"'    `\"YbbdP\"'   88       88  `\"YbbdP\"'   `\"YbbdP\"'   88   `\"Ybbd8\"'\n";
        System.out.println(banner);

        SystemController systemController = new SystemController();
        systemController.run();
    }
}
