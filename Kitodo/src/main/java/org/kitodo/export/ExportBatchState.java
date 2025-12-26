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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.kitodo.data.database.beans.Process;

public class ExportBatchState {

    // parentId -> number of unfinished relevant children
    private final Map<Integer, AtomicInteger> openChildren = new ConcurrentHashMap<>();

    // parents that just became ready
    private final Set<Integer> ready = ConcurrentHashMap.newKeySet();
    private final Set<Integer> started = ConcurrentHashMap.newKeySet();


    /** Called once during batch setup.
     *
     */
    public void register(Process p) {
        Process parent = p.getParent();
        if (Objects.nonNull(parent)) {
            openChildren
                    .computeIfAbsent(parent.getId(), k -> new AtomicInteger())
                    .incrementAndGet();
        }
    }

    public boolean markStarted(Process p) {
        return started.add(p.getId()); // true only the FIRST time
    }

    /** Called when a process finishes.
     *
     */
    public void finished(Process p) {
        Process parent = p.getParent();
        if (Objects.isNull(parent)) {
            return;
        }

        AtomicInteger counter = openChildren.get(parent.getId());
        if (Objects.nonNull(counter) && counter.decrementAndGet() == 0) {
            ready.add(parent.getId());
        }
    }

    /** Pure query.
     *
     */
    public boolean isReady(Process p) {
        return !openChildren.containsKey(p.getId())
                || ready.contains(p.getId());
    }
}
