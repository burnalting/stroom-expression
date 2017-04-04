/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.dashboard.expression;

import java.util.HashMap;
import java.util.Map;

public class FieldIndexMap {
    private final Map<String, Integer> fieldToPos = new HashMap<>();
    private final boolean autoCreate;
    private int index;

    public FieldIndexMap() {
        this(false);
    }

    public FieldIndexMap(final boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public int create(final String fieldName) {
        return create(fieldName, false);
    }

    public int create(final String fieldName, final boolean forceCreation) {
        if (autoCreate || forceCreation) {
            return fieldToPos.computeIfAbsent(fieldName, k -> index++);
        }

        Integer currentIndex = fieldToPos.get(fieldName);
        if (currentIndex == null) {
            return -1;
        }
        return currentIndex;
    }

    public int get(final String fieldName) {
        final Integer currentIndex = fieldToPos.get(fieldName);
        if (currentIndex == null) {
            return -1;
        }
        return currentIndex;
    }

    public int size() {
        return fieldToPos.size();
    }
}
