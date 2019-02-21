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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestIdManager {

    protected Map<String, Integer> ids = new HashMap<>();

    protected static final Pattern PERMITTED_CHARACTERS = Pattern.compile("[a-zA-Z\\d_]");
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
        String normalizedId = normalize(id);
        if (!ids.containsKey(normalizedId)) {
            ids.put(normalizedId, 0);
        }

        return normalizedId;
    }

    public String normalize(String id) {
        if (id != null) {
            String normalizedId = id;
            if (id.length() > 32) {
                normalizedId = id.substring(0, 32);
            }

            Matcher matcher = PERMITTED_CHARACTERS.matcher(normalizedId);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < normalizedId.length(); i++) {
                if (matcher.find(i)) {
                    if (i != matcher.start()) {
                        sb.append("_");
                    } else {
                        sb.append(matcher.group());
                    }
                }
            }

            String result = sb.toString();
            if (result.length() < 2) {
                return PREFIX + result;
            }

            return result;
        }
        return null;
    }

    public void reset() {
        ids.clear();
    }
}