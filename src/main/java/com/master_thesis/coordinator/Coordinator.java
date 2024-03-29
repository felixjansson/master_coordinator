package com.master_thesis.coordinator;

import com.fasterxml.jackson.databind.JsonNode;
import com.master_thesis.coordinator.data.Client;
import com.master_thesis.coordinator.data.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@PropertySource(value = "classpath:rsa.properties")
public class Coordinator {

    private Lock serverLock;
    protected List<Server> servers;
    protected Map<Integer, List<Client>> clients;
    private BigInteger generator = BigInteger.valueOf(307);
    //    private BigInteger fieldBase = BigInteger.valueOf(991);
    private Map<Integer, BigInteger> fieldBases;
    //    private BigInteger fieldBase = BigInteger.ONE.shiftLeft(107).subtract(BigInteger.ONE);
    protected int tSecurity = 2;
    private Map<Integer, Integer> fids;
    private Random random;

    @Value("${FIELD_BASE_BITS}")
    private int fieldBaseBits;

    public Coordinator() {
        serverLock = new ReentrantLock();
        servers = new LinkedList<>();
        clients = new HashMap<>();
        fids = new HashMap<>();
        fieldBases = new HashMap<>();
        random = new SecureRandom();
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
    List<Server> getServerList() {
        return servers;
    }

    @GetMapping(value = "/server/list/ids")
    List<Integer> getServerIDList() {
        return servers.stream().map(Server::getServerID).collect(Collectors.toList());
    }

    @GetMapping(value = "/server/list/uri")
    List<URI> getServerURI() {
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

    @PostMapping(value = "/client/fid")
    void updateFid(@RequestBody JsonNode body) {
        // TODO: 3/26/2020 This function does probably not work as we want
        int substationID = body.get("substationID").asInt();
        int clientFid = body.get("fid").asInt();
        int fid = fids.get(substationID);
        int largestFid = Math.max(fid, clientFid);
        fids.put(substationID, largestFid);
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
        fieldBases.put(substationID, new BigInteger(fieldBase));
    }

    @PostMapping(value = "/setup/fieldBaseBits/{substationID}/{fieldBase}")
    void setFieldBaseBits(@PathVariable int substationID, @PathVariable int fieldBaseBits) {
        this.fieldBaseBits = fieldBaseBits;
        fieldBases.put(substationID, new BigInteger(fieldBaseBits, 16, random));
    }

    @GetMapping(value = "/setup/fieldBase/{substationID}")
    BigInteger getFieldBase(@PathVariable int substationID) {
        fieldBases.putIfAbsent(substationID, new BigInteger(fieldBaseBits, 16, random));
        return fieldBases.get(substationID);
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

    protected List<Integer> getClientListForSubstationID( int substationID) {
        return clients.getOrDefault(substationID, List.of()).stream()
                .map(Client::getClientID).collect(Collectors.toList());
    }

}
