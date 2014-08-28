/**
 * Created with IntelliJ IDEA.
 * User: amir
 * Date: 2014-08-27
 * Time: 11:58
 */
package com.yahoo.labs.samoa.sentinel.model;

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

public interface TwitterStreamReader
{
    public void initStream();
    public void filter(String[] query);
    public int size();
    public void setLanguage(String language);
    public String getAndRemove(int position);
    public void shutdown();
}
