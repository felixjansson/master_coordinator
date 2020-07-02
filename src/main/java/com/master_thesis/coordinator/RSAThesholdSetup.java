package com.master_thesis.coordinator;

import com.master_thesis.coordinator.data.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping(value = "api/setup/rsa-data/")
@PropertySource(value = "classpath:rsa.properties")
public class RSAThesholdSetup {

    private static Random random;
    @Value("${RSA_PRIME_BIT_LENGTH}")
    private int RSA_PRIME_BIT_LENGTH;
    private Map<Integer, BigInteger[]> rsaPrimes;
    private Coordinator coordinator;


    @Autowired
    public RSAThesholdSetup(Coordinator coordinator) {
        this.coordinator = coordinator;
        random = new SecureRandom();
        rsaPrimes = new HashMap<>();

        new Thread(() -> {
            boolean running = true;
            while (running) {
                System.out.println("[field bits] = " + coordinator.getFieldBase(0) + ", [rsa bits] = " + RSA_PRIME_BIT_LENGTH);
                System.out.println(String.format("t security: %s\nServers: %s\nClients: %s\n", coordinator.tSecurity, coordinator.servers.toString(), coordinator.clients.toString()));
                System.out.println("Type the name of the variable to change it");
                Scanner input = new Scanner(System.in);
                switch (input.nextLine().replaceAll(" ", "").toLowerCase()) {
                    case "rsabits":
                    case "r":
                        System.out.print("new value: ");
                        RSA_PRIME_BIT_LENGTH = input.nextInt();
                        input.nextLine();
                        rsaPrimes.put(0, generateRSAPrimes(BigInteger.ZERO));
                        break;
                    case "f":
                    case "fieldbits":
                        System.out.print("new value: ");
                        coordinator.setFieldBaseBits(0, input.nextInt());
                        input.nextLine();
//                        fieldBases.put(0, new BigInteger(fieldBaseBits, 16, random));
//                        System.out.println("New fieldbase: " + fieldBases.get(0));
                        break;
                    case "servers":
                        removeServers(input);
                        break;
                    case "t":
                    case "tsecurity":
                        System.out.print("new value: ");
                        coordinator.tSecurity = input.nextInt();
                        input.nextLine();
                        break;
                    case "clients":
                        removeClients(input);
                        break;
                }
            }

        }).start();

    }

    private void removeClients(Scanner input) {
        System.out.println("Type an ID or \"ALL\" to remove clients: " + coordinator.clients);
        String ans = input.nextLine().toLowerCase();
        if ("all".equals(ans)) {
            coordinator.clients.clear();
        } else {
            try {
                List<Client> subClients = coordinator.clients.get(Integer.parseInt(ans));
                System.out.println("Type an ID or \"ALL\" to remove clients: " + subClients);
                ans = input.nextLine().toLowerCase();
                if ("all".equals(ans)) {
                    subClients.clear();
                } else {
                    subClients.remove(Integer.parseInt(ans));
                }
            } catch (Exception e) {
                System.out.println("Could not remove client " + ans + ". " + e.getMessage());
            }
        }
    }

    private void removeServers(Scanner input) {
        System.out.println("Type an ID or \"ALL\" to remove servers: " + coordinator.servers);
        String ans = input.nextLine().toLowerCase();
        if ("all".equals(ans)) {
            coordinator.servers.clear();
        } else {
            try {
                coordinator.servers.remove(Integer.parseInt(ans));
            } catch (Exception e) {
                System.out.println("Could not remove server " + ans + ". " + e.getMessage());
            }
        }
    }

    @GetMapping(value = "client/{substationID}")
    Object getRsaN(@PathVariable int substationID) {
        try {
            rsaPrimes.putIfAbsent(substationID, generateRSAPrimes(coordinator.getFieldBase(substationID)));
            return rsaPrimes.get(substationID);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }
    }


    public BigInteger[] generateRSAPrimes(BigInteger fieldBase) {
        // Todo: We believe that if the rsa primes are lower than fieldbase there could be an issue but we do not remember why at the moment.

        if (fieldBase.bitLength() > RSA_PRIME_BIT_LENGTH / 2)
            throw new RuntimeException("FieldBase bit length is higher than RSA primes. RSA must be two times larger.");
        if (RSA_PRIME_BIT_LENGTH < 12)
            throw new RuntimeException("There is probably not 2 safe primes of this bit size.");
        BigInteger[] pPair = generateConstrainedSafePrimePair(fieldBase, new BigInteger[]{});
        BigInteger[] qPair = generateConstrainedSafePrimePair(fieldBase, pPair);

        BigInteger rsaNPrime = pPair[0].multiply(qPair[0]);
        BigInteger rsaN = pPair[1].multiply(qPair[1]);
        return new BigInteger[]{rsaN, rsaNPrime};
    }

    private BigInteger[] generateConstrainedSafePrimePair(BigInteger minValue, BigInteger[] forbiddenValues) {
        BigInteger[] pair;
        boolean isSmallerThanMinValue, isForbidden;
        do {
            pair = generateSafePrimePair(minValue);
            isSmallerThanMinValue = pair[1].max(minValue).equals(minValue);
            isForbidden = Arrays.equals(pair, forbiddenValues);
        } while (isForbidden || isSmallerThanMinValue);
        return pair;
    }

    private BigInteger[] generateSafePrimePair(BigInteger minValue) {
        BigInteger p, q;
        do {
            p = new BigInteger(RSA_PRIME_BIT_LENGTH / 2, 16, random);
            q = p.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        } while (!q.isProbablePrime(16) || p.compareTo(minValue) < 1 || p.bitLength() > RSA_PRIME_BIT_LENGTH / 2);
        return new BigInteger[]{q, p};
    }


}
