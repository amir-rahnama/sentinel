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

import com.cybozu.labs.langdetect.Language;

import java.util.ArrayList;

public class Tweet
{
    private ArrayList<Language> detect;

    protected Double minimumProb = 0.95;	//The min probability required for predicted language

    private String originalMessage;
    private String emotionType = "N";
    private String cleanedMessage;

    private String language;
    private double languageProbability;
    protected final Double minimumProbabilityThreshold = 0.95;


    public ArrayList<Language> getDetect()
    {
        return detect;
    }

    public void setDetect(ArrayList<Language> detect)
    {
        this.detect = detect;
    }

    public Double getMinimumProb()
    {
        return minimumProb;
    }

    public void setMinimumProb(Double minimumProb)
    {
        this.minimumProb = minimumProb;
    }

    public String getOriginalMessage()
    {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage)
    {
        this.originalMessage = originalMessage;
    }

    public String getEmotionType()
    {
        return emotionType;
    }

    public void setEmotionType(String emotionType)
    {
        this.emotionType = emotionType;
    }

    public String getCleanedMessage()
    {
        return cleanedMessage;
    }

    public void setCleanedMessage(String cleanedMessage)
    {
        this.cleanedMessage = cleanedMessage;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public double getLanguageProbability()
    {
        return languageProbability;
    }

    public void setLanguageProbability(double languageProbability)
    {
        this.languageProbability = languageProbability;
    }

    public Double getMinimumProbabilityThreshold()
    {
        return minimumProbabilityThreshold;
    }


    public Tweet() {
    }
}
