/**
 * Created with IntelliJ IDEA.
 * User: amir
 * Date: 2014-04-13
 * Time: 17:51
 */
package com.yahoo.labs.samoa.sentinel.util;

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


import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.instances.SparseInstance;
import com.yahoo.labs.samoa.sentinel.sketch.Sketch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FilterTfIdf
{
    private ArrayList<String> stringList = new ArrayList<String>();

    protected Sketch frequentItemMiner;
    protected Sketch positiveFrequentItemMiner;
    protected Sketch negativeFrequentItemMiner;
    protected double numOfDocs = 0.0D;

    public FilterTfIdf(Sketch sketch)
    {
        this.frequentItemMiner = sketch;
    }

    public Instance filter(String s, InstancesHeader header)
    {
        numOfDocs++;

        String[] splitWords = s.split(",");
        String message = splitWords[0];
        String type = splitWords[1];

        message = message.replaceAll("'", "");
        String[] messageTokens = message.split(" ");

        Map<String, Integer> tokensInDoc = getStringIntegerMap(header, type, messageTokens);

        return getInstance(header, type, messageTokens, tokensInDoc);
    }

    private Instance getInstance(InstancesHeader header, String type, String[] messageTokens, Map<String, Integer> tokensInDoc)
    {
        double docSize = messageTokens.length;
        frequentItemMiner.addDoc(docSize);
        int numTokens = (int) tokensInDoc.size();
        double[] attValues = new double[numTokens];
        int[] indices = new int[numTokens];

        int tokenCounter = 0;
        for (Map.Entry<String, Integer> e : tokensInDoc.entrySet())
        {
            String token = e.getKey();
            double numInDoc = e.getValue();
            double docFreq = frequentItemMiner.getCount(token);
            double tf = numInDoc / docSize;
            double idf = Math.log10(this.numOfDocs / (docFreq + 1));
            int attIndex = frequentItemMiner.getAttIndex(token) + 1;
            indices[tokenCounter] = attIndex;
            attValues[tokenCounter] = (tf * idf);
            tokenCounter++;
        }

        Instance inst = new SparseInstance(1.0, attValues, indices, header.numAttributes());
        inst.setDataset(header);
        if (type.equals("S") || type.equals("H"))
        {
            //inst.setClassValue(type.equals("S") ? "S" : "H");
            inst.setClassValue(type.equals("S") ? 0 : 1);
        }
        else
        {
            //inst.setClassMissing();
            inst.setClassValue(2);

        }
        return inst;
    }

    private Map<String, Integer> getStringIntegerMap(InstancesHeader header, String type, String[] messageTokens)
    {
        Map<String, Integer> tokensInDoc = new HashMap<String, Integer>();

        for (String token : messageTokens)
        {
            if (!token.equals(" ") && !token.equals(""))
            {
                Integer freq = tokensInDoc.get(token.toLowerCase());
                tokensInDoc.put(token.toLowerCase(), (freq == null) ? 1 : freq + 1);
            }
        }

        for (Map.Entry<String, Integer> e : tokensInDoc.entrySet())
        {
            int oldAttIndex = frequentItemMiner.getAttIndex(e.getKey());
            frequentItemMiner.addToken(e.getKey(), e.getValue(), type.equals("S") ? 1 : 0);

            int newAttIndex = frequentItemMiner.getAttIndex(e.getKey());
            if (oldAttIndex == -1)
            {
                if (newAttIndex + 1 > header.numAttributes() - 1)
                {
                    Attribute newAtt = new Attribute(e.getKey());
                    //header.insertAttributeAt(newAtt, newAttIndex + 1);
                }
                else
                {
                    //header.renameAttribute(newAttIndex + 1, e.getKey());
                }
            }
        }
        return tokensInDoc;
    }

    public void printSketch()
    {
        this.frequentItemMiner.showNodes();
    }

    public double getFreqWord(String word)
    {
        return frequentItemMiner.getFreqWord(word);
    }

    public String[] getTopTokens(int n)
    {
        return this.frequentItemMiner.topTokens(n);
    }

    public String[] getTopPosTokens(int n)
    {
        return this.negativeFrequentItemMiner.topTokens(n);
    }

    public String[] getTopNegTokens(int n)
    {
        return this.frequentItemMiner.topTokens(n);
    }

}
