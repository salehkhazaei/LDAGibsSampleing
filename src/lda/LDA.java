/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda;

/**
 *
 * @author Saleh
 */
public class LDA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LDAModel model = new LDAModel(5, 0.2, 0.1);
        for ( int i = 0 ; i < 100 ; i ++ )
        {
            long a = System.currentTimeMillis();
            System.out.println("Iteration [" + i + "]");
            model.iterate();
            System.out.println("Perplexity: " + model.perplexity() );
            System.out.println("Time: " + (double)(System.currentTimeMillis() - a) / 1000.0 + "s");
        }
    }
    
}
