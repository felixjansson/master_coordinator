package com.master_thesis.coordinator.data;

import java.net.URI;

public class Server {

    private URI uri;

    private int serverID;

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
