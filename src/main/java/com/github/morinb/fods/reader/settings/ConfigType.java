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

package com.github.morinb.fods.reader.settings;

import com.github.morinb.fods.reader.exceptions.InvalidConfigTypeValueException;

public enum ConfigType {
    INT("int"),
    SHORT("short"),
    BOOLEAN("boolean"),
    STRING("string"),
    BASE64_BINARY("base64binary"),
    ;

    private final String label;

    private ConfigType(String label) {
        this.label = label;
    }

    public ConfigType fromValue(String value) {
        for (ConfigType configType : values()) {
            if (value.equals(configType.getLabel())) {
                return configType;
            }
        }
        throw new InvalidConfigTypeValueException(value);
    }

    public String getLabel() {
        return this.label;
    }
}
