/*
 * Copyright 2018 Pavel Fatin, https://pavelfatin.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pavelfatin.toyide.languages.toy.parser

import org.junit.Test

class WhileParserTest extends ParserTest(WhileParser) {
  @Test
  def normal(): Unit = {
    assertParsed("while (i > 0) { a = a + 1; }",
      """
      while
        WHILE
        LPAREN
        binaryExpression
          referenceToValue
            i
          GT
          literal
            0
        RPAREN
        block
          LBRACE
          assignment
            referenceToValue
              a
            EQ
            binaryExpression
              referenceToValue
                a
              PLUS
              literal
                1
            SEMI
          RBRACE
      """)
  }
}