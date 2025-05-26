package com.example.myunyt;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.*;

public class FileHelper {
    private static final String FILE_NAME = "user_data.txt";

    public static void saveUserCredentials(Context context, String email, String password) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(email + "," + password + " \n"); // Removed spaces to ensure correct parsing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidUser(Context context, String email, String password) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("user_data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equals(email) && parts[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
