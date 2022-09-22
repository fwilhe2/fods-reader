/*
 * Copyright 2021 baptiste
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.morinb.fods.reader.content.cell;

import com.github.morinb.fods.reader.content.CellType;
import com.github.morinb.fods.reader.content.ValueType;
public class FormulaCell implements Cell {
    private final String text;
    private final String formula;
    private final ValueType valueType;

    public FormulaCell(String text, String formula, ValueType valueType) {
        this.text = text;
        this.formula = formula;
        this.valueType = valueType;
    }

    @Override
    public CellType getCellType() {
        return CellType.FORMULA;
    }


    public String getText() {
        return this.text;
    }

    public String getFormula() {
        return this.formula;
    }

    public ValueType getValueType() {
        return this.valueType;
    }
}
