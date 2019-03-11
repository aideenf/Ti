// Copyright (C) 2015  Julián Urbano <urbano.julian@gmail.com>
// Distributed under the terms of the MIT License.
//The classSearchEngineis the entrance point to the program. It justchecksthe input parameters and delegate the execution to the classesIndexer, BatcheInteractive, the ones in charge of running the search engine. The classIndexcontains the data structures of the index. An index is created byIndexer, and it will be used for retrieval fromBatchandInteractive.Also, the interfaceDocumentProcessordefine the basic operations of a document processor for extracting the terms that will be included in the index. For this purpose, during the first sessions, you can use the classSimpleProcessorbut  it  will  be  later  replaced  byHtmlProcessorto  process  HTML  pages  (Part  2  of  the  lab).The  interfaceRetrievalModeldefines basic operations for a retrieval model. ForP1 you should implement the class Cosine.All  the  code  goes  with  comments  and  we  also  provide  the  javadoc. Please  read  carefully  the  documentation  to understand how everything should work

package ti;

import java.io.File;
//import org.jsoup.Jsoup;
//jsoup-1.11.3.jar

/**
 * This class is the main entry point to run the search engine.
 * It contains the {@link #main} method.
 */
public class SearchEngine
{
    /**
     * Run the indexing process with the given command-line arguments.
     *
     * @param args the raw command-line arguments.
     * @throws Exception if an error occurs during the process.
     */
    protected static void doIndex(String[] args) throws Exception
    {
        System.err.println("in do Index with" + args[1]);
        System.err.println("in do Index with" + args[2]);
        System.err.println("in do Index with" + args[3]);

        if (args.length < 3 || args.length > 4) {
            SearchEngine.printUsage();
            System.exit(1);
        }
        File pathToIndex = new File(args[1]);
        File pathToCollection = new File(args[2]);
        File pathToStopWords = args.length == 4 ? new File(args[3]) : null;

        // Check console arguments
        if (pathToIndex.exists() && pathToIndex.isFile()) {
            System.err.println("The index path must be a directory.");
            System.exit(1);
        }
        if (!pathToCollection.exists() || pathToCollection.isFile()) {
            System.err.println("Invalid path to document collection.");
            System.exit(1);
        }
        if (pathToStopWords != null && (!pathToStopWords.exists() || !pathToStopWords.isFile())) {
            System.err.println("Invalid path to list of stop words.");
            System.exit(1);
        }

        // Build index
        DocumentProcessor docProcessor = new SimpleProcessor(); // P3
        Indexer indexer = new Indexer(pathToIndex, pathToCollection, docProcessor);
        indexer.run();
    }

    /**
     * Run the retrieval process in batch mode with the given command-line arguments.
     *
     * @param args the raw command-line arguments.
     * @throws Exception if an error occurs during the process.
     */
    protected static void doBatch(String[] args) throws Exception
    {
        
        System.err.println("in doBatch");
        System.err.println(args.length);
        System.err.println(args[0]);
        System.err.println(args[1]);
        System.err.println(args[2]);
        if (args.length != 3) {
            SearchEngine.printUsage();
            System.exit(1);
        }
        File pathToIndex = new File(args[1]);
        File pathToQueries = new File(args[2]);

        // Check console arguments
        if (!pathToIndex.exists() || pathToIndex.isFile()) {
            System.err.println("Index directory does not exist.");
            System.exit(1);
        }
        if (!pathToQueries.exists() || !pathToQueries.isFile()) {
            System.err.println("Query file does not exist.");
            System.exit(1);
        }

        // Read index
        System.err.print("Loading index...");
        Index ind = new Index(pathToIndex.getPath());
        ind.load();
        System.err.println("done. Statistics:");
        ind.printStatistics();

        // Instantiate retriever and run
        DocumentProcessor docProcessor = new SimpleProcessor(); // P3
        
        //You will basically need to code into the class Cosine, that uses the information from Index. For each query you will run the method:ArrayList<Tuple<Integer, Double>> runQuery(String queryText,Index index,DocumentProcessor docProcessor)This method returns tuples(docID, similarity).Internally, first it extracts thetermsof the query, and then call the method:ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
        //This method receives a list oftermsand returns the query vector, represented as tuples(termID, weight).Then, it calls the method: ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector,Index index)That returns thesimilaritybetween the indexeddocuments and the query vector.
        RetrievalModel cosine = new Cosine(); // P4
        Batch batch = new Batch(pathToQueries, cosine, ind, docProcessor);
        batch.run();
    }

    /**
     * Run the retrieval process in interactive mode with the given command-line arguments.
     *
     * @param args the raw command-line arguments.
     * @throws Exception if an error occurs during the process.
     */
    protected static void doInteractive(String[] args) throws Exception
    {
        if (args.length != 2) {
            SearchEngine.printUsage();
            System.exit(1);
        }
        File pathToIndex = new File(args[1]);

        // Check console arguments
        if (!pathToIndex.exists() || pathToIndex.isFile()) {
            System.err.println("Index directory does not exist.");
            System.exit(1);
        }

        // Read index
        System.err.print("Loading index...");
        Index ind = new Index(pathToIndex.getPath());
        ind.load();
        System.err.println("done. Statistics:");
        ind.printStatistics();

        // Instantiate retriever and run
        DocumentProcessor docProcessor = new SimpleProcessor(); // P3
        RetrievalModel cosine = new Cosine(); // P4
        Interactive inter = new Interactive(cosine, ind, docProcessor);
        inter.run();
    }

    public static void main(String[] args) throws Exception
    {
        System.err.println("in main");
        String html = "<html><head><title>First parse</title></head>"
            + "<body><p>Parsed HTML into a doc.</p></body></html>";
       // Document doc = Jsoup.parse(html);

        
        
        if (args.length < 1) {
            SearchEngine.printUsage();
            System.exit(1);
        }
        switch (args[0].toLowerCase()) {
            //The class Index contains the data structures of the index.
            // An index is created byIndexer, and it will be used for retrieval
            //fromBatchandInteractive.
            case "index":
                System.err.println("in Index case");
                SearchEngine.doIndex(args);
                break;
            case "batch":
                System.err.println("in Batch");
                System.err.println(args[0]);
                System.err.println(args[1]);
                System.err.println(args[2]);
                SearchEngine.doBatch(args);
                break;
            case "interactive":
                SearchEngine.doInteractive(args);
                break;
            default:
                SearchEngine.printUsage();
                System.exit(1);
        }
    }

    protected static void printUsage()
    {
        System.err.println("Usage: ti.SearchEngine <command> <options>");
        System.err.println();
        System.err.println("where <command> and <options> are one of:");
        System.err.println("  - index <path-to-index> <path-to-collection> [<path-to-stopwords>]");
        System.err.println("  - batch <path-to-index> <path-to-queries>");
        System.err.println("  - interactive <path-to-index>");
    }
}