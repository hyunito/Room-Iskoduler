/*package com.roomiskoduler.backend.controller;

import com.roomiskoduler.backend.service.*;
import com.roomiskoduler.backend.dao.RequestInboxDAO;
//import com.roomiskoduler.backend.user.*;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {RequestInboxDAO.cleanExpiredBookings();}, 0, 1, TimeUnit.MINUTES);

        while(true){
            System.out.print("Enter username: ");
            String uname = scanner.nextLine();

            System.out.print("Enter password: ");
            String pword = scanner.nextLine();

            String[] loginResult = LoginChecker.loginWithRole(uname, pword);

            if (loginResult == null) {
                System.out.println("Invalid username or password.");
                continue;
            }

            String role = loginResult[0];
            int userId = Integer.parseInt(loginResult[1]);

            System.out.println("Login successful as " + role + ".");

            if (role.equals("faculty")) {
                FacultyHandler.handle(userId);
            } else if (role.equals("admin")) {
                AdminHandler.handle(userId);
            }
        }

    }
} */