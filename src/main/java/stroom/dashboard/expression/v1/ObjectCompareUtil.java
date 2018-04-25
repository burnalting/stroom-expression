/*
 * Copyright 2016 Crown Copyright
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
 */

package stroom.dashboard.expression.v1;

public final class ObjectCompareUtil {
    private ObjectCompareUtil() {
        // Utility class
    }

    public static int compare(final Var o1, final Var o2) {
        if (o1 != null && o2 != null) {
            final Double d1 = o1.asDouble();
            final Double d2 = o2.asDouble();

            if (d1 != null && d2 != null) {
                return d1.compareTo(d2);
            }

            final String str1 = o1.toString();
            final String str2 = o2.toString();
            if (str1 != null && str2 != null) {
                return str1.compareToIgnoreCase(str2);
            }
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return 0;
    }
}
