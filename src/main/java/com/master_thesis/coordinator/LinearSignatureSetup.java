package com.master_thesis.coordinator;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.master_thesis.coordinator.data.LinearClientData;
import com.master_thesis.coordinator.data.LinearPublicData;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api/linear-data")
public class LinearSignatureSetup {

    private final SecureRandom random = new SecureRandom();
    private static final Logger log = (Logger) LoggerFactory.getLogger(LinearSignatureSetup.class);
    private final int PRIME_BIT_LENGTH = 64;
    private final int PRIME_BIT_LENGTH_PRIME = 64;

    private Map<Integer, Substation<BigInteger>>  substationPrimes;
    private Map<Integer, Substation<BigInteger[]>>  substationHs;
    private Map<Integer, LinearClientData> substationData;
    private Coordinator coordinator;
    private LastClientTau lastClientTau;

    @Autowired
    public LinearSignatureSetup(Coordinator coordinator, LastClientTau lastClientTau) {
        this.coordinator = coordinator;
        substationPrimes = new HashMap<>();
        substationHs = new HashMap<>();
        substationData = new HashMap<>();
        this.lastClientTau = lastClientTau;
    }

    // TODO: 03/04/2020 Lock something
    @SneakyThrows
    @GetMapping(value = "/client/{substationID}/{fid}")
    LinearClientData getLinearClientData(@PathVariable int substationID, @PathVariable  int fid){
        int numberOfClients = coordinator.getClientListForSubstationID(substationID).size();
        substationData.putIfAbsent(substationID, substationSetup());
        LinearClientData linearClientData = substationData.get(substationID);


        substationHs.putIfAbsent(substationID, new Substation<>());
        Substation<BigInteger[]> substationH = substationHs.get(substationID);
        substationH.putIfAbsent(fid, generateHVector(numberOfClients, linearClientData.getNRoof()));
        BigInteger[] hVector = substationH.get(fid);


        substationPrimes.putIfAbsent(substationID, new Substation<>());
        Substation<BigInteger> substationPrime = substationPrimes.get(substationID);
        substationPrime.putIfAbsent(fid, generateUniquePrime(substationPrime.values(), linearClientData));
        BigInteger prime = substationPrime.get(fid);

        LinearClientData data = new LinearClientData(linearClientData, hVector, prime);
        log.debug("Returning [sid: {}, fid: {}] {}", substationID, fid, new ObjectMapper().writeValueAsString(data));
        return data;
    }

    @SneakyThrows
    @GetMapping(value = "/public/{substationID}/{fid}")
    LinearPublicData getLinearPublicData(@PathVariable int substationID, @PathVariable int fid){
        int numberOfClients = coordinator.getClientListForSubstationID(substationID).size();
        substationData.putIfAbsent(substationID, substationSetup());
        LinearClientData linearClientData = substationData.get(substationID);


        substationHs.putIfAbsent(substationID, new Substation<>());
        Substation<BigInteger[]> substationH = substationHs.get(substationID);
        substationH.putIfAbsent(fid, generateHVector(numberOfClients, linearClientData.getNRoof()));
        BigInteger[] hVector = substationH.get(fid);

        substationPrimes.putIfAbsent(substationID, new Substation<>());
        Substation<BigInteger> substationPrime = substationPrimes.get(substationID);
        substationPrime.putIfAbsent(fid, generateUniquePrime(substationPrime.values(), linearClientData));
        BigInteger prime = substationPrime.get(fid);

        LinearPublicData data = new LinearPublicData(linearClientData, hVector, prime);
        log.debug("Returning PublicData [sid: {}, fid: {}] {}", substationID, fid, new ObjectMapper().writeValueAsString(data));
        return data;
    }

    private BigInteger[] generateHVector(int numberOfClients, BigInteger nRoof) {
            BigInteger[] h = new BigInteger[numberOfClients];
            Arrays.fill(h, generateRandomBigInteger(nRoof));
            return h;

    }

    @GetMapping(value = "/rn/{substationID}/{fid}")
    BigInteger getRn(@PathVariable int substationID, @PathVariable int fid){
        BigInteger[] sk = substationData.get(substationID).getSk();
        return lastClientTau.getRn(substationID, fid, sk);
    }

    private BigInteger generateUniquePrime(Collection<BigInteger> usedPrimes, LinearClientData linearClientData) {
        BigInteger prime;
        BigInteger totientNRoof = linearClientData.getSk()[0].subtract(BigInteger.ONE).multiply(linearClientData.getSk()[1].subtract(BigInteger.ONE));
        // TODO: 03/04/2020 I will break when we run out of primes of given bit size :))
        do {
            prime = new BigInteger((PRIME_BIT_LENGTH / 2) - 1, 16, random);
        } while (usedPrimes.contains(prime) && !totientNRoof.gcd(prime).equals(BigInteger.ONE));
        return prime;
    }

    /**
     * This is the setup from the paper.
     */
    private LinearClientData substationSetup() {
        BigInteger[] pqRoof= generateSafePrimePair(PRIME_BIT_LENGTH);
        BigInteger NRoof = pqRoof[0].multiply(pqRoof[1]);
        BigInteger totientRoof = pqRoof[0].subtract(BigInteger.ONE).multiply(pqRoof[1].subtract(BigInteger.ONE));
        BigInteger[] pq;
        BigInteger N;
        int tries = 0;
        do {
            pq = generateSafePrimePair(PRIME_BIT_LENGTH_PRIME);
            N = pq[0].multiply(pq[1]);
            log.debug("Generating safe prime try: {}, totientRoof: {}, N: {}", ++tries, totientRoof, N);
        } while (!totientRoof.gcd(N).equals(BigInteger.ONE));
        return new LinearClientData(N, NRoof, generateRandomBigInteger(NRoof), generateRandomBigInteger(NRoof), pqRoof);
    }

    private BigInteger generateRandomBigInteger(BigInteger modulo){
        return new BigInteger(modulo.bitLength(), random).mod(modulo);
    }

    private BigInteger[] generateSafePrimePair(int bitLength) {
        BigInteger p, q;
        do {
            p = new BigInteger(bitLength/2, 16, random);
            q = p.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        } while (!q.isProbablePrime(16));
        return new BigInteger[]{q, p};
    }

    private static class Substation<T> extends HashMap<Integer, T>{}

}
