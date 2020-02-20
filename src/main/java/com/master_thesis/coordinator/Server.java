package com.master_thesis.coordinator;

import java.net.URI;

public class Server {

    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = URI.create(uri);
    }
}
