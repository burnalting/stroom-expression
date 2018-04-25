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

import java.util.Objects;

public class VarLong implements VarNumber {
    private final long value;

    public VarLong(final long value) {
        this.value = value;
    }

    @Override
    public Integer asInteger() {
        return (int) value;
    }

    @Override
    public Long asLong() {
        return value;
    }

    @Override
    public Double asDouble() {
        return (double) value;
    }

    @Override
    public Boolean asBoolean() {
        return value != 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final VarLong varLong = (VarLong) o;
        return value == varLong.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
