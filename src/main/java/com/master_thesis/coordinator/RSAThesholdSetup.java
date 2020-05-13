package com.master_thesis.coordinator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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
                Scanner input = new Scanner(System.in);
                switch (input.nextLine().replaceAll(" ", "").toLowerCase()) {
                    case "rsabits":
                    case "r":
                        System.out.print("new value: ");
                        RSA_PRIME_BIT_LENGTH = input.nextInt();
                        rsaPrimes.put(0, generateRSAPrimes(BigInteger.ZERO));
                        break;
                    case "f":
                    case "fieldbits":
                        System.out.print("new value: ");
                        coordinator.setFieldBaseBits(0, input.nextInt());
//                        fieldBases.put(0, new BigInteger(fieldBaseBits, 16, random));
//                        System.out.println("New fieldbase: " + fieldBases.get(0));
                }
            }

        }).start();

    }

    @GetMapping(value = "client/{substationID}")
    BigInteger[] getRsaN(@PathVariable int substationID) {
        rsaPrimes.putIfAbsent(substationID, generateRSAPrimes(coordinator.getFieldBase(substationID)));
        return rsaPrimes.get(substationID);
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
