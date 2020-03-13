package com.master_thesis.coordinator;

import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class Coordinator {

    private Lock serverLock;
    List<Server> servers;
    List<Client> clients;
    private int generator = 307;
//    private final BigInteger fieldBase = BigInteger.valueOf(991);
    private final BigInteger fieldBase = BigInteger.ONE.shiftLeft(107).subtract(BigInteger.ONE);
    private int tSecurity = 2;

    public Coordinator() {
        serverLock = new ReentrantLock();
        servers = new LinkedList<>();
        clients = new LinkedList<>();
//        checkGenerator();
    }

    @PostMapping(value = "/server/register")
    int registerServer(@RequestBody Server server) throws InterruptedException {
        boolean isLockAcquired = serverLock.tryLock(1, TimeUnit.SECONDS);
        if (isLockAcquired) {
            try {
                int serverID = servers.size() + 1;
                server.setServerID(serverID);
                servers.add(server);
                return serverID;
            } finally {
                serverLock.unlock();
            }
        }
        throw new RuntimeException("Could not register server. Lock issues?");
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
        return generator;
    }

    @PostMapping(value = "/setup/generator/{transformatorID}/{newG}")
    void setGenerator(@PathVariable int transformatorID, @PathVariable int newG) {
        generator = newG;
    }

    private void checkGenerator() {
        BigInteger g = BigInteger.valueOf(generator);
        System.out.print("Order of " + generator + " in field " + fieldBase + ": ");
        for (BigInteger i = BigInteger.ONE; !i.equals(fieldBase); i = i.add(BigInteger.ONE)) {
            if (g.modPow(i, fieldBase).equals(BigInteger.ONE)) {
                System.out.println(i);
                return;
            }
        }
    }

    @GetMapping(value = "/setup/t-security/{transformatorID}")
    int getTSecurity(@PathVariable int transformatorID) {
        return tSecurity;
    }

    @PostMapping(value = "/setup/t-security/{transformatorID}/{newT}")
    void setTSecurity(@PathVariable int transformatorID, @PathVariable int newT) {
        tSecurity = newT;
    }

    @PostMapping(value = "/setup/fieldBase/{transformatorID}/{fieldBase}")
    void setFieldBase(@PathVariable int transformatorID, @PathVariable String fieldBase) {
        this.fieldBase = new BigInteger(fieldBase);
    }

    @GetMapping(value = "/setup/fieldBase/{transformatorID}")
    BigInteger getFieldBase(@PathVariable int transformatorID) {
        return fieldBase;
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
