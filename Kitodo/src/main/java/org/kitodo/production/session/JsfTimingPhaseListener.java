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

package org.kitodo.production.session;

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsfTimingPhaseListener implements PhaseListener {

    private static final Logger logger =
        LogManager.getLogger(JsfTimingPhaseListener.class);

    private static final String REQUEST_START =
        JsfTimingPhaseListener.class.getName() + ".requestStart";

    private static final String PHASE_START =
        JsfTimingPhaseListener.class.getName() + ".phaseStart";

    @Override
    public void beforePhase(PhaseEvent event) {

        Map<String, Object> requestMap = event.getFacesContext()
            .getExternalContext()
            .getRequestMap();

        long now = System.nanoTime();

        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            requestMap.put(REQUEST_START, now);

            logger.info(
                "[JSF-TIMING] ===== REQUEST START ===== view={}",
                event.getFacesContext().getViewRoot() != null
                    ? event.getFacesContext().getViewRoot().getViewId()
                    : "<not restored yet>");
        }

        requestMap.put(PHASE_START, now);

        logger.info("[JSF-TIMING] START {}", event.getPhaseId());
    }

    @Override
    public void afterPhase(PhaseEvent event) {

        Map<String, Object> requestMap = event.getFacesContext()
            .getExternalContext()
            .getRequestMap();

        Long phaseStart = (Long) requestMap.get(PHASE_START);

        if (phaseStart != null) {
            double milliseconds =
                (System.nanoTime() - phaseStart) / 1_000_000.0;

            logger.info(
                "[JSF-TIMING] END   {} = {} ms",
                event.getPhaseId(),
                String.format("%.3f", milliseconds));
        }

        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            Long requestStart = (Long) requestMap.get(REQUEST_START);

            if (requestStart != null) {
                double milliseconds =
                    (System.nanoTime() - requestStart) / 1_000_000.0;

                logger.info(
                    "[JSF-TIMING] ===== REQUEST TOTAL = {} ms =====",
                    String.format("%.3f", milliseconds));
            }
        }
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
}