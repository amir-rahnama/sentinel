package com.yahoo.labs.samoa.sentinel.sketch;

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

import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.IntOption;
import moa.tasks.TaskMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpaceSaving extends AbstractOptionHandler implements Sketch
{
    private static final long serialVersionUID = 1L;

    protected int _top = 0;
    protected Map<String, Node> _map = null;
    protected ArrayList<Node> _nodes = null;
    protected int numDoc = 0;
    protected double numTerms = 0;

    public IntOption capacityOption = new IntOption("capacity", 'c', "Number of attributes to use", 10000);

    protected Node newNode(String token, int index, int freq) {
        return new Node(token, index, freq);
    }

    @Override
    public int addToken(String token, int freq) {
        Node node = _map.get(token);
        if (node == null) {
            if (_top < this.capacityOption.getValue()) {
                push(token, freq);
            } else {
                popFirstNodePushNewToken(token, freq);
            }
        } else {
            addCount(node, freq);
            updatePosition(node);
        }

        return _map.get(token).attrIndex;
    }

    @Override
    public int addToken(String token, int freq, int classIndex) {
        return addToken(token, freq);
    }

    @Override
    public void showNodes() {
        for (Node node : _nodes) {
            if (node != null) {
                System.out.println(node);
            }
        }
    }

    @Override
    public void addDoc(double docSize) {
        this.numDoc++;
        this.numTerms += docSize;
    }

    @Override
    public double getCount(String token) {
        Node node = _map.get(token);
        if (node == null) {
            return 0.0;
        } else {
            return node.getCount();
        }
    }

    @Override
    public int getAttIndex(String token) {
        Node node = _map.get(token);
        if (node != null) {
            return node.attrIndex;
        } else {
            return -1;
        }
    }

    @Override
    public double getFreqWord(String word) {
        return getCount(word) / this.numTerms;
    }

    @Override
    public void remove(String token)
    {  }

    @Override
    public String[] topTokens(int paramInt)
    {
        return new String[0];
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository)
    {
        _top = 0;
        _map = new HashMap<String, Node>();
        _nodes = new ArrayList<Node>();
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {    }

    protected void push(String token, int freq) {
        Node node = newNode(token, _top, freq);
        _nodes.add(node);
        _map.put(token, node);
        updatePosition(node);
        _top++;
    }

    protected void popFirstNodePushNewToken(String token, int freq) {
        Node node = _nodes.get(0);
        _map.remove(node.token);
        _map.put(token, node);
        node.token = token;
        addCount(node, freq);
        updatePosition(node);
    }

    protected boolean addCount(Node node, int freq) {
        node.addCount((double) freq, numDoc);
        return false;
    }

    protected void updatePosition(Node node) {
        boolean isDescending = false;

        int offset = node.index + 1;
        while ((offset < _nodes.size()) && (node.getCount() > _nodes.get(offset).getCount())) {
            offset++;
        }
        offset--;

        //Descend in the list order if count was reduced
        if (offset == node.index) {
            offset = node.index - 1;
            while ((offset >= 0) && (node.count < _nodes.get(offset).getCount())) {
                offset--;
                isDescending = true;
            }
            offset++;
        }

        if (offset != node.index) {
            int oldIndex = node.index;

            if (isDescending) {
                _nodes.add(offset, node);
                _nodes.remove(oldIndex + 1);
            } else {
                _nodes.add(offset + 1, node);
                _nodes.remove(oldIndex);
            }
        }
        if (_nodes != null) {
            for (int i = 0; i < _nodes.size(); i++) {
                _nodes.get(i).index = i;
            }
        }
    }


    static class Node
    {
        private static final long serialVersionUID = 1L;

        int index;
        int attrIndex;
        double count;
        String token;

        public Node() {
        }

        Node(String token, int index, int freq) {
            this.index = index;
            this.attrIndex = index;
            this.token = token;
            initCount(freq);
        }

        protected void initCount(int freq) {
            this.count = freq;
        }

        protected double getCount() {
            return this.count;
        }

        protected boolean addCount(double freq, int doc) {
            this.count += freq;
            return false;
        }

        public int compareTo(Node other) {
            if (getCount() < other.getCount()) {
                return -1;
            }
            if (getCount() > other.getCount()) {
                return 1;
            }
            return 0;
        }

        protected String getToken() {
            return this.token;
        }

        @Override
        public String toString() {
            return "" + index + "," + attrIndex + "," + count + "," + token;
        }
    }
}
