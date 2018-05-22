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

import com.caucho.hessian.io.Hessian2Output;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExpressionParser {
    private final ExpressionParser parser = new ExpressionParser(new FunctionFactory(), new ParamFactory());

    @Test
    public void testBasic() throws ParseException {
        test("${val}");
        test("min(${val})");
        test("max(${val})");
        test("sum(${val})");
        test("min(round(${val}, 4))");
        test("min(roundDay(${val}))");
        test("min(roundMinute(${val}))");
        test("ceiling(${val})");
        test("floor(${val})");
        test("ceiling(floor(min(roundMinute(${val}))))");
        test("ceiling(floor(min(round(${val}))))");
        test("max(${val})-min(${val})");
        test("max(${val})/count()");
        test("round(${val})/(min(${val})+max(${val}))");
        test("concat('this is', 'it')");
        test("concat('it''s a string', 'with a quote')");
        test("'it''s a string'");
        test("stringLength('it''s a string')");
        test("upperCase('it''s a string')");
        test("lowerCase('it''s a string')");
        test("substring('Hello', 0, 1)");
        test("equals(${val}, ${val})");
        test("greaterThan(1, 0)");
        test("lessThan(1, 0)");
        test("greaterThanOrEqualTo(1, 0)");
        test("lessThanOrEqualTo(1, 0)");
        test("1=0");
        test("decode('fred', 'fr.+', 'freda', 'freddy')");
        test("extractHostFromUri('http://www.example.com:1234/this/is/a/path')");
    }

    private void test(final String expression) throws ParseException {
        final Expression exp = createExpression(expression);
        System.out.println(exp.toString());
    }

    @Test
    public void testMin1() throws ParseException {
        final Generator gen = createGenerator("min(${val})");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(180D, Offset.offset(0D));

        gen.set(getVal(500D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(180D, Offset.offset(0D));

        gen.set(getVal(600D));
        gen.set(getVal(13D));
        gen.set(getVal(99.3D));
        gen.set(getVal(87D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(13D, Offset.offset(0D));
    }

    private Val[] getVal(final String... str) {
        final Val[] result = new Val[str.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = ValString.create(str[i]);
        }
        return result;
    }

    private Val[] getVal(final double... d) {
        final Val[] result = new Val[d.length];
        for (int i = 0; i < d.length; i++) {
            result[i] = ValDouble.create(d[i]);
        }
        return result;
    }

    @Test
    public void testMinUngrouped2() throws ParseException {
        final Generator gen = createGenerator("min(${val}, 100, 30, 8)");

        gen.set(getVal(300D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testMinGrouped2() throws ParseException {
        final Generator gen = createGenerator("min(min(${val}), 100, 30, 8)");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testMin3() throws ParseException {
        final Generator gen = createGenerator("min(min(${val}), 100, 30, 8, count(), 55)");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testMax1() throws ParseException {
        final Generator gen = createGenerator("max(${val})");

        gen.set(getVal(300D));
        gen.set(getVal(180D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(300D, Offset.offset(0D));

        gen.set(getVal(500D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(500D, Offset.offset(0D));

        gen.set(getVal(600D));
        gen.set(getVal(13D));
        gen.set(getVal(99.3D));
        gen.set(getVal(87D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(600D, Offset.offset(0D));
    }

    @Test
    public void testMaxUngrouped2() throws ParseException {
        final Generator gen = createGenerator("max(${val}, 100, 30, 8)");

        gen.set(getVal(10D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(100D, Offset.offset(0D));
    }

    @Test
    public void testMaxGrouped2() throws ParseException {
        final Generator gen = createGenerator("max(max(${val}), 100, 30, 8)");

        gen.set(getVal(10D));
        gen.set(getVal(40D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(100D, Offset.offset(0D));
    }

    @Test
    public void testMax3() throws ParseException {
        final Generator gen = createGenerator("max(max(${val}), count())");

        gen.set(getVal(3D));
        gen.set(getVal(2D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testSum() throws ParseException {
        // This is a bad usage of functions as ${val} will produce the last set
        // value when we evaluate the sum. As we are effectively grouping and we
        // don't have any control over the order that cell values are inserted
        // we will end up with indeterminate behaviour.
        final Generator gen = createGenerator("sum(${val}, count())");

        gen.set(getVal(3D));
        gen.set(getVal(2D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(5D, Offset.offset(0D));
    }

    @Test
    public void testSumOfSum() throws ParseException {
        final Generator gen = createGenerator("sum(sum(${val}), count())");

        gen.set(getVal(3D));
        gen.set(getVal(2D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(7D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(11D, Offset.offset(0D));
    }

    @Test
    public void testAverageUngrouped() throws ParseException {
        // This is a bad usage of functions as ${val} will produce the last set
        // value when we evaluate the sum. As we are effectively grouping and we
        // don't have any control over the order that cell values are inserted
        // we will end up with indeterminate behaviour.
        final Generator gen = createGenerator("average(${val}, count())");

        gen.set(getVal(3D));
        gen.set(getVal(4D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(8D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(6D, Offset.offset(0D));
    }

    @Test
    public void testAverageGrouped() throws ParseException {
        final Generator gen = createGenerator("average(${val})");

        gen.set(getVal(3D));
        gen.set(getVal(4D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.5D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(8D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testMatch1() throws ParseException {
        final Generator gen = createGenerator("match('this', 'this')");

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isTrue();
    }

    @Test
    public void testMatch2() throws ParseException {
        final Generator gen = createGenerator("match('this', 'that')");

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isFalse();
    }

    @Test
    public void testMatch3() throws ParseException {
        final Generator gen = createGenerator("match(${val}, 'this')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isTrue();
    }

    @Test
    public void testMatch4() throws ParseException {
        final Generator gen = createGenerator("match(${val}, 'that')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isFalse();
    }

    @Test
    public void testTrue() throws ParseException {
        final Generator gen = createGenerator("true()");

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isTrue();
    }

    @Test
    public void testFalse() throws ParseException {
        final Generator gen = createGenerator("false()");

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isFalse();
    }

    @Test
    public void testNull() throws ParseException {
        final Generator gen = createGenerator("null()");

        final Val out = gen.eval();
        assertThat(out).isInstanceOf(ValNull.class);
    }

    @Test
    public void testErr() throws ParseException {
        final Generator gen = createGenerator("err()");

        final Val out = gen.eval();
        assertThat(out).isInstanceOf(ValErr.class);
    }

    @Test
    public void testNotTrue() throws ParseException {
        final Generator gen = createGenerator("not(true())");

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isFalse();
    }

    @Test
    public void testNotFalse() throws ParseException {
        final Generator gen = createGenerator("not(false())");

        final Val out = gen.eval();
        assertThat(out.toBoolean()).isTrue();
    }

    @Test
    public void testIf1() throws ParseException {
        final Generator gen = createGenerator("if(true(), 'this', 'that')");

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this");
    }

    @Test
    public void testIf2() throws ParseException {
        final Generator gen = createGenerator("if(false(), 'this', 'that')");

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("that");
    }

    @Test
    public void testIf3() throws ParseException {
        final Generator gen = createGenerator("if(${val}, 'this', 'that')");

        gen.set(getVal("true"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this");
    }

    @Test
    public void testIf4() throws ParseException {
        final Generator gen = createGenerator("if(${val}, 'this', 'that')");

        gen.set(getVal("false"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("that");
    }

    @Test
    public void testIf5() throws ParseException {
        final Generator gen = createGenerator("if(match(${val}, 'foo'), 'this', 'that')");

        gen.set(getVal("foo"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this");
    }

    @Test
    public void testIf6() throws ParseException {
        final Generator gen = createGenerator("if(match(${val}, 'foo'), 'this', 'that')");

        gen.set(getVal("bar"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("that");
    }

    @Test
    public void testNotIf() throws ParseException {
        final Generator gen = createGenerator("if(not(${val}), 'this', 'that')");

        gen.set(getVal("false"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this");
    }

    @Test
    public void testReplace1() throws ParseException {
        final Generator gen = createGenerator("replace('this', 'is', 'at')");

        gen.set(getVal(3D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("that");
    }

    @Test
    public void testReplace2() throws ParseException {
        final Generator gen = createGenerator("replace(${val}, 'is', 'at')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("that");
    }

    @Test
    public void testConcat1() throws ParseException {
        final Generator gen = createGenerator("concat('this', ' is ', 'it')");

        gen.set(getVal(3D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this is it");
    }

    @Test
    public void testConcat2() throws ParseException {
        final Generator gen = createGenerator("concat(${val}, ' is ', 'it')");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this is it");
    }

    @Test
    public void testStringLength1() throws ParseException {
        final Generator gen = createGenerator("stringLength(${val})");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testSubstring1() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 1, 2)");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("h");
    }

    @Test
    public void testSubstring3() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 2, 99)");

        gen.set(getVal("his"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("s");
    }

    @Test
    public void testSubstring4() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 1+1, 99-1)");

        gen.set(getVal("his"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("s");
    }

    @Test
    public void testSubstring5() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 2+5, 99-1)");

        gen.set(getVal("his"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    public void testSubstringBefore1() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, '-')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("aa");
    }

    @Test
    public void testSubstringBefore2() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, 'a')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    public void testSubstringBefore3() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, 'b')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("aa-");
    }

    @Test
    public void testSubstringBefore4() throws ParseException {
        final Generator gen = createGenerator("substringBefore(${val}, 'q')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    public void testSubstringAfter1() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, '-')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("bb");
    }

    @Test
    public void testSubstringAfter2() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, 'a')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("a-bb");
    }

    @Test
    public void testSubstringAfter3() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, 'b')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("b");
    }

    @Test
    public void testSubstringAfter4() throws ParseException {
        final Generator gen = createGenerator("substringAfter(${val}, 'q')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    public void testIndexOf() throws ParseException {
        final Generator gen = createGenerator("indexOf(${val}, '-')");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toInteger().intValue()).isEqualTo(2);
    }

    @Test
    public void testIndexOf1() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, '-'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("-bb");
    }

    @Test
    public void testIndexOf2() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, 'a'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("aa-bb");
    }

    @Test
    public void testIndexOf3() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, 'b'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("bb");
    }

    @Test
    public void testIndexOf4() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, indexOf(${val}, 'q'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    public void testLastIndexOf1() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, '-'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("-bb");
    }

    @Test
    public void testLastIndexOf2() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, 'a'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("a-bb");
    }

    @Test
    public void testLastIndexOf3() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, 'b'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("b");
    }

    @Test
    public void testLastIndexOf4() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, lastIndexOf(${val}, 'q'), stringLength(${val}))");

        gen.set(getVal("aa-bb"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEmpty();
    }

    @Test
    public void testDecode1() throws ParseException {
        final Generator gen = createGenerator("decode(${val}, 'hullo', 'hello', 'goodbye')");

        gen.set(getVal("hullo"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("hello");
    }

    @Test
    public void testDecode2() throws ParseException {
        final Generator gen = createGenerator("decode(${val}, 'h.+o', 'hello', 'goodbye')");

        gen.set(getVal("hullo"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("hello");
    }

    @Test
    public void testInclude1() throws ParseException {
        final Generator gen = createGenerator("include(${val}, 'this', 'that')");
        gen.set(getVal("this"));
        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this");
    }

    @Test
    public void testInclude2() throws ParseException {
        final Generator gen = createGenerator("include(${val}, 'this', 'that')");
        gen.set(getVal("that"));
        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("that");
    }

    @Test
    public void testInclude3() throws ParseException {
        final Generator gen = createGenerator("include(${val}, 'this', 'that')");
        gen.set(getVal("other"));
        final Val out = gen.eval();
        assertThat(out.toString()).isNull();
    }

    @Test
    public void testExclude1() throws ParseException {
        final Generator gen = createGenerator("exclude(${val}, 'this', 'that')");
        gen.set(getVal("this"));
        final Val out = gen.eval();
        assertThat(out.toString()).isNull();
    }

    @Test
    public void testExclude2() throws ParseException {
        final Generator gen = createGenerator("exclude(${val}, 'this', 'that')");
        gen.set(getVal("that"));
        final Val out = gen.eval();
        assertThat(out.toString()).isNull();
    }

    @Test
    public void testExclude3() throws ParseException {
        final Generator gen = createGenerator("exclude(${val}, 'this', 'that')");
        gen.set(getVal("other"));
        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("other");
    }

    @Test
    public void testEquals1() throws ParseException {
        final Generator gen = createGenerator("equals(${val}, 'plop')");

        gen.set(getVal("plop"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testEquals2() throws ParseException {
        final Generator gen = createGenerator("equals(${val}, ${val})");

        gen.set(getVal("plop"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testEquals3() throws ParseException {
        final Generator gen = createGenerator("equals(${val}, 'plip')");

        gen.set(getVal("plop"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testEquals4() throws ParseException {
        final Generator gen = createGenerator2("equals(${val1}, ${val2})");

        gen.set(getVal("plop", "plip"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testEquals5() throws ParseException {
        final Generator gen = createGenerator2("equals(${val1}, ${val2})");

        gen.set(getVal("plop", "plop"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testEquals6() throws ParseException {
        final Generator gen = createGenerator2("${val1}=${val2}");

        gen.set(getVal("plop", "plop"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThan1() throws ParseException {
        final Generator gen = createGenerator2("lessThan(1, 0)");

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testLessThan2() throws ParseException {
        final Generator gen = createGenerator2("lessThan(1, 1)");

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testLessThan3() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal(1D, 2D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThan4() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal("fred", "fred"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testLessThan5() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal("fred", "fred1"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThan6() throws ParseException {
        final Generator gen = createGenerator2("lessThan(${val1}, ${val2})");

        gen.set(getVal("fred1", "fred"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testLessThanOrEqualTo1() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(1, 0)");

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testLessThanOrEqualTo2() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(1, 1)");

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThanOrEqualTo3() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal(1D, 2D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThanOrEqualTo3_mk2() throws ParseException {
        final Generator gen = createGenerator2("(${val1}<=${val2})");

        gen.set(getVal(1D, 2D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThanOrEqualTo4() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal("fred", "fred"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThanOrEqualTo5() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal("fred", "fred1"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testLessThanOrEqualTo6() throws ParseException {
        final Generator gen = createGenerator2("lessThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal("fred1", "fred"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("false");
    }

    @Test
    public void testGreaterThanOrEqualTo1() throws ParseException {
        final Generator gen = createGenerator2("greaterThanOrEqualTo(${val1}, ${val2})");

        gen.set(getVal(2D, 1D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testGreaterThanOrEqualTo1_mk2() throws ParseException {
        final Generator gen = createGenerator2("(${val1}>=${val2})");

        gen.set(getVal(2D, 1D));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("true");
    }

    @Test
    public void testBooleanExpressions() throws ParseException {

        ValBoolean vTrue = ValBoolean.TRUE;
        ValBoolean vFals = ValBoolean.FALSE; // intentional typo to keep var name length consistent
        ValNull vNull = ValNull.INSTANCE;
        ValErr vEror = ValErr.create("Expecting an error"); // intentional typo to keep var name length consistent

        ValLong vLng0 = ValLong.create(0L);
        ValLong vLng1 = ValLong.create(1L);
        ValLong vLng2 = ValLong.create(2L);

        ValInteger vInt0 = ValInteger.create(0);
        ValInteger vInt1 = ValInteger.create(1);
        ValInteger vInt2 = ValInteger.create(2);

        ValDouble vDbl0 = ValDouble.create(0);
        ValDouble vDbl1 = ValDouble.create(1);
        ValDouble vDbl2 = ValDouble.create(2);

        ValString vStr1 = ValString.create("1");
        ValString vStr2 = ValString.create("2");
        ValString vStrA = ValString.create("AAA");
        ValString vStrB = ValString.create("BBB");
        ValString vStra = ValString.create("aaa");
        ValString vStrT = ValString.create("true");
        ValString vStrF = ValString.create("false");
        ValString vStr_ = ValString.EMPTY;

        // null/error, equals
        assertBooleanExpression(vNull, "=", vNull, vTrue);
        assertBooleanExpression(vNull, "=", vEror, vFals);
        assertBooleanExpression(vEror, "=", vEror, vTrue);

        // booleans, equals
        assertBooleanExpression(vTrue, "=", vTrue, vTrue);
        assertBooleanExpression(vFals, "=", vFals, vTrue);
        assertBooleanExpression(vTrue, "=", vFals, vFals);

        // longs, equals
        assertBooleanExpression(vLng1, "=", vNull, vFals);
        assertBooleanExpression(vNull, "=", vLng1, vFals);
        assertBooleanExpression(vLng1, "=", vLng1, vTrue);
        assertBooleanExpression(vLng1, "=", vLng2, vFals);
        assertBooleanExpression(vLng1, "=", vTrue, vTrue); // true() cast to 1
        assertBooleanExpression(vLng1, "=", vFals, vFals);

        // integers, equals
        assertBooleanExpression(vInt1, "=", vNull, vFals);
        assertBooleanExpression(vNull, "=", vInt1, vFals);
        assertBooleanExpression(vInt1, "=", vInt1, vTrue);
        assertBooleanExpression(vInt1, "=", vInt2, vFals);
        assertBooleanExpression(vInt1, "=", vTrue, vTrue); // true() cast to 1
        assertBooleanExpression(vInt1, "=", vFals, vFals);

        // doubles, equals
        assertBooleanExpression(vDbl1, "=", vNull, vFals);
        assertBooleanExpression(vNull, "=", vDbl1, vFals);
        assertBooleanExpression(vDbl1, "=", vDbl1, vTrue);
        assertBooleanExpression(vDbl1, "=", vDbl2, vFals);
        assertBooleanExpression(vDbl1, "=", vTrue, vTrue); // true() cast to 1
        assertBooleanExpression(vDbl1, "=", vFals, vFals);

        // strings, equals
        assertBooleanExpression(vStrA, "=", vNull, vFals);
        assertBooleanExpression(vNull, "=", vStrA, vFals);
        assertBooleanExpression(vStrA, "=", vStrA, vTrue);
        assertBooleanExpression(vStrA, "=", vStrB, vFals);
        assertBooleanExpression(vStrA, "=", vTrue, vFals);
        assertBooleanExpression(vStrA, "=", vFals, vFals);
        assertBooleanExpression(vStrA, "=", vStra, vFals);

        // mixed types, equals
        assertBooleanExpression(vLng1, "=", vStr1, vTrue);
        assertBooleanExpression(vDbl1, "=", vStr1, vTrue);
        assertBooleanExpression(vLng1, "=", vTrue, vTrue); //true cast to 1
        assertBooleanExpression(vInt1, "=", vTrue, vTrue); //true cast to 1
        assertBooleanExpression(vDbl1, "=", vTrue, vTrue);
        assertBooleanExpression(vLng0, "=", vFals, vTrue); // false() cast to 0
        assertBooleanExpression(vInt0, "=", vFals, vTrue); // false() cast to 0
        assertBooleanExpression(vDbl0, "=", vFals, vTrue); // false() cast to 0
        assertBooleanExpression(vDbl1, "=", vLng1, vTrue);
        assertBooleanExpression(vStrT, "=", vTrue, vTrue); // true() cast to "true"
        assertBooleanExpression(vStrF, "=", vFals, vTrue); // false() cast to "false"


        // booleans, greater than
        assertBooleanExpression(vTrue, ">", vTrue, vFals);
        assertBooleanExpression(vFals, ">", vFals, vFals);
        assertBooleanExpression(vTrue, ">", vFals, vTrue);

        // longs, greater than
        assertBooleanExpression(vLng1, ">", vNull, vEror);
        assertBooleanExpression(vNull, ">", vLng1, vEror);
        assertBooleanExpression(vLng1, ">", vLng1, vFals);
        assertBooleanExpression(vLng1, ">", vLng2, vFals);
        assertBooleanExpression(vLng2, ">", vLng1, vTrue);
        assertBooleanExpression(vLng1, ">", vTrue, vFals); //true cast to 1
        assertBooleanExpression(vLng2, ">", vDbl1, vTrue);
        assertBooleanExpression(vLng2, ">", vStr1, vTrue);

        // longs, greater than
        assertBooleanExpression(vInt1, ">", vNull, vEror);
        assertBooleanExpression(vNull, ">", vInt1, vEror);
        assertBooleanExpression(vInt1, ">", vInt1, vFals);
        assertBooleanExpression(vInt1, ">", vInt2, vFals);
        assertBooleanExpression(vInt2, ">", vInt1, vTrue);
        assertBooleanExpression(vInt1, ">", vTrue, vFals); // true cast to 1
        assertBooleanExpression(vInt2, ">", vDbl1, vTrue);
        assertBooleanExpression(vInt2, ">", vStr1, vTrue);

        // doubles, greater than
        assertBooleanExpression(vDbl1, ">", vNull, vEror);
        assertBooleanExpression(vNull, ">", vDbl1, vEror);
        assertBooleanExpression(vDbl1, ">", vDbl1, vFals);
        assertBooleanExpression(vDbl1, ">", vDbl2, vFals);
        assertBooleanExpression(vDbl2, ">", vDbl1, vTrue);
        assertBooleanExpression(vDbl1, ">", vTrue, vFals); //true() cast to 1
        assertBooleanExpression(vDbl2, ">", vDbl1, vTrue);
        assertBooleanExpression(vDbl2, ">", vStr1, vTrue);

        // strings, greater than
        assertBooleanExpression(vStrA, ">", vStrA, vFals);
        assertBooleanExpression(vStrA, ">", vStrB, vFals);
        assertBooleanExpression(vStrB, ">", vStrA, vTrue);
        assertBooleanExpression(vStrA, ">", vStr_, vTrue);
        assertBooleanExpression(vStrA, ">", vStr1, vTrue);
        assertBooleanExpression(vStrA, ">", vNull, vEror);
        assertBooleanExpression(vStrA, ">", vStra, vFals);
        assertBooleanExpression(vStra, ">", vStrA, vTrue);


        // booleans, greater than or equal to
        assertBooleanExpression(vTrue, ">=", vTrue, vTrue);
        assertBooleanExpression(vFals, ">=", vFals, vTrue);
        assertBooleanExpression(vTrue, ">=", vFals, vTrue);
        assertBooleanExpression(vFals, ">=", vTrue, vFals);

        // longs, greater than or equal to
        assertBooleanExpression(vLng1, ">=", vNull, vEror);
        assertBooleanExpression(vNull, ">=", vLng1, vEror);
        assertBooleanExpression(vLng1, ">=", vLng1, vTrue);
        assertBooleanExpression(vLng1, ">=", vLng2, vFals);
        assertBooleanExpression(vLng2, ">=", vLng1, vTrue);
        assertBooleanExpression(vLng1, ">=", vTrue, vTrue); // true() cast to 1
        assertBooleanExpression(vLng2, ">=", vDbl1, vTrue);
        assertBooleanExpression(vLng2, ">=", vStr1, vTrue);

        // integers, greater than or equal to
        assertBooleanExpression(vInt1, ">=", vNull, vEror);
        assertBooleanExpression(vNull, ">=", vInt1, vEror);
        assertBooleanExpression(vInt1, ">=", vInt1, vTrue);
        assertBooleanExpression(vInt1, ">=", vInt2, vFals);
        assertBooleanExpression(vInt2, ">=", vInt1, vTrue);
        assertBooleanExpression(vInt1, ">=", vTrue, vTrue); //true() cast to 1
        assertBooleanExpression(vInt2, ">=", vDbl1, vTrue);
        assertBooleanExpression(vInt2, ">=", vStr1, vTrue);

        // doubles, greater than or equal to
        assertBooleanExpression(vDbl1, ">=", vNull, vEror);
        assertBooleanExpression(vNull, ">=", vDbl1, vEror);
        assertBooleanExpression(vDbl1, ">=", vDbl1, vTrue);
        assertBooleanExpression(vDbl1, ">=", vDbl2, vFals);
        assertBooleanExpression(vDbl2, ">=", vDbl1, vTrue);
        assertBooleanExpression(vDbl1, ">=", vTrue, vTrue); // true() cast to 1
        assertBooleanExpression(vDbl2, ">=", vDbl1, vTrue);
        assertBooleanExpression(vDbl2, ">=", vStr1, vTrue);

        // strings, greater than or equal to
        assertBooleanExpression(vStrA, ">=", vStrA, vTrue);
        assertBooleanExpression(vStrA, ">=", vStrB, vFals);
        assertBooleanExpression(vStrB, ">=", vStrA, vTrue);
        assertBooleanExpression(vStrA, ">=", vStr_, vTrue);
        assertBooleanExpression(vStrA, ">=", vStr1, vTrue);
        assertBooleanExpression(vStrA, ">=", vNull, vEror);


        // booleans, less than
        assertBooleanExpression(vTrue, "<", vTrue, vFals);
        assertBooleanExpression(vFals, "<", vFals, vFals);
        assertBooleanExpression(vTrue, "<", vFals, vFals);
        assertBooleanExpression(vFals, "<", vTrue, vTrue);

        // longs, less than
        assertBooleanExpression(vLng1, "<", vNull, vEror);
        assertBooleanExpression(vNull, "<", vLng1, vEror);
        assertBooleanExpression(vLng1, "<", vLng1, vFals);
        assertBooleanExpression(vLng1, "<", vLng2, vTrue);
        assertBooleanExpression(vLng2, "<", vLng1, vFals);
        assertBooleanExpression(vLng1, "<", vTrue, vFals); // true() cast to 1
        assertBooleanExpression(vLng2, "<", vDbl1, vFals);
        assertBooleanExpression(vLng2, "<", vStr1, vFals);

        // integers, less than
        assertBooleanExpression(vInt1, "<", vNull, vEror);
        assertBooleanExpression(vNull, "<", vInt1, vEror);
        assertBooleanExpression(vInt1, "<", vInt1, vFals);
        assertBooleanExpression(vInt1, "<", vInt2, vTrue);
        assertBooleanExpression(vInt2, "<", vInt1, vFals);
        assertBooleanExpression(vInt1, "<", vTrue, vFals); // true() cast to 1
        assertBooleanExpression(vInt2, "<", vDbl1, vFals);
        assertBooleanExpression(vInt2, "<", vStr1, vFals);

        // doubles, less than
        assertBooleanExpression(vDbl1, "<", vNull, vEror);
        assertBooleanExpression(vNull, "<", vDbl1, vEror);
        assertBooleanExpression(vDbl1, "<", vDbl1, vFals);
        assertBooleanExpression(vDbl1, "<", vDbl2, vTrue);
        assertBooleanExpression(vDbl2, "<", vDbl1, vFals);
        assertBooleanExpression(vDbl1, "<", vTrue, vFals); // true() cast to 1
        assertBooleanExpression(vDbl2, "<", vDbl1, vFals);
        assertBooleanExpression(vDbl2, "<", vStr1, vFals);

        // strings, less than
        assertBooleanExpression(vStrA, "<", vStrA, vFals);
        assertBooleanExpression(vStrA, "<", vStrB, vTrue);
        assertBooleanExpression(vStrB, "<", vStrA, vFals);
        assertBooleanExpression(vStrA, "<", vStr_, vFals);
        assertBooleanExpression(vStrA, "<", vStr1, vFals);
        assertBooleanExpression(vStrA, "<", vNull, vEror);


        // booleans, less than or equal to
        assertBooleanExpression(vTrue, "<=", vTrue, vTrue);
        assertBooleanExpression(vFals, "<=", vFals, vTrue);
        assertBooleanExpression(vTrue, "<=", vFals, vFals);
        assertBooleanExpression(vFals, "<=", vTrue, vTrue);

        // longs, less than or equal to
        assertBooleanExpression(vLng1, "<=", vNull, vEror);
        assertBooleanExpression(vNull, "<=", vLng1, vEror);
        assertBooleanExpression(vLng1, "<=", vLng1, vTrue);
        assertBooleanExpression(vLng1, "<=", vLng2, vTrue);
        assertBooleanExpression(vLng2, "<=", vLng1, vFals);
        assertBooleanExpression(vLng1, "<=", vTrue, vTrue); // true() cast to 1
        assertBooleanExpression(vLng2, "<=", vDbl1, vFals);
        assertBooleanExpression(vDbl1, "<=", vLng2, vTrue);
        assertBooleanExpression(vLng2, "<=", vStr1, vFals);

        // integers, less than or equal to
        assertBooleanExpression(vInt1, "<=", vNull, vEror);
        assertBooleanExpression(vNull, "<=", vInt1, vEror);
        assertBooleanExpression(vInt1, "<=", vInt1, vTrue);
        assertBooleanExpression(vInt1, "<=", vInt2, vTrue);
        assertBooleanExpression(vInt2, "<=", vInt1, vFals);
        assertBooleanExpression(vInt1, "<=", vTrue, vTrue); //true() cast to 1
        assertBooleanExpression(vInt2, "<=", vDbl1, vFals);
        assertBooleanExpression(vInt1, "<=", vDbl2, vTrue);
        assertBooleanExpression(vInt2, "<=", vStr1, vFals);
        assertBooleanExpression(vInt1, "<=", vStr2, vTrue);

        // doubles, less than or equal to
        assertBooleanExpression(vDbl1, "<=", vNull, vEror);
        assertBooleanExpression(vNull, "<=", vDbl1, vEror);
        assertBooleanExpression(vDbl1, "<=", vDbl1, vTrue);
        assertBooleanExpression(vDbl1, "<=", vDbl2, vTrue);
        assertBooleanExpression(vDbl2, "<=", vDbl1, vFals);
        assertBooleanExpression(vDbl1, "<=", vTrue, vTrue); // true() caste to 1
        assertBooleanExpression(vDbl2, "<=", vStr1, vFals);
        assertBooleanExpression(vDbl1, "<=", vStr2, vTrue);

        // strings, less than or equal to
        assertBooleanExpression(vStrA, "<=", vStrA, vTrue);
        assertBooleanExpression(vStrA, "<=", vStrB, vTrue);
        assertBooleanExpression(vStrB, "<=", vStrA, vFals);
        assertBooleanExpression(vStrA, "<=", vStr_, vFals);
        assertBooleanExpression(vStrA, "<=", vStr1, vFals);
        assertBooleanExpression(vStrA, "<=", vNull, vEror);

    }


    @Test
    public void testSubstring2() throws ParseException {
        final Generator gen = createGenerator("substring(${val}, 0, 99)");

        gen.set(getVal("this"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this");
    }

    @Test
    public void testHash1() throws ParseException {
        final Generator gen = createGenerator("hash(${val})");

        gen.set(getVal("test"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08");
    }

    @Test
    public void testHash2() throws ParseException {
        final Generator gen = createGenerator("hash(${val}, 'SHA-512')");

        gen.set(getVal("test"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff");
    }

    @Test
    public void testHash3() throws ParseException {
        final Generator gen = createGenerator("hash(${val}, 'SHA-512', 'mysalt')");

        gen.set(getVal("test"));

        final Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("af2910d4d8acf3fcf9683d3ca4425327cb1b4b48bc690f566e27b0e0144c17af82066cf6af14d3a30312ed9df671e0e24b1c66ed3973d1a7836899d75c4d6bb8");
    }

    @Test
    public void testCount() throws ParseException {
        final Generator gen = createGenerator("count()");

        gen.set(getVal(122D));
        gen.set(getVal(133D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));

        gen.set(getVal(11D));
        gen.set(getVal(122D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testCountUnique() throws ParseException {
        final Generator gen = createGenerator("countUnique(${val})");

        gen.set(getVal(122D));
        gen.set(getVal(133D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));

        gen.set(getVal(11D));
        gen.set(getVal(122D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3D, Offset.offset(0D));
    }

    @Test
    public void testCountUniqueStaticValue() throws ParseException {
        final Generator gen = createGenerator("countUnique('test')");

        gen.set(getVal(122D));
        gen.set(getVal(133D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(1D, Offset.offset(0D));

        gen.set(getVal(11D));
        gen.set(getVal(122D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(1D, Offset.offset(0D));
    }

    @Test
    public void testAdd1() throws ParseException {
        final Generator gen = createGenerator("3+4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(7D, Offset.offset(0D));
    }

    @Test
    public void testAdd2() throws ParseException {
        final Generator gen = createGenerator("3+4+5");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(12D, Offset.offset(0D));
    }

    @Test
    public void testAdd3() throws ParseException {
        final Generator gen = createGenerator("2+count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(6D, Offset.offset(0D));
    }

    @Test
    public void testSubtract1() throws ParseException {
        final Generator gen = createGenerator("3-4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(-1D, Offset.offset(0D));
    }

    @Test
    public void testSubtract2() throws ParseException {
        final Generator gen = createGenerator("2-count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(0D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(-2D, Offset.offset(0D));
    }

    @Test
    public void testMultiply1() throws ParseException {
        final Generator gen = createGenerator("3*4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(12D, Offset.offset(0D));
    }

    @Test
    public void testMultiply2() throws ParseException {
        final Generator gen = createGenerator("2*count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testDivide1() throws ParseException {
        final Generator gen = createGenerator("8/4");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));
    }

    @Test
    public void testDivide2() throws ParseException {
        final Generator gen = createGenerator("8/count()");

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));

        gen.set(getVal(1D));
        gen.set(getVal(1D));

        out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));
    }

    @Test
    public void testDivide_byZero() throws ParseException {
        final Generator gen = createGenerator("8/0");

        final Val out = gen.eval();
        assertThat(out instanceof ValErr).isTrue();
        System.out.println("Error message: " + ((ValErr) out).getMessage());
    }

    @Test
    public void testFloorNum1() throws ParseException {
        final Generator gen = createGenerator("floor(8.4234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testFloorNum2() throws ParseException {
        final Generator gen = createGenerator("floor(8.5234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testFloorNum3() throws ParseException {
        final Generator gen = createGenerator("floor(${val})");

        gen.set(getVal(1.34D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(1D, Offset.offset(0D));
    }

    @Test
    public void testFloorNum4() throws ParseException {
        final Generator gen = createGenerator("floor(${val}+count())");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3D, Offset.offset(0D));
    }

    @Test
    public void testFloorNum5() throws ParseException {
        final Generator gen = createGenerator("floor(${val}+count(), 1)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.8D, Offset.offset(0D));
    }

    @Test
    public void testFloorNum6() throws ParseException {
        final Generator gen = createGenerator("floor(${val}+count(), 2)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.86D, Offset.offset(0D));
    }

    @Test
    public void testCeilNum1() throws ParseException {
        final Generator gen = createGenerator("ceiling(8.4234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(9D, Offset.offset(0D));
    }

    @Test
    public void testCeilNum2() throws ParseException {
        final Generator gen = createGenerator("ceiling(8.5234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(9D, Offset.offset(0D));
    }

    @Test
    public void testCeilNum3() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val})");

        gen.set(getVal(1.34D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));
    }

    @Test
    public void testCeilNum4() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val}+count())");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testCeilNum5() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val}+count(), 1)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.9D, Offset.offset(0D));
    }

    @Test
    public void testCeilNum6() throws ParseException {
        final Generator gen = createGenerator("ceiling(${val}+count(), 2)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.87D, Offset.offset(0D));
    }

    @Test
    public void testRoundNum1() throws ParseException {
        final Generator gen = createGenerator("round(8.4234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testRoundNum2() throws ParseException {
        final Generator gen = createGenerator("round(8.5234)");

        gen.set(getVal(1D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(9D, Offset.offset(0D));
    }

    @Test
    public void testRoundNum3() throws ParseException {
        final Generator gen = createGenerator("round(${val})");

        gen.set(getVal(1.34D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(1D, Offset.offset(0D));
    }

    @Test
    public void testRoundNum4() throws ParseException {
        final Generator gen = createGenerator("round(${val}+count())");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(4D, Offset.offset(0D));
    }

    @Test
    public void testRoundNum5() throws ParseException {
        final Generator gen = createGenerator("round(${val}+count(), 1)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.9D, Offset.offset(0D));
    }

    @Test
    public void testRoundNum6() throws ParseException {
        final Generator gen = createGenerator("round(${val}+count(), 2)");

        gen.set(getVal(1.34D));
        gen.set(getVal(1.8655D));

        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(3.87D, Offset.offset(0D));
    }

    @Test
    public void testTime() throws ParseException {
        testTime("floorSecond", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:12.000Z");
        testTime("floorMinute", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:00.000Z");
        testTime("floorHour", "2014-02-22T12:12:12.888Z", "2014-02-22T12:00:00.000Z");
        testTime("floorDay", "2014-02-22T12:12:12.888Z", "2014-02-22T00:00:00.000Z");
        testTime("floorMonth", "2014-02-22T12:12:12.888Z", "2014-02-01T00:00:00.000Z");
        testTime("floorYear", "2014-02-22T12:12:12.888Z", "2014-01-01T00:00:00.000Z");

        testTime("ceilingSecond", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:13.000Z");
        testTime("ceilingMinute", "2014-02-22T12:12:12.888Z", "2014-02-22T12:13:00.000Z");
        testTime("ceilingHour", "2014-02-22T12:12:12.888Z", "2014-02-22T13:00:00.000Z");
        testTime("ceilingDay", "2014-02-22T12:12:12.888Z", "2014-02-23T00:00:00.000Z");
        testTime("ceilingMonth", "2014-02-22T12:12:12.888Z", "2014-03-01T00:00:00.000Z");
        testTime("ceilingYear", "2014-02-22T12:12:12.888Z", "2015-01-01T00:00:00.000Z");

        testTime("roundSecond", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:13.000Z");
        testTime("roundMinute", "2014-02-22T12:12:12.888Z", "2014-02-22T12:12:00.000Z");
        testTime("roundHour", "2014-02-22T12:12:12.888Z", "2014-02-22T12:00:00.000Z");
        testTime("roundDay", "2014-02-22T12:12:12.888Z", "2014-02-23T00:00:00.000Z");
        testTime("roundMonth", "2014-02-22T12:12:12.888Z", "2014-03-01T00:00:00.000Z");
        testTime("roundYear", "2014-02-22T12:12:12.888Z", "2014-01-01T00:00:00.000Z");
    }

    private void testTime(final String function, final String in, final String expected) throws ParseException {
        final double expectedMs = DateUtil.parseNormalDateTimeString(expected);
        final String expression = function + "(${val})";
        final Generator gen = createGenerator(expression);

        gen.set(getVal(in));
        final Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(expectedMs, Offset.offset(0D));
    }

    @Test
    public void testBODMAS1() throws ParseException {
        final Generator gen = createGenerator("4+4/2+2");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 8.
        assertThat(out.toDouble()).isEqualTo(8D, Offset.offset(0D));
    }

    @Test
    public void testBODMAS2() throws ParseException {
        final Generator gen = createGenerator("(4+4)/2+2");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 6.
        assertThat(out.toDouble()).isEqualTo(6D, Offset.offset(0D));
    }

    @Test
    public void testBODMAS3() throws ParseException {
        final Generator gen = createGenerator("(4+4)/(2+2)");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 6 or even 4 - BODMAS should be 2.
        assertThat(out.toDouble()).isEqualTo(2D, Offset.offset(0D));
    }

    @Test
    public void testBODMAS4() throws ParseException {
        final Generator gen = createGenerator("4+4/2+2*3");

        final Val out = gen.eval();

        // Non BODMAS would evaluate as 18 - BODMAS should be 12.
        assertThat(out.toDouble()).isEqualTo(12D, Offset.offset(0D));
    }

    @Test
    public void testExtractAuthorityFromUri() throws ParseException {
        final Generator gen = createGenerator("extractAuthorityFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("www.example.com:1234");
    }

    @Test
    public void testExtractFragmentFromUri() throws ParseException {
        final Generator gen = createGenerator("extractFragmentFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path#frag"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("frag");
    }

    @Test
    public void testExtractHostFromUri() throws ParseException {
        final Generator gen = createGenerator("extractHostFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("www.example.com");
    }

    @Test
    public void testExtractPathFromUri() throws ParseException {
        final Generator gen = createGenerator("extractPathFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("/this/is/a/path");
    }

    @Test
    public void testExtractPortFromUri() throws ParseException {
        final Generator gen = createGenerator("extractPortFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("1234");
    }

    @Test
    public void testExtractQueryFromUri() throws ParseException {
        final Generator gen = createGenerator("extractQueryFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path?this=that&foo=bar"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("this=that&foo=bar");
    }

    @Test
    public void testExtractSchemeFromUri() throws ParseException {
        final Generator gen = createGenerator("extractSchemeFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("http");
    }

    @Test
    public void testExtractSchemeSpecificPartFromUri() throws ParseException {
        final Generator gen = createGenerator("extractSchemeSpecificPartFromUri(${val})");

        gen.set(getVal("http://www.example.com:1234/this/is/a/path"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("//www.example.com:1234/this/is/a/path");
    }

    @Test
    public void testExtractUserInfoFromUri() throws ParseException {
        final Generator gen = createGenerator("extractUserInfoFromUri(${val})");

        gen.set(getVal("http://john:doe@example.com:81/"));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("john:doe");
    }

    @Test
    public void testParseDate1() throws ParseException {
        final Generator gen = createGenerator("parseDate(${val})");

        gen.set(getVal("2014-02-22T12:12:12.888Z"));
        Val out = gen.eval();
        assertThat(out.toLong().longValue()).isEqualTo(1393071132888L);
    }

    @Test
    public void testParseDate2() throws ParseException {
        final Generator gen = createGenerator("parseDate(${val}, 'yyyy MM dd')");

        gen.set(getVal("2014 02 22"));
        Val out = gen.eval();
        assertThat(out.toLong().longValue()).isEqualTo(1393027200000L);
    }

    @Test
    public void testParseDate3() throws ParseException {
        final Generator gen = createGenerator("parseDate(${val}, 'yyyy MM dd', '+0400')");

        gen.set(getVal("2014 02 22"));
        Val out = gen.eval();
        assertThat(out.toLong().longValue()).isEqualTo(1393012800000L);
    }

    @Test
    public void testFormatDate1() throws ParseException {
        final Generator gen = createGenerator("formatDate(${val})");

        gen.set(getVal(1393071132888L));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("2014-02-22T12:12:12.888Z");
    }

    @Test
    public void testFormatDate2() throws ParseException {
        final Generator gen = createGenerator("formatDate(${val}, 'yyyy MM dd')");

        gen.set(getVal(1393071132888L));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("2014 02 22");
    }

    @Test
    public void testFormatDate3() throws ParseException {
        final Generator gen = createGenerator("formatDate(${val}, 'yyyy MM dd', '+1200')");

        gen.set(getVal(1393071132888L));
        Val out = gen.eval();
        assertThat(out.toString()).isEqualTo("2014 02 23");
    }

    @Test
    public void testVariance1() throws ParseException {
        final Generator gen = createGenerator("variance(600, 470, 170, 430, 300)");

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(21704D, Offset.offset(0D));
    }

    @Test
    public void testVariance2() throws ParseException {
        final Generator gen = createGenerator("variance(${val})");

        gen.set(getVal(600));
        gen.set(getVal(470));
        gen.set(getVal(170));
        gen.set(getVal(430));
        gen.set(getVal(300));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(21704D, Offset.offset(0D));
    }

    @Test
    public void testStDev1() throws ParseException {
        final Generator gen = createGenerator("round(stDev(600, 470, 170, 430, 300))");

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(147, Offset.offset(0D));
    }

    @Test
    public void testStDev2() throws ParseException {
        final Generator gen = createGenerator("round(stDev(${val}))");

        gen.set(getVal(600));
        gen.set(getVal(470));
        gen.set(getVal(170));
        gen.set(getVal(430));
        gen.set(getVal(300));

        Val out = gen.eval();
        assertThat(out.toDouble()).isEqualTo(147, Offset.offset(0D));
    }

    @Test
    public void testToBoolean1() throws ParseException {
        final Generator gen = createGenerator("toBoolean('true')");
        assertThat(gen.eval()).isEqualTo(ValBoolean.TRUE);
    }

    @Test
    public void testToBoolean2() throws ParseException {
        final Generator gen = createGenerator("toBoolean(${val})");
        gen.set(getVal("true"));
        assertThat(gen.eval()).isEqualTo(ValBoolean.TRUE);
    }

    @Test
    public void testToDouble1() throws ParseException {
        final Generator gen = createGenerator("toDouble('100')");
        assertThat(gen.eval()).isEqualTo(ValDouble.create(100));
    }

    @Test
    public void testToDouble2() throws ParseException {
        final Generator gen = createGenerator("toDouble(${val})");
        gen.set(getVal("100"));
        assertThat(gen.eval()).isEqualTo(ValDouble.create(100));
    }

    @Test
    public void testToInteger1() throws ParseException {
        final Generator gen = createGenerator("toInteger('100')");
        assertThat(gen.eval()).isEqualTo(ValInteger.create(100));
    }

    @Test
    public void testToInteger2() throws ParseException {
        final Generator gen = createGenerator("toInteger(${val})");
        gen.set(getVal("100"));
        assertThat(gen.eval()).isEqualTo(ValInteger.create(100));
    }

    @Test
    public void testToLong1() throws ParseException {
        final Generator gen = createGenerator("toLong('100')");
        assertThat(gen.eval()).isEqualTo(ValLong.create(100));
    }

    @Test
    public void testToLong2() throws ParseException {
        final Generator gen = createGenerator("toLong(${val})");
        gen.set(getVal("100"));
        assertThat(gen.eval()).isEqualTo(ValLong.create(100));
    }

    @Test
    public void testToString1() throws ParseException {
        final Generator gen = createGenerator("toString('100')");
        assertThat(gen.eval()).isEqualTo(ValString.create("100"));
    }

    @Test
    public void testToString2() throws ParseException {
        final Generator gen = createGenerator("toString(${val})");
        gen.set(getVal("100"));
        assertThat(gen.eval()).isEqualTo(ValString.create("100"));
    }

    @Test
    public void testTypeOf() throws ParseException {
        ValBoolean vTrue = ValBoolean.TRUE;
        ValBoolean vFals = ValBoolean.FALSE; // intentional typo to keep var name length consistent
        ValNull vNull = ValNull.INSTANCE;
        ValErr vEror = ValErr.create("Expecting an error"); // intentional typo to keep var name length consistent
        ValLong vLng0 = ValLong.create(0L);
        ValInteger vInt0 = ValInteger.create(1);
        ValDouble vDbl0 = ValDouble.create(1.1);
        ValString vStr1 = ValString.create("abc");

        assertTypeOf(vTrue, "boolean");
        assertTypeOf(vFals, "boolean");
        assertTypeOf(vNull, "null");
        assertTypeOf(vEror, "error");
        assertTypeOf(vLng0, "long");
        assertTypeOf(vInt0, "integer");
        assertTypeOf(vDbl0, "double");
        assertTypeOf(vStr1, "string");

        assertTypeOf("typeOf(err())", "error");
        assertTypeOf("typeOf(null())", "null");
        assertTypeOf("typeOf(true())", "boolean");
        assertTypeOf("typeOf(1+2)", "double");
        assertTypeOf("typeOf(concat('a', 'b'))", "string");
        assertTypeOf("typeOf('xxx')", "string");
        assertTypeOf("typeOf(1.234)", "double");
        assertTypeOf("typeOf(2>=1)", "boolean");
    }

    private Generator createGenerator(final String expression) throws ParseException {
        final Expression exp = createExpression(expression);
        final Generator gen = exp.createGenerator();
        testSerialisation(gen);
        return gen;
    }

    private Expression createExpression(final String expression) throws ParseException {
        final FieldIndexMap fieldIndexMap = new FieldIndexMap();
        fieldIndexMap.create("val", true);

        final Expression exp = parser.parse(fieldIndexMap, expression);
        final String actual = exp.toString();
        assertThat(actual).isEqualTo(expression);

        testSerialisation(exp);
        return exp;
    }

    private Generator createGenerator2(final String expression) throws ParseException {
        final Expression exp = createExpression2(expression);
        final Generator gen = exp.createGenerator();
        testSerialisation(gen);
        return gen;
    }

    private Expression createExpression2(final String expression) throws ParseException {
        final FieldIndexMap fieldIndexMap = new FieldIndexMap();
        fieldIndexMap.create("val1", true);
        fieldIndexMap.create("val2", true);

        final Expression exp = parser.parse(fieldIndexMap, expression);
        final String actual = exp.toString();
        assertThat(actual).isEqualTo(expression);

        testSerialisation(exp);
        return exp;
    }

    private void testSerialisation(final Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(baos);
            out.writeObject(object);
            out.close();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private void assertBooleanExpression(final Val val1, final String operator, final Val val2, final Val expectedOutput)
            throws ParseException {

        final String expression = String.format("(${val1}%s${val2})", operator);
        final Expression exp = createExpression2(expression);
        final Generator gen = exp.createGenerator();
        gen.set(new Val[]{val1, val2});
        Val out = gen.eval();

        System.out.println(String.format("[%s: %s] %s [%s: %s] => [%s: %s%s]",
                val1.getClass().getSimpleName(), val1.toString(),
                operator,
                val2.getClass().getSimpleName(), val2.toString(),
                out.getClass().getSimpleName(), out.toString(),
                (out instanceof ValErr ? (" - " + ((ValErr) out).getMessage()) : "")));

        if (!(expectedOutput instanceof ValErr)) {
            assertThat(out).isEqualTo(expectedOutput);
        }
        assertThat(out.getClass()).isEqualTo(expectedOutput.getClass());
    }

    private void assertTypeOf(final String expression, final String expectedType) throws ParseException {
        final Expression exp = createExpression(expression);
        final Generator gen = exp.createGenerator();
        Val out = gen.eval();

        System.out.println(String.format("%s => [%s:%s%s]",
                expression,
                out.getClass().getSimpleName(), out.toString(),
                (out instanceof ValErr ? (" - " + ((ValErr) out).getMessage()) : "")));

        // The output type is always wrapped in a ValString
        assertThat(out.getType()).isEqualTo("string");

        assertThat(out).isInstanceOf(ValString.class);
        assertThat(out.toString()).isEqualTo(expectedType);
    }

    private void assertTypeOf(final Val val1, final String expectedType) throws ParseException {

        final String expression = "typeOf(${val})";
        final Expression exp = createExpression(expression);
        final Generator gen = exp.createGenerator();
        gen.set(new Val[]{val1});
        Val out = gen.eval();

        System.out.println(String.format("%s - [%s:%s] => [%s:%s%s]",
                expression,
                val1.getClass().getSimpleName(), val1.toString(),
                out.getClass().getSimpleName(), out.toString(),
                (out instanceof ValErr ? (" - " + ((ValErr) out).getMessage()) : "")));

        // The output type is always wrapped in a ValString
        assertThat(out.getType()).isEqualTo("string");

        assertThat(out).isInstanceOf(ValString.class);
        assertThat(out.toString()).isEqualTo(expectedType);
    }
}
