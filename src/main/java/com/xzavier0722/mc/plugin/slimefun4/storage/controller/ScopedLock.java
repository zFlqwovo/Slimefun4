package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.common.ScopeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class ScopedLock {
    private final Map<ScopeKey, ReentrantLock> locks;

    ScopedLock() {
        locks = new ConcurrentHashMap<>();
    }

    void createLock(ScopeKey scopeKey) {
        locks.put(scopeKey, new ReentrantLock());
    }

    void destroyLock(ScopeKey scopeKey) {
        var removed = locks.remove(scopeKey);
        if (removed != null) {
            removed.unlock();
        }
    }

    void lock(ScopeKey scopeKey) {
        var lock = locks.get(scopeKey);
        if (lock == null) {
            return;
        }

        lock.lock();
    }

    void unlock(ScopeKey scopeKey) {
        var lock = locks.get(scopeKey);
        if (lock == null) {
            return;
        }

        lock.unlock();
        locks.remove(scopeKey);
    }
}
