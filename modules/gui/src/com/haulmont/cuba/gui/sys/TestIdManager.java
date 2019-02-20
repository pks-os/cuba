/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.gui.sys;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TestIdManager {

    protected Map<String, Integer> ids = new HashMap<>();

    protected Pattern wrongCharacters = Pattern.compile("[^a-zA-Z\\d]");
    protected static final String PREFIX = "id_";

    public String getTestId(String baseId) {
        String id = normalize(baseId);

        Integer number = ids.get(id);
        if (number == null) {
            number = 0;
        } else {
            number++;
        }
        ids.put(id, number);

        // prevent conflicts
        while (ids.containsKey(id + number)) {
            number++;
        }

        if (number > 0) {
            id = id + number;
        }

        return id;
    }

    public String reserveId(String id) {
        if (!ids.containsKey(id)) {
            ids.put(id, 0);
        }

        return id;
    }

    public String normalize(String id) {
        if (id != null) {
            if (id.length() > 32) {
                id = id.substring(0, 32);
            }

            id = wrongCharacters.matcher(id).replaceAll("_");
            if (id.length() < 2) {
                return PREFIX + id;
            }

            return id;
        }
        return null;
    }

    public void reset() {
        ids.clear();
    }
}