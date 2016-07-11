/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Saleh
 */
public class Document {

    public HashMap<Integer, Integer> occurance = new HashMap<>();
    public ArrayList<WordTopic> words = new ArrayList<>();

    public Document(ArrayList<Integer> words, LDAModel model) {
        for (Integer w : words) {
            WordTopic wt = new WordTopic(w, model.wordCount, this);
            this.words.add(wt);
            model.wordPointer[model.wordCount] = wt;
            model.wordCount++;
            if (model.wordCount >= model.wordPointer.length) {
                WordTopic[] p = model.wordPointer;
                model.wordPointer = new WordTopic[p.length + 1000];
                System.arraycopy(p, 0, model.wordPointer, 0, p.length);
            }
            this.occurance.put(w, (this.occurance.containsKey(w) ? this.occurance.get(w) + 1 : 1));
        }
    }

    public Document(HashMap<Integer, Integer> occurance, LDAModel model) {
        for (Integer w : occurance.keySet()) {
            this.occurance.put(w, occurance.get(w));
            for (int i = 0; i < occurance.get(w); i++) {
                WordTopic wt = new WordTopic(w, model.wordCount, this);
                this.words.add(wt);
                model.wordPointer[model.wordCount] = wt;
                model.wordCount++;
                if (model.wordCount >= model.wordPointer.length) {
                    WordTopic[] p = model.wordPointer;
                    model.wordPointer = new WordTopic[p.length + 1000];
                    System.arraycopy(p, 0, model.wordPointer, 0, p.length);
                }
            }
        }
    }

    public class WordTopic {

        public Document parent;
        public Integer word;
        public int indexInTopic;

        public WordTopic(Integer word, int indexInTopic, Document parent) {
            this.word = word;
            this.indexInTopic = indexInTopic;
            this.parent = parent;
        }
    }
}
