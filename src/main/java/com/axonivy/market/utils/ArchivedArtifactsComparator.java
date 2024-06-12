package com.axonivy.market.utils;

import com.axonivy.market.github.model.ArchivedArtifact;

import java.util.Comparator;

public class ArchivedArtifactsComparator implements Comparator<ArchivedArtifact> {

    @Override
    public int compare(ArchivedArtifact v1, ArchivedArtifact v2) {
        // Split by "."
        String[] parts1 = v1.getLastVersion().split("\\.");
        String[] parts2 = v2.getLastVersion().split("\\.");

        // Compare up to the shorter length
        int length = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            try {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                // Return difference for numeric parts
                if (num1 != num2) {
                    return num2 - num1;
                }
                // Handle non-numeric parts (e.g., "m229")
            } catch (NumberFormatException e) {
                return parts1[i].compareTo(parts2[i]);
            }
        }

        // Versions with more parts are considered larger
        return parts2.length - parts1.length;
    }
}
