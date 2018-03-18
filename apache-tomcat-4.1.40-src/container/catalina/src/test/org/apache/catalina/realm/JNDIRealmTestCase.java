/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.realm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Jeff Tulley
 */
public class JNDIRealmTestCase extends TestCase {
    public JNDIRealmTestCase(String arg0) {
        super(arg0);
    }

    public void testParseSimpleUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "o=somepath";
        String[] expected = { searchPath };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseSimpleUserPatternWithParens() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=somepath)";
        String[] expected = { "o=somepath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    // would somebody do this?  Probably not, but it wouldn't hurt to support it for resiliency sake
    public void testParseSimpleUserPatternWithParensAndLDAPOr() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(|(o=somepath))";
        String[] expected = { "o=somepath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseSimpleUserPatternWithParensPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=so\\(me\\)path)";
        String[] expected = { "o=so\\(me\\)path" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseSimpleUserPatternWithParensReallyPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=so\\(mepath\\))";
        String[] expected = { "o=so\\(mepath\\)" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseTwoPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=somepath)(o=someotherpath)";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    // people will probably try this, need to handle it
    public void testParseLegitLDAPTwoPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(|(o=somepath)(o=someotherpath))";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseTwoPartUserPatternIgnoreWhitespace() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = " (o=somepath)   (o=someotherpath)    ";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    // people will probably try this, need to handle it
    public void testParseLegitLDAPTwoPartUserPatternIgnoreWhitespace() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(| (o=somepath)         (o=someotherpath)  )";
        String[] expected = { "o=somepath", "o=someotherpath" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseTwoPartUserPatternPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(o=somepath)(o=someo\\(ther\\(path)";
        String[] expected = { "o=somepath", "o=someo\\(ther\\(path" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseLegitLDAPTwoPartUserPatternPathological() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "(|(o=somepath)(o=someo\\(ther\\(path))";
        String[] expected = { "o=somepath", "o=someo\\(ther\\(path" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseMultiPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(o=somepath)(o=someotherpath)(o=somepath2)(o=someotherpath2)(o=somepath3)(o=someotherpath3)";
        String[] expected =
            {
                "o=somepath",
                "o=someotherpath",
                "o=somepath2",
                "o=someotherpath2",
                "o=somepath3",
                "o=someotherpath3" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseComplexMultiPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(cn={0},ou=ourou,o=somepath)(cn={0},ou=ourou,o=somepath2)(cn={0},ou=ourou,o=someotherpath2)";
        String[] expected =
            {
                "cn={0},ou=ourou,o=somepath",
                "cn={0},ou=ourou,o=somepath2",
                "cn={0},ou=ourou,o=someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseLegitLDAPComplexMultiPartUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(|(cn={0},ou=ourou,o=somepath)(cn={0},ou=ourou,o=somepath2)(cn={0},ou=ourou,o=someotherpath2))";
        String[] expected =
            {
                "cn={0},ou=ourou,o=somepath",
                "cn={0},ou=ourou,o=somepath2",
                "cn={0},ou=ourou,o=someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseComplexMultiPartUserPatternEdirectorySyntax() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(|(cn={0}.ou=ourou.o=somepath)(cn={0}.ou=ourou.o=somepath2)(cn={0}.ou=ourou.o=someotherpath2))";
        String[] expected =
            {
                "cn={0}.ou=ourou.o=somepath",
                "cn={0}.ou=ourou.o=somepath2",
                "cn={0}.ou=ourou.o=someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseComplexMultiPartUserPatternTypelessEdirectorySyntax() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath =
            "(|({0}.ourou.somepath)({0}.ourou.somepath2)({0}.ourou.someotherpath2))";
        String[] expected =
            {
                "{0}.ourou.somepath",
                "{0}.ourou.somepath2",
                "{0}.ourou.someotherpath2" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseEmptyUserPattern() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "";
        String[] expected = { "" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }

    public void testParseEmptyUserPatternWithParens() {
        JNDIRealm realm = new JNDIRealm();
        String searchPath = "()";
        String[] expected = { "" };
        String[] actual = realm.parseUserPatternString(searchPath);
        assertStringArraysEquals(expected, actual);
    }


    public void testRFC2254EncodingEmptyString() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("");
        Assert.assertEquals("empty", "", actual);
    }

    public void testRFC2254EncodingNoChange() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=aname,o=acontext");
        Assert.assertEquals("no change", "cn=aname,o=acontext", actual);
    }

    public void testRFC2254EncodingAsterisk() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=some*name,o=somecontext");
        Assert.assertEquals("asterisk", "cn=some\\2aname,o=somecontext", actual);
    }

    public void testRFC2254EncodingAsteriskAtEnd() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=somename,o=somecontext*");
        Assert.assertEquals("asterisk", "cn=somename,o=somecontext\\2a", actual);
    }

    public void testRFC2254EncodingAsteriskAtBeginning() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=*somename,o=somecontext");
        Assert.assertEquals("asterisk", "cn=\\2asomename,o=somecontext", actual);
    }

    public void testRFC2254EncodingOpenParen() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=somena(me,o=somecontext");
        Assert.assertEquals("asterisk", "cn=somena\\28me,o=somecontext", actual);
    }

    public void testRFC2254EncodingCloseParen() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=somename,o=some)context");
        Assert.assertEquals("asterisk", "cn=somename,o=some\\29context", actual);
    }

    public void testRFC2254EncodingSlash() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=s\\omename,o=somecontext");
        Assert.assertEquals("asterisk", "cn=s\\5comename,o=somecontext", actual);
    }

    public void testRFC2254EncodingNul() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=so\0mename,o=somecontext");
        Assert.assertEquals("asterisk", "cn=so\\00mename,o=somecontext", actual);
    }

    public void testRFC2254EncodingTwoCharsInARow() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=so\\\\mename,o=somecontext");
        Assert.assertEquals("asterisk", "cn=so\\5c\\5cmename,o=somecontext", actual);
    }

    public void testRFC2254EncodingAllEncodedChars() {
        JNDIRealm realm = new JNDIRealm();
        String actual = realm.doRFC2254Encoding("cn=so\\*()\0\\mename,o=somecontext");
        Assert.assertEquals("asterisk", "cn=so\\5c\\2a\\28\\29\\00\\5cmename,o=somecontext", actual);
    }

    public void assertStringArraysEquals(String[] expected, String[] actual) {
        Assert.assertTrue("not null", actual != null);
        Assert.assertEquals("array count is wrong", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals("element " + i + " is wrong", expected[i], actual[i]);
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JNDIRealmTestCase.class);
    }
}


