package com.master_thesis.coordinator;

import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    int registerServer(@RequestBody Server server) {
        int serverID = servers.size() + 1;
        server.setServerID(serverID);
        servers.add(server);
        return serverID;
    }

    @GetMapping(value = "/server/list")
    List<Server> getServerList(){
        return servers;
    }

    @GetMapping(value = "/server/list/ids")
    List<Integer> getServerIDList(){
        return servers.stream().map(Server::getServerID).collect(Collectors.toList());
    }

    @GetMapping(value = "/server/list/uri")
    List<URI> getServerURI(){
        return servers.stream().map(Server::getUri).collect(Collectors.toList());
    }

    @PostMapping(value = "/client/register")
    Client registerClient() {
        Client client = new Client(clients.size(), 0); //TODO : transformator id set here!
        clients.add(client);
        return client;
    }

    @GetMapping(value = "/client/list")
    Map<Integer, List<Client>> getClientList(){
        Map<Integer, List<Client>> json = new HashMap<>();
        clients.forEach(client -> {
            json.putIfAbsent(client.getTransformatorID(), new LinkedList<>());
            json.get(client.getTransformatorID()).add(client);
        });
        return json;
    }

    @GetMapping(value = "/client/list/{transformatorID}")
    List<Integer> getClientListForTransformatorID(@PathVariable int transformatorID){
        List<Integer> clientIDs = clients.stream()
                .filter(client -> client.isConnectedTo(transformatorID))
                .map(Client::getClientID)
                .collect(Collectors.toList());
        return clientIDs;
    }

    @GetMapping(value = "/setup/generator/{transformatorID}")
    int getGenerator(@PathVariable int transformatorID) {
        return 29;
    }


    @GetMapping(value = "/setup/fieldBase/{transformatorID}")
    int getFieldBase(@PathVariable int transformatorID) {
        return 991;
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
