package com.hrznstudio.spatial.util;

public enum ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED;

    public boolean isConnected() {
        return this == CONNECTED;
    }

    public boolean isConnecting() {
        return this == CONNECTING;
    }
}
