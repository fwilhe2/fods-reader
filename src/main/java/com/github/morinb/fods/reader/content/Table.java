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

package com.github.morinb.fods.reader.content;

import com.github.morinb.fods.reader.content.cell.Cell;
import com.github.morinb.fods.reader.exceptions.InvalidCoordinatesException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Table {
    private final String name;
    private final long numberOfColumns;

    private final
    List<List<Cell>> rows;

    public Table(String name, long numberOfColumns, List<List<Cell>> rows) {
        this.name = name;
        this.numberOfColumns = numberOfColumns;
        this.rows = rows;
    }

    public static TableBuilder builder() {
        return new TableBuilder();
    }

    public Cell getCellAt(int row, int col) {
        if (row > rows.size() || col > numberOfColumns) {
            throw new InvalidCoordinatesException(row, col, rows.size()
                    , numberOfColumns);
        }

        return rows.get(row - 1).get(col - 1);
    }

    public String getValueAt(int row, int col) {
        return getCellAt(row, col).getText();
    }

    public String getName() {
        return this.name;
    }

    public long getNumberOfColumns() {
        return this.numberOfColumns;
    }

    public List<List<Cell>> getRows() {
        return this.rows;
    }

    public static class TableBuilder {
        private String name;
        private long numberOfColumns;
        private ArrayList<List<Cell>> rows;

        TableBuilder() {
        }

        public TableBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TableBuilder numberOfColumns(long numberOfColumns) {
            this.numberOfColumns = numberOfColumns;
            return this;
        }

        public TableBuilder row(List<Cell> row) {
            if (this.rows == null) this.rows = new ArrayList<List<Cell>>();
            this.rows.add(row);
            return this;
        }

        public TableBuilder rows(Collection<? extends List<Cell>> rows) {
            if (this.rows == null) this.rows = new ArrayList<List<Cell>>();
            this.rows.addAll(rows);
            return this;
        }

        public TableBuilder clearRows() {
            if (this.rows != null)
                this.rows.clear();
            return this;
        }

        public Table build() {
            List<List<Cell>> rows;
            switch (this.rows == null ? 0 : this.rows.size()) {
                case 0:
                    rows = java.util.Collections.emptyList();
                    break;
                case 1:
                    rows = java.util.Collections.singletonList(this.rows.get(0));
                    break;
                default:
                    rows = java.util.Collections.unmodifiableList(new ArrayList<List<Cell>>(this.rows));
            }

            return new Table(name, numberOfColumns, rows);
        }

        public String toString() {
            return "Table.TableBuilder(name=" + this.name + ", numberOfColumns=" + this.numberOfColumns + ", rows=" + this.rows + ")";
        }
    }
}
