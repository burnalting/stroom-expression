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

package stroom.dashboard.expression.v1;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RoundSecond extends RoundDate {
    public static final String NAME = "roundSecond";
    private static final Calc CALC = new Calc();

    public RoundSecond(final String name) {
        super(name);
    }

    @Override
    protected RoundCalculator getCalculator() {
        return CALC;
    }

    public static class Calc extends RoundDateCalculator {
        private static final long serialVersionUID = -5893918049538006730L;

        @Override
        protected LocalDateTime adjust(final LocalDateTime dateTime) {
            LocalDateTime result = dateTime.truncatedTo(ChronoUnit.SECONDS);
            if (dateTime.isAfter(result.plusNanos(500000000))) {
                result = result.plusSeconds(1);
            }
            return result;
        }
    }
}
