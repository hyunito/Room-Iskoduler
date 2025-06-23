
package main;

import logic.LoginChecker;
import user.AdminHandler;
import user.FacultyHandler;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
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
}
