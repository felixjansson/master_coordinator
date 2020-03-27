package com.master_thesis.coordinator;

import com.fasterxml.jackson.databind.JsonNode;
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
    Map<Integer, List<Client>> clients;
    private BigInteger generator = BigInteger.valueOf(307);
    private BigInteger fieldBase = BigInteger.valueOf(991);
    //    private BigInteger fieldBase = BigInteger.ONE.shiftLeft(107).subtract(BigInteger.ONE);
    private int tSecurity = 2;
    private Map<Integer, Integer> fids;

    public Coordinator() {
        serverLock = new ReentrantLock();
        servers = new LinkedList<>();
        clients = new HashMap<>();
        fids = new HashMap<>();
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
        int substationID = 0; //TODO : substation id set here!
        fids.putIfAbsent(substationID, 1);
        clients.putIfAbsent(substationID, new LinkedList<>());
        Client client = new Client(clients.get(substationID).size(), substationID, fids.get(substationID));
        clients.get(substationID).add(client);
        return client;
    }

    @GetMapping(value = "/client/list")
    Map<Integer, List<Client>> getClientList() {
        return clients;
    }

    @PostMapping(value = "/client/fid")
    void updateFid(@RequestBody JsonNode body) {
        // TODO: 3/26/2020 This function does probably not work as we want
        int substationID = body.get("substationID").asInt();
        int clientFid = body.get("fid").asInt();
        int fid = fids.get(substationID);
        int largestFid = Math.max(fid, clientFid);
        fids.put(substationID, largestFid);
    }

    @GetMapping(value = "/client/list/{substationID}")
    List<Integer> getClientListForSubstationID(@PathVariable int substationID) {
        return clients.get(substationID).stream()
                .map(Client::getClientID).collect(Collectors.toList());
    }

    @GetMapping(value = "/client/list/{substationID}/{fid}")
    List<Integer> getClientListForSubstationIDFid(@PathVariable int substationID, @PathVariable int fid) {
        return clients.get(substationID).stream()
                .filter(client -> client.getStartFid() <= fid)
                .map(Client::getClientID).collect(Collectors.toList());
    }

    @GetMapping(value = "/setup/generator/{substationID}")
    BigInteger getGenerator(@PathVariable int substationID) {
        return generator;
    }

    @PostMapping(value = "/setup/generator/{substationID}/{newG}")
    void setGenerator(@PathVariable int substationID, @PathVariable BigInteger newG) {
        generator = newG;
    }

    private void checkGenerator() {
        System.out.print("Order of " + generator + " in field " + fieldBase + ": ");
        for (BigInteger i = BigInteger.ONE; !i.equals(fieldBase); i = i.add(BigInteger.ONE)) {
            if (generator.modPow(i, fieldBase).equals(BigInteger.ONE)) {
                System.out.println(i);
                return;
            }
        }
    }

    @GetMapping(value = "/setup/t-security/{substationID}")
    int getTSecurity(@PathVariable int substationID) {
        return tSecurity;
    }

    @PostMapping(value = "/setup/setFid/{substationID}")
    void setFid(@PathVariable int substationID, @RequestBody int newFid) {
        fids.put(substationID, newFid);
    }

    @PostMapping(value = "/setup/t-security/{substationID}/{newT}")
    void setTSecurity(@PathVariable int substationID, @PathVariable int newT) {
        tSecurity = newT;
    }

    @PostMapping(value = "/setup/fieldBase/{substationID}/{fieldBase}")
    void setFieldBase(@PathVariable int substationID, @PathVariable String fieldBase) {
        this.fieldBase = new BigInteger(fieldBase);
    }

    @GetMapping(value = "/setup/fieldBase/{substationID}")
    BigInteger getFieldBase(@PathVariable int substationID) {
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
