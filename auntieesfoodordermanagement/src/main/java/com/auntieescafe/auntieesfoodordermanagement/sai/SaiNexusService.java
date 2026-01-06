package com.auntieescafe.auntieesfoodordermanagement.sai;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SAI Nexus is the heart of the Secure Application Interchange protocol.
 * It is responsible for initiating the "entanglement" between two services
 * by generating and distributing a unique, single-use cryptographic seed.
 */
@Service
public class SaiNexusService {

    private final SecureRandom secureRandom = new SecureRandom();

    // In a real microservices architecture, this would be a distributed cache like Redis.
    // This map stores the seed for a channel, keyed by a unique channel ID.
    private final Map<String, String> channelSeeds = new ConcurrentHashMap<>();

    /**
     * Creates a new secure channel between two services.
     *
     * @param serviceA_ID The identifier for the initiating service.
     * @param serviceB_ID The identifier for the target service.
     * @return The unique ID for the newly created channel.
     */
    public String createChannel(String serviceA_ID, String serviceB_ID) {
        // 1. Generate a high-entropy cryptographic seed.
        byte[] seed = new byte[64]; // 512 bits of entropy
        secureRandom.nextBytes(seed);
        String encodedSeed = Base64.getEncoder().encodeToString(seed);

        // 2. Create a unique identifier for this channel.
        String channelId = UUID.randomUUID().toString();

        // 3. Store the seed. In a real system, this would involve securely
        //    distributing the seed to the respective services. For now, we store it
        //    in a map, ready to be fetched.
        channelSeeds.put(channelId, encodedSeed);

        System.out.println(String.format(
            "SAI Nexus: Created channel %s for %s <-> %s", channelId, serviceA_ID, serviceB_ID
        ));

        return channelId;
    }

    /**
     * Retrieves the seed for a given channel and then consumes it.
     * This ensures the seed is single-use.
     *
     * @param channelId The ID of the channel.
     * @return The Base64 encoded seed.
     */
    public String getAndConsumeSeed(String channelId) {
        // The `remove` operation is atomic, ensuring the seed is retrieved only once.
        String seed = channelSeeds.remove(channelId);
        if (seed == null) {
            throw new IllegalStateException("SAI Channel not found or already consumed: " + channelId);
        }
        return seed;
    }
}
