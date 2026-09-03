package org.kitodo.production.session;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsfTimingPanel extends UIPanel {

    private static final Logger logger =
        LogManager.getLogger(JsfTimingPanel.class);

    private static final String AGGREGATE_PREFIX =
        JsfTimingPanel.class.getName() + ".aggregate.";

    private enum PropertyKeys {
        label,
        aggregate,
        expectedCount
    }

    public String getLabel() {
        return (String) getStateHelper().eval(
            PropertyKeys.label,
            null);
    }

    public void setLabel(String label) {
        getStateHelper().put(PropertyKeys.label, label);
    }

    public boolean isAggregate() {
        return (Boolean) getStateHelper().eval(
            PropertyKeys.aggregate,
            false);
    }

    public void setAggregate(boolean aggregate) {
        getStateHelper().put(PropertyKeys.aggregate, aggregate);
    }

    public int getExpectedCount() {
        return (Integer) getStateHelper().eval(
            PropertyKeys.expectedCount,
            0);
    }

    public void setExpectedCount(int expectedCount) {
        getStateHelper().put(PropertyKeys.expectedCount, expectedCount);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        long start = System.nanoTime();

        try {
            for (UIComponent child : getChildren()) {
                child.encodeAll(context);
            }
        } finally {
            long elapsed = System.nanoTime() - start;

            if (isAggregate()) {
                recordAggregate(context, elapsed);
            } else {
                double milliseconds = elapsed / 1_000_000.0;

                logger.info(
                    "[RENDER-TIMING] {} = {} ms",
                    getTimingLabel(context),
                    String.format("%.3f", milliseconds));
            }
        }
    }

    private void recordAggregate(FacesContext context, long elapsed) {
        String label = getTimingLabel(context);
        String key = AGGREGATE_PREFIX + label;

        AggregateTiming timing =
            (AggregateTiming) context.getExternalContext()
                .getRequestMap()
                .get(key);

        if (timing == null) {
            timing = new AggregateTiming();
            context.getExternalContext()
                .getRequestMap()
                .put(key, timing);
        }

        timing.totalNanos += elapsed;
        timing.count++;

        int expectedCount = getExpectedCount();

        if (expectedCount > 0 && timing.count == expectedCount) {
            logger.info(
                "[RENDER-TIMING] {} TOTAL = {} ms, calls = {}, avg = {} ms",
                label,
                String.format("%.3f",
                    timing.totalNanos / 1_000_000.0),
                timing.count,
                String.format("%.3f",
                    timing.totalNanos
                        / 1_000_000.0
                        / timing.count));
        }
    }

    private String getTimingLabel(FacesContext context) {
        String label = getLabel();

        if (label != null && !label.isBlank()) {
            return label;
        }

        return getClientId(context);
    }

    private static class AggregateTiming {
        private long totalNanos;
        private int count;
    }
}