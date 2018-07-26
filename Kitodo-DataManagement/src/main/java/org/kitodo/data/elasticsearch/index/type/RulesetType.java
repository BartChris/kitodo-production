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

package org.kitodo.data.elasticsearch.index.type;

import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.elasticsearch.index.type.enums.RulesetTypeField;

/**
 * Implementation of Ruleset Type.
 */
public class RulesetType extends BaseType<Ruleset> {

    @Override
    JsonObject getJsonObject(Ruleset ruleset) {
        Integer clientId = Objects.nonNull(ruleset.getClient()) ? ruleset.getClient().getId() : 0;
        String clientName = Objects.nonNull(ruleset.getClient()) ? ruleset.getClient().getName() : "";

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(RulesetTypeField.TITLE.getKey(), preventNull(ruleset.getTitle()));
        jsonObjectBuilder.add(RulesetTypeField.FILE.getKey(), preventNull(ruleset.getFile()));
        jsonObjectBuilder.add(RulesetTypeField.ORDER_METADATA_BY_RULESET.getKey(), ruleset.isOrderMetadataByRuleset());
        jsonObjectBuilder.add(RulesetTypeField.FILE_CONTENT.getKey(), "");
        jsonObjectBuilder.add(RulesetTypeField.ACTIVE.getKey(), ruleset.isActive());
        jsonObjectBuilder.add(RulesetTypeField.CLIENT_ID.getKey(), clientId);
        jsonObjectBuilder.add(RulesetTypeField.CLIENT_NAME.getKey(), clientName);
        return jsonObjectBuilder.build();
    }
}
