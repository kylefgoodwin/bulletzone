package edu.unh.cs.cs619.bulletzone.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
    private static final String TAG = "FileHelper";
    private Context context;

    public FileHelper(Context context) {
        this.context = context;
    }

    // Save ArrayList of ReplayData objects to a file
    public void saveReplayList(String filename, List<ReplayDataFlat> replayList) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(replayList);
            oos.close();
            fos.close();
            Log.d(TAG, "Successfully saved " + replayList.size() + " replays");
        } catch (IOException e) {
            Log.e(TAG, "Error saving replay list", e);
        }
    }

    // Load ArrayList of ReplayData objects from a file
    @SuppressWarnings("unchecked")
    public List<ReplayDataFlat> loadReplayList(String filename) {
        List<ReplayDataFlat> replayList = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            replayList = (List<ReplayDataFlat>) ois.readObject();
            ois.close();
            fis.close();
            Log.d(TAG, "Successfully loaded " + replayList.size() + " replays");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "No existing replay file found");
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading replay list", e);
        }
        return replayList;
    }

    // Add a single replay to existing file
    public void addReplay(String filename, ReplayDataFlat newReplay) {
        List<ReplayDataFlat> existingReplays = loadReplayList(filename);
        existingReplays.add(0, newReplay);
        saveReplayList(filename, existingReplays);
        Log.d(TAG, "Added new replay. Total replays: " + existingReplays.size());
    }

    // Check if replay file exists
    public boolean replayFileExists(String filename) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists();
    }

    // Delete replay file
    public boolean deleteReplayFile(String filename) {
        return context.deleteFile(filename);
    }
}
