/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import lda.Document.WordTopic;

/**
 *
 * @author Saleh
 */
public class LDAModel {

    // model parameters
    public int T;
    public double Beta;
    public double Alpha;

    public int W;

    public ArrayList<Document> documents = new ArrayList<>();
    public WordTopic wordPointer[];
    public int wordTopics[];
    public String vocabs[];
    public ArrayList<int[]> samples = new ArrayList<>();

    public int Nwij[][]; // no of times that word "w" has been assigned to assigned to j-th topic (without Wi)
    public int Nij[]; // no of words which have been assigned to j-th topic
    public int Ndij[][]; // no of words of i-th document which has been assigned to j-th topic
    public int Ndi[]; // no of words of i-th document

    public int wordCount = 0;

    public double phi[][];
    public double teta[][];

    public LDAModel(int T, double Beta, double Alpha) {
        this.T = T;
        this.Beta = Beta;
        this.Alpha = Alpha;
        try {
            initiate();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LDAModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initiate() throws FileNotFoundException {
        // Read the dataset file
        W = 10473;
        vocabs = new String[W];
        Scanner sc = new Scanner(new File("ap/vocab.txt"));
        for (int i = 0; i < W; i++) {
            vocabs[i] = sc.nextLine();
        }
        sc = new Scanner(new File("ap/ap.dat"));

        wordPointer = new WordTopic[1000];

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.length() > 0) {
                HashMap<Integer, Integer> occurance = new HashMap<>();
                Scanner sc2 = new Scanner(line);
                int numberOfWords = sc2.nextInt();
                for (int i = 0; i < numberOfWords; i++) {
                    String[] strWord = sc2.next().split(":");
                    int wordIndex = Integer.parseInt(strWord[0]);
                    int ocu = Integer.parseInt(strWord[1]);
                    occurance.put(wordIndex, ocu);
                }
                Document doc = new Document(occurance, this);
                documents.add(doc);
            }
        }

        Nwij = new int[vocabs.length][T]; // no of times that word "w" has been assigned to assigned to j-th topic (without Wi)
        Nij = new int[T]; // no of words which have been assigned to j-th topic
        Ndij = new int[documents.size()][T]; // no of words of i-th document which has been assigned to j-th topic
        Ndi = new int[documents.size()]; // no of words of i-th document

        // fill the topic array randomly
        Random rand = new Random();
        wordTopics = new int[wordCount];

        for (int i = 0; i < wordTopics.length; i++) {
            wordTopics[i] = rand.nextInt(T);
            Nwij[wordPointer[i].word][wordTopics[i]]++;
            Nij[wordTopics[i]]++;
            Ndij[documents.indexOf(wordPointer[i].parent)][wordTopics[i]]++;
        }
        for (int i = 0; i < documents.size(); i++) {
            Ndi[i] = documents.get(i).words.size();
        }
    }

    public void iterate() {
        // update topic of each word
        for (int i = 0; i < wordTopics.length; i++) {
            // remove old topic
            int docid = documents.indexOf(wordPointer[i].parent);
            Nwij[wordPointer[i].word][wordTopics[i]]--;
            Nij[wordTopics[i]]--;
            Ndij[docid][wordTopics[i]]--;
            Ndi[docid]--;

            // compute probability of each topic 
            int topic = 0;
            BigDecimal max_p = new BigDecimal(0);
            for (int j = 0; j < T; j++) {
                BigDecimal a = new BigDecimal(Nwij[wordPointer[i].word][j] + Beta);
                BigDecimal b = new BigDecimal(Nij[j] + vocabs.length * Beta);
                BigDecimal c = new BigDecimal(Ndij[docid][j] + Alpha);
                BigDecimal d = new BigDecimal(Ndi[docid] + T * Alpha);

                a = a.divide(b, 20, RoundingMode.HALF_UP);
                c = c.divide(d, 20, RoundingMode.HALF_UP);
                a = a.multiply(c);

                if (a.compareTo(max_p) > 0) {
                    topic = j;
                    max_p = a;
                }
            }

            // add new topic
            //System.out.println("Topic[" + i + "] " + wordTopics[i] + " -> " + topic);
            wordTopics[i] = topic;
            Nwij[wordPointer[i].word][wordTopics[i]]++;
            Nij[wordTopics[i]]++;
            Ndij[docid][wordTopics[i]]++;
            Ndi[docid]++;
        }
        int sample[] = new int[wordTopics.length];
        System.arraycopy(wordTopics, 0, sample, 0, wordTopics.length);
        samples.add(sample);
    }

    public void updateParameters() {
        phi = new double[vocabs.length][T];
        teta = new double[documents.size()][T];

        for (int i = 0; i < vocabs.length; i++) {
            for (int j = 0; j < T; j++) {
                BigDecimal a = new BigDecimal(Nwij[wordPointer[i].word][j] + Beta);
                BigDecimal b = new BigDecimal(Nij[j] + vocabs.length * Beta);

                a = a.divide(b, 3, RoundingMode.HALF_UP);
                phi[i][j] = a.doubleValue();
            }
        }

        for (int i = 0; i < documents.size(); i++) {
            for (int j = 0; j < T; j++) {
                BigDecimal c = new BigDecimal(Ndij[i][j] + Alpha);
                BigDecimal d = new BigDecimal(Ndi[i] + T * Alpha);

                c = c.divide(d, 3, RoundingMode.HALF_UP);
                teta[i][j] = c.doubleValue();
            }
        }
    }

    public double perplexity() {
        double sumLog = 0;
        for (int i = 0; i < wordCount; ++i) {
            int word = wordPointer[i].word;
            int doc = documents.indexOf(wordPointer[i].parent);
            int t = wordTopics[i];
            double prob = ((Ndij[doc][t] + 1) / (double) (Ndi[doc] + Alpha)) * ((Nwij[word][t] + 1) / (double) (Nij[t] + Beta));
            sumLog += Math.log(prob);
        }
        sumLog = 0 - (sumLog / (double) wordCount);
        double perplexity = Math.exp(sumLog);
        return perplexity;
    }
}
