/*
 * Copyright 2018 Crown Copyright
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

import java.math.BigDecimal;
import java.util.Objects;

public class ValDouble implements ValNumber {
    private static final String TYPE = "double";
    private double value;

    ValDouble() {
    }

    ValDouble(final double value) {
        this.value = value;
    }

    public static ValDouble create(final double value) {
        return new ValDouble(value);
    }

    @Override
    public Integer toInteger() {
        return (int) value;
    }

    @Override
    public Long toLong() {
        return (long) value;
    }

    @Override
    public Double toDouble() {
        return value;
    }

    @Override
    public Boolean toBoolean() {
        return value != 0;
    }

    @Override
    public String toString() {
        final BigDecimal bigDecimal = BigDecimal.valueOf(value);
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    @Override
    public void appendString(final StringBuilder sb) {
        sb.append(toString());
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ValDouble valDouble = (ValDouble) o;
        return Double.compare(valDouble.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(final Val o) {
        return Double.compare(value, ((ValDouble) o).value);
    }
}
