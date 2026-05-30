package com.serverutils.claims;

// Keeps ownership, trust, and rent status for one claim.

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Claim {
    private final UUID owner;
    private final Set<UUID> trusted;
    private long nextRentDueGameTime;
    private boolean unpaid;

    public Claim(UUID owner, long nextRentDueGameTime) {
        this(owner, new HashSet<>(), nextRentDueGameTime, false);
    }

    public Claim(UUID owner, Set<UUID> trusted, long nextRentDueGameTime, boolean unpaid) {
        this.owner = owner;
        this.trusted = trusted;
        this.nextRentDueGameTime = nextRentDueGameTime;
        this.unpaid = unpaid;
    }

    public UUID owner() {
        return owner;
    }

    public Set<UUID> trusted() {
        return trusted;
    }

    public long nextRentDueGameTime() {
        return nextRentDueGameTime;
    }

    public void setNextRentDueGameTime(long nextRentDueGameTime) {
        this.nextRentDueGameTime = nextRentDueGameTime;
    }

    public boolean unpaid() {
        return unpaid;
    }

    public void setUnpaid(boolean unpaid) {
        this.unpaid = unpaid;
    }

    public boolean canBuild(UUID playerId) {
        return owner.equals(playerId) || trusted.contains(playerId);
    }
}
