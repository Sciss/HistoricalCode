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

package com.pavelfatin.toyide.editor

import com.pavelfatin.toyide.Observable
import com.pavelfatin.toyide.document.Document

import scala.swing.Component

trait Editor extends Observable {
  def document: Document

  def data: Data

  def holder: ErrorHolder

  def terminal: Terminal

  def component: Component

  def pane: Component

  var text: String

  def actions: EditorActions

  def message: Option[String]

  def dispose(): Unit
}