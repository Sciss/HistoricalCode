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

class TypeSpecParserTest extends ParserTest(TypeSpecParser) {
  @Test
  def typeInteger(): Unit = {
    assertParsed(": integer",
      """
      typeSpec
        COLON
        INTEGER
      """)
  }

  @Test
  def typeString(): Unit = {
    assertParsed(": string",
      """
      typeSpec
        COLON
        STRING
      """)
  }

  @Test
  def typeBoolean(): Unit = {
    assertParsed(": boolean",
      """
      typeSpec
        COLON
        BOOLEAN
      """)
  }

  @Test
  def typeVoid(): Unit = {
    assertParsed(": void",
      """
      typeSpec
        COLON
        VOID
      """)
  }
}