package com.master_thesis.coordinator;

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

    public void setUri(String uri) {
        this.uri = URI.create(uri);
    }
}
