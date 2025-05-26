package com.winappverifier.util;

import java.util.HashMap;
import java.util.Map;

public class AppMapper {
    private static final Map<String, String> swidToDisplayNameMap = new HashMap<>();

    static {
        // Mock mappings (replace with real data or DB lookup later)
        swidToDisplayNameMap.put("7zip.7zip_Igor Pavlov_24.09", "7-Zip");
        swidToDisplayNameMap.put("Notepadplusplus.Company_8.6.2", "Notepad++");
    }

    public static String getDisplayName(String swId) {
        String displayName = swidToDisplayNameMap.get(swId);
        if (displayName == null) {
            throw new RuntimeException("No display name found for sw_id: " + swId);
        }
        return displayName;
    }
}
