package com.master_thesis.coordinator;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class Coordinator {

    List<Server> servers;
    List<Client> clients;

    public Coordinator() {
        servers = new LinkedList<>();
        clients = new LinkedList<>();
    }

    @PostMapping(value = "/server/register")
    int registerServer(@RequestBody Server uri) {
        servers.add(uri);
        return servers.size();
    }

    @GetMapping(value = "/server/list")
    List<Server> getServerList(){
        return servers;
    }

    @PostMapping(value = "/client/register")
    Client registerClient() {
        Client client = new Client(clients.size());
        clients.add(client);
        return client;
    }

    @GetMapping(value = "/client/amount")
    int getClientCount(){
        return clients.size();
    }

    @DeleteMapping
    void clearAll() {
        servers.clear();
        clients.clear();
    }

    @DeleteMapping(value = "/server")
    void clearServers() {
        servers.clear();
    }

    @DeleteMapping(value = "/client")
    void clearClients() {
        clients.clear();
    }

}
