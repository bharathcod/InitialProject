package com.example.collab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LockServiceTest {

    private LockService lockService;

    @BeforeEach
    void setUp() {
        lockService = new LockService();
    }

    @Test
    void acquireAndReleaseLock() {
        boolean ok = lockService.acquireLock("M-x","F-1","alice", 60L, false);
        assertTrue(ok);
        var lock = lockService.getLock("M-x","F-1").orElseThrow();
        assertEquals("alice", lock.getOwner());

        boolean releaseOk = lockService.releaseLock("M-x","F-1","alice", false);
        assertTrue(releaseOk);
        assertTrue(lockService.getLock("M-x","F-1").isEmpty());
    }

    @Test
    void cannotAcquireWhenLockedByOther() {
        assertTrue(lockService.acquireLock("M-a","F-2","bob", 60L, false));
        boolean ok = lockService.acquireLock("M-a","F-2","alice", 60L, false);
        assertFalse(ok);
    }

    @Test
    void ttlExpires() throws InterruptedException {
        assertTrue(lockService.acquireLock("M-e","F-3","carol", 1L, false));
        Thread.sleep(1100);
        assertTrue(lockService.getLock("M-e","F-3").isEmpty());
    }
}