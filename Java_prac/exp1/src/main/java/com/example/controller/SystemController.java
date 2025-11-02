package com.example.controller;

public class SystemController {

    private String _token;
    private final AuthController authController = new AuthController();

    public SystemController() {}

    /**
     * Runs the system controller.
     *
     * @param none
     * @return void
     */
    public void run() {
        // login
        this.login();
    }

    /**
     * Login process.
     * @param none
     * @return inner token
     */
    private void login() {
        this._token = authController.handleLogin();
    }
}
