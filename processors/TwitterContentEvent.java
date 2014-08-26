/**
 * Created with IntelliJ IDEA.
 * User: amir
 * Date: 2014-08-24
 * Time: 09:04
 */
package com.yahoo.labs.samoa.sentinel.processors;

import com.yahoo.labs.samoa.core.ContentEvent;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2013 - 2014 Yahoo! Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
public class TwitterContentEvent implements ContentEvent
{
    private static final long serialVersionUID = -2406968925730298156L;
    private final boolean isLastEvent;
    private final int helloWorldData;

    public TwitterContentEvent(int helloWorldData, boolean isLastEvent) {
        this.isLastEvent = isLastEvent;
        this.helloWorldData = helloWorldData;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setKey(String str) {
        // do nothing, it's key-less content event
    }

    @Override
    public boolean isLastEvent() {
        return isLastEvent;
    }

    public int getHelloWorldData() {
        return helloWorldData;
    }

    @Override
    public String toString() {
        return "HelloWorldContentEvent [helloWorldData=" + helloWorldData + "]";
    }
}
