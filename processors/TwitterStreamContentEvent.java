package com.yahoo.labs.samoa.sentinel.processors;

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

import com.yahoo.labs.samoa.core.ContentEvent;
import weka.core.Instance;

public class TwitterStreamContentEvent implements ContentEvent
{
    private static final long serialVersionUID = -2406968925730298156L;
    private final boolean isLastEvent;
    private Instance tweet = null;

    public TwitterStreamContentEvent(Instance tweet, boolean isLastEvent)
    {
        this.isLastEvent = isLastEvent;
        this.tweet = tweet;
    }

    @Override
    public String getKey()
    {
        return null;
    }

    @Override
    public void setKey(String str)
    {
        // do nothing, it's key-less content event
    }

    @Override
    public boolean isLastEvent()
    {
        return isLastEvent;
    }

    public Instance getTweetInstace() {return tweet;}

    @Override
    public String toString()
    {
        return "HelloWorldContentEvent [helloWorldData=" + tweet + "]";
    }
}

