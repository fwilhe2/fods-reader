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

package com.github.morinb.fods.reader;

import com.github.morinb.fods.reader.content.*;
import com.github.morinb.fods.reader.content.cell.Cell;
import com.github.morinb.fods.reader.content.cell.EmptyCell;
import com.github.morinb.fods.reader.content.cell.FormulaCell;
import com.github.morinb.fods.reader.content.cell.TextCell;
import com.github.morinb.fods.reader.exceptions.Logger;
import com.github.morinb.fods.reader.exceptions.TooManyBodyItemException;
import com.github.morinb.fods.reader.exceptions.TooManySpreadsheetItemException;
import com.github.morinb.fods.reader.exceptions.TooManyTextItemException;
import com.github.morinb.fods.reader.meta.Metadata;
import com.github.morinb.fods.reader.settings.Settings;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * FODS Spreadsheet
 */
public class OfficeDocument {
    private static final Logger LOGGER = new Logger();
    public static final String TABLE_NUMBER_COLUMNS_REPEATED = "table:number-columns-repeated";

    private Metadata metadata;
    private Settings settings;
    private Body body;

    /**
     * Creates an OfficeDocument from a Flat ODS file input stream.
     *
     * @param inputStream the fods file inputstream.
     */
    public OfficeDocument(InputStream inputStream) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(unused -> "", exception);
        }
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(unused -> "", exception);
        }


        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(inputStream);
            final Element rootElement = document.getDocumentElement();
            rootElement.normalize();

            metadata = readMetadata(rootElement.getElementsByTagName("office:meta"));
            settings = readSettings(rootElement.getElementsByTagName("office.settings"));
            body = readBody(rootElement.getElementsByTagName("office:body"));


        } catch (IllegalArgumentException | ParserConfigurationException | SAXException | IOException e) {
            MDC.put("UUID", "[" + UUID.randomUUID().toString() + "]");
            LOGGER.error(unused -> MethodHandles.lookup().lookupClass().getSimpleName());
        }
    }


    /**
     * Read the body part of fods file
     *
     * @param nodeList the office:body xml node
     * @return the Body element
     */
    private Body readBody(NodeList nodeList) {
        if (nodeList.getLength() != 1) {
            throw new TooManyBodyItemException(nodeList.getLength());
        }
        final Node bodyNode = nodeList.item(0);

        int nbBodyChildElements = 0;
        final NodeList bodyChildNodes = bodyNode.getChildNodes();
        CalculationSettings calculationSettings = new CalculationSettings(false, false, false);
        final List<Table> tables = new ArrayList<>();
        for (int bodyIndex = 0; bodyIndex < bodyChildNodes.getLength(); bodyIndex++) {
            if (bodyChildNodes.item(bodyIndex).getNodeType() == Node.ELEMENT_NODE) {
                nbBodyChildElements++;
                if (nbBodyChildElements > 1) {
                    throw new TooManySpreadsheetItemException(bodyNode.getChildNodes().getLength());
                }

                final Node spreadSheet = bodyChildNodes.item(bodyIndex);
                final NodeList childrenNodes = spreadSheet.getChildNodes();

                for (int index = 0; index < childrenNodes.getLength(); index++) {
                    final Node item = childrenNodes.item(index);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        if (item.getNodeName().equals("table:calculation-settings")) {
                            calculationSettings = readCalculationSettings(item);
                        } else if (item.getNodeName().equals("table:table")) {
                            tables.add(readTable(item));
                        }
                    }
                }

            }
        }


        final Spreadsheet spreadsheet = new Spreadsheet(calculationSettings, tables);


        return new Body(spreadsheet);
    }

    private Table readTable(Node itemTable) {
        String tableName = itemTable.getAttributes().getNamedItem("table:name").getNodeValue();
        final NodeList childNodes = itemTable.getChildNodes();
        int nbCol = 0;
        List<List<Cell>> rows = new ArrayList<>();
        for (int index = 0; index < childNodes.getLength(); index++) {
            final Node item = childNodes.item(index);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getNodeName().equals("table:table-column")) {
                    final Node numberOfColumnsRepeated =
                            item.getAttributes().getNamedItem(TABLE_NUMBER_COLUMNS_REPEATED);
                    if (numberOfColumnsRepeated != null) {
                        nbCol += Integer.parseInt(numberOfColumnsRepeated.getNodeValue());
                    } else {
                        nbCol++;
                    }
                } else if (item.getNodeName().equals("table:table-row")) {
                    int nbRowRepeat = 1;
                    final Node numberRowRepeatedNode = item.getAttributes().getNamedItem("table:number-rows-repeated");
                    if (numberRowRepeatedNode != null) {
                        nbRowRepeat = Integer.parseInt(numberRowRepeatedNode.getNodeValue());
                    }
                    for (int nbRowRepeatIndex = 0; nbRowRepeatIndex < nbRowRepeat; nbRowRepeatIndex++) {
                        rows.add(readRow(item));
                    }
                } else {
                    LOGGER.warn(unused -> "Unknown item '{}'", item.getNodeName());
                }
            }
        }

        return new Table(tableName, nbCol, rows);
    }

    private List<Cell> readRow(Node itemRow) {
        final List<Cell> cells = new ArrayList<>();

        final NodeList childNodes = itemRow.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            final Node cellNode = childNodes.item(index);
            if (cellNode.getNodeType() == Node.ELEMENT_NODE) {

                if (!cellNode.hasChildNodes()) {
                    if (cellNode.hasAttributes()) {
                        final Node nbColumnsRepeated = cellNode.getAttributes().getNamedItem(
                                TABLE_NUMBER_COLUMNS_REPEATED);
                        if (nbColumnsRepeated != null) {
                            int nbEmptyCells = Integer.parseInt(nbColumnsRepeated.getNodeValue());
                            for (int i = 0; i < nbEmptyCells; i++) {
                                cells.add(EmptyCell.EMPTY_CELL);
                            }
                        } else {
                            cells.add(EmptyCell.EMPTY_CELL);
                        }
                    } else {
                        cells.add(EmptyCell.EMPTY_CELL);
                    }
                } else {
                    int nbRepeat = 1;
                    if (cellNode.hasAttributes()) {
                        final Node nbColumnsRepeated = cellNode.getAttributes().getNamedItem("table:number-columns" +
                                "-repeated");
                        if (nbColumnsRepeated != null) {
                            nbRepeat = Integer.parseInt(nbColumnsRepeated.getNodeValue());
                        }
                    }

                    final NodeList textNodeList = cellNode.getChildNodes();
                    int nbTextNodes = 0;
                    for (int textNodeIndex = 0; textNodeIndex < textNodeList.getLength(); textNodeIndex++) {
                        final Node textNode = textNodeList.item(textNodeIndex);
                        if (textNode.getNodeType() == Node.ELEMENT_NODE) {
                            nbTextNodes++;
                            if (nbTextNodes > 1) {
                                throw new TooManyTextItemException(textNodeList.getLength());
                            }
                            final Node formulaAttribute = cellNode.getAttributes().getNamedItem("table:formula");
                            String text = textNode.getFirstChild().getNodeValue();
                            for (int repeat = 0; repeat < nbRepeat; repeat++) {
                                if (formulaAttribute != null) {
                                    ValueType valueType = ValueType.valueOf(cellNode.getAttributes()
                                            .getNamedItem("office:value-type").getNodeValue().toUpperCase(Locale.ENGLISH));
                                    String formula = formulaAttribute.getNodeValue();
                                    cells.add(new FormulaCell(text, formula, valueType));
                                } else {
                                    cells.add(new TextCell(text));
                                }
                            }
                        }

                    }
                }
            }
        }

        return cells;
    }

    private CalculationSettings readCalculationSettings(Node itemCalculationSettings) {
        boolean automaticFindLabel = Boolean.parseBoolean(itemCalculationSettings.getAttributes()
                .getNamedItem("table:automatic-find-labels").getNodeValue());
        boolean useRegularExpressions = Boolean.parseBoolean(itemCalculationSettings.getAttributes()
                .getNamedItem("table:use-regular-expressions").getNodeValue());
        boolean useWildcards = Boolean.parseBoolean(itemCalculationSettings.getAttributes()
                .getNamedItem("table:use-wildcards").getNodeValue());

        return new CalculationSettings(automaticFindLabel, useRegularExpressions, useWildcards);
    }

    private Settings readSettings(NodeList nodeList) {
        // TODO implement settings read

        return new Settings();
    }

    private Metadata readMetadata(NodeList nodeList) {
        // TODO implement metadata

        return new Metadata();
    }


    public Metadata getMetadata() {
        return this.metadata;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public Body getBody() {
        return this.body;
    }
}
