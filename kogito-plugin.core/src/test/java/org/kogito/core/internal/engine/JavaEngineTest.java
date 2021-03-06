/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kogito.core.internal.engine;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

class JavaEngineTest {

    //    private final Logger logger = LoggerFactory.getLogger(JavaEngineTest.class);
    private Logger logger = Logger.getLogger(JavaEngineTest.class);

    @Test
    public void test() {
        JavaEngine javaEngine = new JavaEngine();
        TemplateParameters params = new TemplateParameters();
        String result = javaEngine.evaluate(Templates.TEMPLATE_CLASS, params);
        logger.info(result);
    }
}