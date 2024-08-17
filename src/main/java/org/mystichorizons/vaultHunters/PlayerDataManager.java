package org.mystichorizons.vaultHunters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> new PlayerData());
    }

    public static class PlayerData {
        private boolean bypassing = false;

        public boolean isBypassing() {
            return bypassing;
        }

        public void setBypassing(boolean bypassing) {
            this.bypassing = bypassing;
        }
    }
}
