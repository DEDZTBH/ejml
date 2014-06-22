/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.equation;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestEquation {

    Random rand = new Random(234);

    /**
     * Basic test which checks ability parse basic operators and order of operation
     */
    @Test
    public void compile_basic() {
        Equation eq = new Equation();

        SimpleMatrix A = new SimpleMatrix(5, 6);
        SimpleMatrix B = SimpleMatrix.random(5, 6, -1, 1, rand);
        SimpleMatrix C = SimpleMatrix.random(5, 4, -1, 1, rand);
        SimpleMatrix D = SimpleMatrix.random(4, 6, -1, 1, rand);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");
        eq.alias(C.getMatrix(), "C");
        eq.alias(D.getMatrix(), "D");

        Sequence sequence = eq.compile("A=B+C*D-B");
        SimpleMatrix expected = C.mult(D);
        sequence.perform();
        assertTrue(expected.isIdentical(A,1e-15));
    }

    /**
     * Output is included in input
     */
    @Test
    public void compile_output() {
        Equation eq = new Equation();

        SimpleMatrix A = SimpleMatrix.random(6, 6, -1, 1, rand);
        SimpleMatrix B = SimpleMatrix.random(6, 6, -1, 1, rand);

        eq.alias(A.getMatrix(), "A");
        eq.alias(B.getMatrix(), "B");

        Sequence sequence = eq.compile("A=A*B");
        SimpleMatrix expected = A.mult(B);
        sequence.perform();
        assertTrue(expected.isIdentical(A,1e-15));
    }

    @Test
    public void compile_parentheses() {
        fail("Implement");
    }

    @Test
    public void compile_transpose() {
        fail("Implement");
    }

    @Test
    public void compile_negative() {
        fail("Implement");
    }

    @Test
    public void compile_scalar() {
        fail("Implement");
    }

    @Test
    public void compile_function() {
        fail("Implement");
    }

    @Test
    public void parseOperations() {
        Equation eq = new Equation();

        eq.alias(new DenseMatrix64F(1, 1), "A");
        eq.alias(new DenseMatrix64F(1, 1), "B");
        eq.alias(new DenseMatrix64F(1, 1), "C");

        TokenList tokens = eq.extractTokens("A=B+B-A*B*A");
        Sequence sequence = new Sequence();

        eq.parseOperations(tokens.first.next.next,new char[]{'*'},tokens,sequence);

        assertEquals(2,sequence.operations.size());
        assertEquals(7,tokens.size);
        assertTrue(tokens.last.isVariable());
        assertTrue('-'==tokens.last.previous.getSymbol());

        tokens = eq.extractTokens("A=B+B*B*A-B");
        sequence = new Sequence();

        eq.parseOperations(tokens.first.next.next,new char[]{'+','-'},tokens,sequence);

        assertEquals(2,sequence.operations.size());
        assertEquals(7,tokens.size);
        assertTrue(tokens.last.isVariable());
        assertTrue('*'==tokens.last.previous.getSymbol());
        assertTrue('*' == tokens.first.next.next.next.getSymbol());
    }

    @Test
    public void createOp() {
        Equation eq = new Equation();

        eq.alias(new DenseMatrix64F(1, 1), "A");
        eq.alias(new DenseMatrix64F(1, 1), "B");

        TokenList tokens = eq.extractTokens("A=A*B");

        TokenList.Token t0 = tokens.first.next.next;
        TokenList.Token t1 = t0.next;
        TokenList.Token t2 = t1.next;

        Sequence sequence = new Sequence();

        TokenList.Token found = eq.createOp(t0,t1,t2,tokens,sequence);
        assertTrue(found.isVariable());
        assertEquals(3, tokens.size);
        assertTrue('=' == tokens.first.next.getSymbol());
        assertTrue(found==tokens.last);
        assertEquals(1, sequence.operations.size());
    }

    @Test
    public void lookupVariable() {
        Equation eq = new Equation();
        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        eq.lookupVariable("A");
        eq.lookupVariable("BSD");
        try {
            eq.lookupVariable("dDD");
            fail("Exception should have been thrown");
        } catch( RuntimeException ignore ) {}

    }

    @Test
    public void extractTokens() {
        Equation eq = new Equation();
        eq.alias(new DenseMatrix64F(1,1),"A");
        eq.alias(new DenseMatrix64F(1,1),"BSD");

        Variable v0 = eq.lookupVariable("A");
        Variable v1 = eq.lookupVariable("BSD");


        TokenList list = eq.extractTokens("A = A*A + BSD*(A+BSD) -A*BSD");

        TokenList.Token t = list.getFirst();

        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue('='==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue('*'==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue('+'==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
        assertTrue('*'==t.getSymbol()); t = t.next;
        assertTrue('('==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue('+'==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
        assertTrue(')'==t.getSymbol()); t = t.next;
        assertTrue('-'==t.getSymbol()); t = t.next;
        assertTrue(v0==t.getVariable()); t = t.next;
        assertTrue('*'==t.getSymbol()); t = t.next;
        assertTrue(v1==t.getVariable()); t = t.next;
    }

    @Test
    public void isTargetOp() {
        assertTrue(Equation.isTargetOp(new TokenList.Token('a'),new char[]{'b','c','a'}));
        assertFalse(Equation.isTargetOp(new TokenList.Token('d'), new char[]{'b', 'c', 'a'}));
    }

    @Test
    public void isLetter() {
        assertTrue(Equation.isLetter('a'));
        assertTrue(Equation.isLetter('_'));
        assertTrue(Equation.isLetter('5'));

        assertFalse(Equation.isLetter(' '));
        assertFalse(Equation.isLetter('\t'));
        assertFalse(Equation.isLetter('*'));
        assertFalse(Equation.isLetter('+'));
        assertFalse(Equation.isLetter('-'));
        assertFalse(Equation.isLetter('('));
        assertFalse(Equation.isLetter(')'));
        assertFalse(Equation.isLetter('='));
    }

}
