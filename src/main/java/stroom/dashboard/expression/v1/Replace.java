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

import java.io.Serializable;
import java.text.ParseException;
import java.util.regex.Pattern;

public class Replace extends AbstractManyChildFunction implements Serializable {
    private static final long serialVersionUID = -305845496003936297L;

    public static final String NAME = "replace";

    private Generator gen;
    private boolean simple;

    public Replace(final String name) {
        super(name, 3, 3);
    }

    @Override
    public void setParams(final Param[] params) throws ParseException {
        super.setParams(params);

        // See if this is a static computation.
        simple = true;
        for (Param param : params) {
            if (!(param instanceof Var)) {
                simple = false;
                break;
            }
        }

        if (simple) {
            // Static computation.
            final String value = params[0].toString();
            final String regex = params[1].toString();
            final String replacement = params[2].toString();

            if (regex.length() == 0) {
                throw new ParseException("An empty regex has been defined for second argument of '" + name + "' function", 0);
            }

            final Pattern pattern = PatternCache.get(regex);
            final String newValue = pattern.matcher(value).replaceAll(replacement);
            gen = new StaticValueFunction(VarString.create(newValue)).createGenerator();

        } else {
            if (params[1] instanceof Var) {
                // Test regex is valid.
                final String regex = params[1].toString();
                if (regex.length() == 0) {
                    throw new ParseException("An empty regex has been defined for second argument of '" + name + "' function", 0);
                }
                PatternCache.get(regex);
            }
        }
    }

    @Override
    public Generator createGenerator() {
        if (gen != null) {
            return gen;
        }
        return super.createGenerator();
    }

    @Override
    protected Generator createGenerator(final Generator[] childGenerators) {
        return new Gen(childGenerators);
    }

    @Override
    public boolean hasAggregate() {
        if (simple) {
            return false;
        }
        return super.hasAggregate();
    }

    private static class Gen extends AbstractManyChildGenerator {
        private static final long serialVersionUID = 8153777070911899616L;

        public Gen(final Generator[] childGenerators) {
            super(childGenerators);
        }

        @Override
        public void set(final Var[] values) {
            for (final Generator generator : childGenerators) {
                generator.set(values);
            }
        }

        @Override
        public Var eval() {
            final Var val = childGenerators[0].eval();
            if (!val.hasValue()) {
                return val;
            }

            try {
                final String value = val.toString();
                final String regex = childGenerators[1].eval().toString();
                final String replacement = childGenerators[2].eval().toString();
                final Pattern pattern = PatternCache.get(regex);
                return VarString.create(pattern.matcher(value).replaceAll(replacement));

            } catch (final RuntimeException e) {
                return VarErr.create(e.getMessage());
            }
        }
    }
}
