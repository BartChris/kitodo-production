/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.export;

import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ExportDirectoryGuard {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS =
            new ConcurrentHashMap<>();

    private ExportDirectoryGuard() {

    }

    /**
     * Acquires an exclusive, JVM-local lock for the given export directory.
     *
     * @param dir
     *         the export directory to lock
     * @return
     *         a locked {@link ReentrantLock} guarding the given directory
     */
    public static ReentrantLock lock(URI dir) {
        String key = Paths.get(dir).toAbsolutePath().normalize().toString();
        ReentrantLock lock = LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        return lock;
    }

    /**
     * Releases a previously acquired directory lock.
     *
     * @param dir
     *         the export directory associated with the lock
     * @param lock
     *         the lock instance previously returned by {@link #lock(URI)}
     */
    public static void unlock(URI dir, ReentrantLock lock) {
        try {
            lock.unlock();
        } finally {
            if (!lock.isLocked()) {
                LOCKS.remove(Paths.get(dir).toAbsolutePath().normalize().toString(), lock);
            }
        }
    }

}


