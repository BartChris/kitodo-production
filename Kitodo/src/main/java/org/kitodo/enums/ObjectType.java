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

package org.kitodo.enums;

import de.sub.goobi.helper.Helper;

public enum ObjectType {
    AUTHORITY("authorities"),
    CLIENT("clients"),
    BATCH("batches"),
    DOCKET("dockets"),
    PROCESS("processes"),
    PROJECT("projects"),
    PROPERTY("properties"),
    RULESET("rulesets"),
    TASK("tasks"),
    TEMPLATE("template"),
    USER("users"),
    USERGROUP("userGroups"),
    WORKFLOW("workflow"),
    FILTER("filter"),
    NONE("");

    private String messageKey;

    /**
     * Constructor setting the message key of the object type, used to retrieve it's
     * translation from the messages resource bundle.
     *
     * @param messageKey
     *            used for translating the object types name
     */
    ObjectType(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Retrieve and return the translation of the object type.
     *
     * @return translation of this object type
     */
    public String getTranslation() {
        return Helper.getTranslation(messageKey);
    }
}
