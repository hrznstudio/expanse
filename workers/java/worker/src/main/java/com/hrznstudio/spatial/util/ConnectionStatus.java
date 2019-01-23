package com.hrznstudio.spatial.util;

public enum ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED;

    public boolean isConnected() {
        return this == CONNECTED;
    }

    public boolean isConnecting() {
        return this == CONNECTING;
    }

    public boolean isFailure() {
        return this == FAILED;
    }
}
