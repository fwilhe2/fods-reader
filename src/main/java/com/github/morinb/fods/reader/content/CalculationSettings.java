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

public class CalculationSettings {
    private final boolean automaticFindLabels;
    private final boolean useRegularExpressions;
    private final boolean useWildcards;

    public CalculationSettings(boolean automaticFindLabels, boolean useRegularExpressions, boolean useWildcards) {
        this.automaticFindLabels = automaticFindLabels;
        this.useRegularExpressions = useRegularExpressions;
        this.useWildcards = useWildcards;
    }

    public boolean isAutomaticFindLabels() {
        return this.automaticFindLabels;
    }

    public boolean isUseRegularExpressions() {
        return this.useRegularExpressions;
    }

    public boolean isUseWildcards() {
        return this.useWildcards;
    }
}
