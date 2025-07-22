package com.example;

/**
 * Simple holder for the current shard identifier using {@link ThreadLocal}.
 */
public final class ShardContext {
    private static final ThreadLocal<String> CURRENT_SHARD = new ThreadLocal<>();

    private ShardContext() {
        // utility class
    }

    public static void setShard(String shard) {
        CURRENT_SHARD.set(shard);
    }

    public static String getShard() {
        return CURRENT_SHARD.get();
    }

    public static void clear() {
        CURRENT_SHARD.remove();
    }
}
