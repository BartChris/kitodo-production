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

package org.kitodo.api.externaldatamanagement;

import java.util.Collection;

import javax.swing.event.EventListenerList;

import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.exceptions.NoRecordFoundException;

/**
 * Manages the import of data from an external source.
 */
public interface ExternalDataImportInterface {

    EventListenerList externalDataListeners = new EventListenerList();

    /**
     * Get the full record with the given ID from the catalog.
     *
     * @param catalogId
     *            The ID of the catalog that will be queried.
     * @param identifier
     *            The ID of the record that will be imported.
     * @return The queried record as Record object.
     */
    DataRecord getFullRecordById(String catalogId, String identifier) throws NoRecordFoundException;

    /**
     * Perform search in catalog with given ID 'catalogId' with given search fields
     * 'field' and term 'term'. The parameter rows controls how many records should
     * be returned.
     *
     * @param catalogId
     *            ID of the catalog that will be queried.
     * @param field
     *            search field that will be queried
     * @param term
     *            value of search field that will be queried
     * @param rows
     *            number of records to be returned
     * @return Search result of performed query.
     */
    SearchResult search(String catalogId, String field, String term, int rows);

    /**
     * Perform search in catalog with given ID 'catalogId' with given search fields
     * 'field' and term 'term'. The parameter rows controls how many records should
     * be returned. The parameter 'start' controls the index of the first hit.
     *
     * @param catalogId
     *            ID of the catalog that will be queried.
     * @param field
     *            search field that will be queried
     * @param term
     *            value of search field that will be queried
     * @param rows
     *            number of records to be returned
     * @param start
     *            index of the first record to be returned
     * @return Search result of performed query.
     */
    SearchResult search(String catalogId, String field, String term, int start, int rows);

    /**
     * Searches for Data in a given source by term and field.
     *
     * @param ids
     *            The ids from the entries to get.
     * @param catalogId
     *            ID of the catalog that will be queried.
     * @return A list of result data.
     */
    Collection<SingleHit> getMultipleEntriesById(Collection<String> ids, String catalogId);
}
