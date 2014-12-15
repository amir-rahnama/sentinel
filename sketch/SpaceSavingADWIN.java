package com.yahoo.labs.samoa.sentinel.sketch;

import moa.classifiers.core.driftdetection.ADWIN;

public class SpaceSavingADWIN extends SpaceSaving
{
/*
*    SpaceSavingAdwin.java
*    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
*    @author Bernhard Pfahringer (bernhard at cs dot waikato dot ac dot nz)
*    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program; if not, write to the Free Software
*    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

    protected int numberOfChanges = 0;

    protected String textChanges = "";

    @Override
    protected Node newNode(String token, int index, int freq) {
        return new NodeAdwin(token, index, freq);
    }

    @Override
    public void addDoc(double docSize) {
        //Add zeros
        //for (Node n : _nodes) {
        for (int i = 0;i < _nodes.size(); i++) {
            Node n = _nodes.get(i);
            if (n != null) {
                if (((NodeAdwin) n).getLastDoc() != this.numDoc) {
                    double oldFreq = n.getCount();
                    boolean change = n.addCount(0, this.numDoc);
                    if (change) {
                        this.numberOfChanges++;
                        this.textChanges += n.getToken() + "," + oldFreq + "," + n.getCount() + " \n";
                    }
                    updatePosition(n);
                }
            }
        }
        if (this.numberOfChanges > 0.05 * _nodes.size()) {
            this.textChanges = "";
            this.numberOfChanges = 0;
        }
        this.numDoc++;
        this.numTerms += docSize;


    }

    @Override
    protected boolean addCount(Node n, int freq) {
        double oldFreq = n.getCount();
        boolean change = n.addCount((double) freq, this.numDoc);
        if (change) {
            this.numberOfChanges++;
            this.textChanges += n.getToken() + "," + oldFreq + "," + n.getCount() + " \n";
        }
        return change;
    }

    static class NodeAdwin extends Node {

        private static final long serialVersionUID = 1L;

        protected int lastDoc;

        protected ADWIN adwinCounter;

        public int getLastDoc() {
            return lastDoc;
        }

        NodeAdwin(String token, int index, int freq) {
            super(token, index, freq);
        }

        @Override
        protected boolean addCount(double freq, int doc) {
            boolean ret = this.adwinCounter.setInput(freq);
            this.lastDoc = doc;
            return ret;
        }

        @Override
        protected void initCount(int freq) {
            this.adwinCounter = new ADWIN();
            this.addCount(freq, 0); //?
        }

        @Override
        protected double getCount() {
            return this.adwinCounter != null ? this.adwinCounter.getEstimation() : 0.0;
        }

        public int compareTo(NodeAdwin other) {
            if (this.adwinCounter.getEstimation() < other.adwinCounter.getEstimation()) {
                return -1;
            }
            if (this.adwinCounter.getEstimation() > other.adwinCounter.getEstimation()) {
                return 1;
            }
            return 0;
        }

        @Override
        public String toString() {
            return "<Node " + index + " " + attrIndex + " " + token + " " + count + " >"; // "/" + min +
        }
    }

    @Override
    public double getFreqWord(String word) {
        return getCount(word) / (this.numTerms / (double) this.numDoc);
    }
}
