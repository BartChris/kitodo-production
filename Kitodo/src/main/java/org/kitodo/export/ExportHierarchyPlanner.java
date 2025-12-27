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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kitodo.data.database.beans.Process;

public class ExportHierarchyPlanner {

    /**
     * Plans a bottom-up export sequence by collecting all ancestors of the given roots.
     * Sorts the deduplicated set by hierarchy depth in descending order to ensure leaf
     * nodes (children) are processed and finalized before their respective parents.
     *
     * @param roots the initial processes to be analyzed and expanded for export
     * @return a sorted list representing the dependency-safe execution order
     */
    public List<Process> plan(Collection<Process> roots) {
        Set<Process> all = new LinkedHashSet<>();
        for (Process p : roots) {
            all.add(p);
            all.addAll(getAllParents(p));
        }

        List<Process> ordered = new ArrayList<>(all);
        ordered.sort(Comparator.comparingInt(this::getHierarchyDepth).reversed());
        return ordered;
    }

    private Set<Process> getAllParents(Process process) {
        Set<Process> parents = new LinkedHashSet<>();
        Process current = process.getParent();
        while (current != null) {
            parents.add(current);
            current = current.getParent();
        }
        return parents;
    }

    private int getHierarchyDepth(Process process) {
        int depth = 0;
        Process current = process;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }
}

