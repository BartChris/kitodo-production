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

package org.kitodo.production.forms.dataeditor;

import static org.kitodo.constants.StringConstants.MEDIA_ID;
import static org.kitodo.constants.StringConstants.MEDIA_VIEW;
import static org.kitodo.constants.StringConstants.PREVIEW;
import static org.kitodo.constants.StringConstants.PROCESS;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.helper.Helper;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Session scoped media provider bean.
 */
@SessionScoped
@Named
public class MediaProvider implements Serializable {

    private static final Logger logger = LogManager.getLogger(MediaProvider.class);

    private final Map<Integer, Map<String, GalleryMediaContent>> mediaResolver = new HashMap<>();
    private static final ThreadLocal<Long> hasPreviewVariantTotalNanos =
        ThreadLocal.withInitial(() -> 0L);

    private static final ThreadLocal<Integer> hasPreviewVariantCalls =
        ThreadLocal.withInitial(() -> 0);

    private static final ThreadLocal<Long> hasMediaViewVariantTotalNanos =
        ThreadLocal.withInitial(() -> 0L);

    private static final ThreadLocal<Integer> hasMediaViewVariantCalls =
        ThreadLocal.withInitial(() -> 0);

    /**
     * Get the media resolver.
     *
     * @return value of media resolver
     */
    public Map<String, GalleryMediaContent> getMediaResolver(int processId) {
        if (!mediaResolver.containsKey(processId)) {
            mediaResolver.put(processId, new HashMap<>());

        }
        return mediaResolver.get(processId);
    }

    /**
     * Add media content to the media resolver.
     */
    public void addMediaContent(int processId, GalleryMediaContent galleryMediaContent) {
        if (galleryMediaContent.isShowingInPreview() || galleryMediaContent.isShowingInMediaView()) {
            getMediaResolver(processId).put(galleryMediaContent.getId(), galleryMediaContent);
        }
    }

    /**
     * Reset media resolver for process with provided ID 'processId'
     * by removing the corresponding map.
     *
     * @param processId process ID
     */
    public void resetMediaResolverForProcess(int processId) {
        mediaResolver.remove(processId);
    }

    private static final ThreadLocal<Long> previewDataTotalNanos =
        ThreadLocal.withInitial(() -> 0L);

    private static final ThreadLocal<Integer> previewDataCalls =
        ThreadLocal.withInitial(() -> 0);

    /**
     * Returns the media content of the preview media.
     *
     * @return preview of media content as PrimeFaces StreamedContent
     */
    public StreamedContent getPreviewData() {
        long start = System.nanoTime();

        try {
            Map<String, String> parameterMap = FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();

            return getMediaContent(
                PREVIEW,
                parameterMap.get(PROCESS),
                parameterMap.get(MEDIA_ID));

        } finally {
            long elapsed = System.nanoTime() - start;

            long total = previewDataTotalNanos.get() + elapsed;
            int calls = previewDataCalls.get() + 1;

            previewDataTotalNanos.set(total);
            previewDataCalls.set(calls);

            if (calls == 792) {
                logger.info(
                    "[MEDIA-TIMING] getPreviewData TOTAL = {} ms, calls = {}, avg = {} ms",
                    String.format("%.3f", total / 1_000_000.0),
                    calls,
                    String.format("%.3f",
                        total / 1_000_000.0 / calls));

                previewDataTotalNanos.remove();
                previewDataCalls.remove();
            }
        }
    }

    /**
     * Returns the media content of the media view.
     *
     * @return media view of media content as PrimeFaces StreamedContent
     */
    public StreamedContent getMediaView() {
        Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return getMediaContent(MEDIA_VIEW, parameterMap.get(PROCESS), parameterMap.get(MEDIA_ID));
    }

    /**
     * Returns if media content has preview variant.
     *
     * @param galleryMediaContent
     *         The gallery media content object
     * @return True if media content has preview variant
     */
    public boolean hasPreviewVariant(GalleryMediaContent galleryMediaContent) {
        long start = System.nanoTime();

        try {
            return Objects.nonNull(galleryMediaContent)
                && galleryMediaContent.isShowingInPreview();

        } finally {
            long elapsed = System.nanoTime() - start;

            long total = hasPreviewVariantTotalNanos.get() + elapsed;
            int calls = hasPreviewVariantCalls.get() + 1;

            hasPreviewVariantTotalNanos.set(total);
            hasPreviewVariantCalls.set(calls);

            /*
             * For the current test object we have 792 unstructured media.
             * Log after 792 evaluations.
             */
            if (calls == 792) {
                logger.info(
                    "[MEDIA-TIMING] hasPreviewVariant TOTAL = {} ms, calls = {}, avg = {} ms",
                    String.format("%.3f", total / 1_000_000.0),
                    calls,
                    String.format(
                        "%.6f",
                        total / 1_000_000.0 / calls));

                hasPreviewVariantTotalNanos.remove();
                hasPreviewVariantCalls.remove();
            }
        }
    }

    /**
     * Returns if media content has media view variant.
     *
     * @param galleryMediaContent
     *         The gallery media content object
     * @return True if media content has media view variant
     */
    public boolean hasMediaViewVariant(GalleryMediaContent galleryMediaContent) {
        long start = System.nanoTime();

        try {
            return Objects.nonNull(galleryMediaContent)
                && galleryMediaContent.isShowingInMediaView();

        } finally {
            long elapsed = System.nanoTime() - start;

            long total = hasMediaViewVariantTotalNanos.get() + elapsed;
            int calls = hasMediaViewVariantCalls.get() + 1;

            hasMediaViewVariantTotalNanos.set(total);
            hasMediaViewVariantCalls.set(calls);

            if (calls == 792) {
                logger.info(
                    "[MEDIA-TIMING] hasMediaViewVariant TOTAL = {} ms, calls = {}, avg = {} ms",
                    String.format("%.3f", total / 1_000_000.0),
                    calls,
                    String.format(
                        "%.6f",
                        total / 1_000_000.0 / calls));

                hasMediaViewVariantTotalNanos.remove();
                hasMediaViewVariantCalls.remove();
            }
        }
    }

    private StreamedContent getMediaContent(String mediaVariant, String processIdString, String mediaIdString) {
        if (Objects.nonNull(processIdString) && Objects.nonNull(mediaIdString)) {
            try {
                int processId = Integer.parseInt(processIdString);
                if (mediaResolver.containsKey(processId)) {
                    Map<String, GalleryMediaContent> processPreviewData = mediaResolver.get(processId);
                    GalleryMediaContent mediaContent = processPreviewData.get(mediaIdString);
                    if (Objects.nonNull(mediaContent)) {
                        logger.trace("Serving image request '{}'", StringEscapeUtils.escapeJava(mediaIdString));
                        if (PREVIEW.equals(mediaVariant)) {
                            return mediaContent.getPreviewData();
                        }
                        if (MEDIA_VIEW.equals(mediaVariant)) {
                            return mediaContent.getMediaViewData();
                        }
                        logger.error("Error: Unknown media variant '{}'", StringEscapeUtils.escapeJava(mediaVariant));
                    }
                    logger.debug("Cannot serve image request, mediaId = '{}'", StringEscapeUtils.escapeJava(mediaIdString));
                }
                logger.debug("Media resolver does not contain media content for process with ID {}", processId);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage("Process ID '" + StringEscapeUtils.escapeJava(processIdString) + "' is not numeric!");
            }
        }
        return DefaultStreamedContent.builder().stream(() -> InputStream.nullInputStream()).build();
    }
}
