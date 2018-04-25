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

public class LessThan extends AbstractManyChildFunction {
    public static final String NAME = "<";
    public static final String ALIAS = "lessThan";
    private final boolean usingOperator;

    public LessThan(final String name) {
        super(name, 2, 2);
        usingOperator = name.length() == 1;

    }

    @Override
    protected Generator createGenerator(final Generator[] childGenerators) {
        return new Gen(childGenerators);
    }

    @Override
    public void appendString(final StringBuilder sb) {
        if (usingOperator) {
            appendParams(sb);
        } else {
            super.appendString(sb);
        }
    }

    @Override
    protected void appendParams(final StringBuilder sb) {
        if (usingOperator) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    final Param param = params[i];
                    appendParam(sb, param);
                    if (i < params.length - 1) {
                        sb.append(name);
                    }
                }
            }
        } else {
            super.appendParams(sb);
        }
    }

    private static class Gen extends AbstractManyChildGenerator {
        private static final long serialVersionUID = 217968020285584214L;

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
            final Var a = childGenerators[0].eval();
            final Var b = childGenerators[1].eval();
            Var retVal = VarBoolean.FALSE;

            if (!a.hasValue() || !b.hasValue()) {
                retVal = VarNull.INSTANCE;
            } else {
                final Double da = a.toDouble();
                final Double db = b.toDouble();
                if (da == null || db == null) {
                    int ret = a.toString().compareTo(b.toString());
                    if (ret < 0) {
                        retVal = VarBoolean.TRUE;
                    }
                } else {
                    if (da < db) {
                        retVal = VarBoolean.TRUE;
                    }
                }
            }

            return retVal;
        }
    }
}
