// Copyright (C) 2015  Julián Urbano <urbano.julian@gmail.com>
// Distributed under the terms of the MIT License.

	/////////////////FROM HOMEWORK NOTES///////////////////////////////////
	//You will basically need to code into the class Cosine, that uses the
	//information from Index. For each query you will run the method:
	
		//// ArrayList<Tuple<Integer, Double>> runQuery(String queryText,Index index,DocumentProcessor docProcessor)
	
	//This method returns tuples(docID, similarity).
	//Internally, first it extracts the terms of the query, and then call the
	//method:
	
	 	////ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index) 
	
	//This method receives a list of terms and returns the query vector, represented as tuples(termID, weight).
	
	//Then, it calls the method: 
	//ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector,Index index)

	
	//That returns the similarity between the indexed documents and the query
	//vector.


package ti;

import java.util.*;
import javax.management.remote.*;



/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation.
 */
public class Cosine implements RetrievalModel
{
	public Cosine()
	{
		// was empty  --- code CacheRequest
		
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
		// P1//////OUR CODE FROM HERE/////////////////////
		//Note- This runQuery method returns tuples(docID, similarity).
		System.err.println(queryText);
		
		// Extract the terms from the query
		ArrayList <String> myStringArr;
		//UPDATE  docProcessor to HTMLProcessor....
		//the simple processor is just removing words less than 4 chars.
		myStringArr = docProcessor.processText(queryText);
		System.err.println("in Run query- Processed text:" + myStringArr);
		
		
		// Calculate the query vector - This method receives a list of terms and returns the query vector, 
		//represented as tuples(termID, weight).
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();
		vector = computeVector(myStringArr, index);

		// Calculate the document similarity 
		//this method returns the similarity between the indexed documents and the query vector.
		ArrayList<Tuple<Integer, Double>> scores = new ArrayList<>();
		scores = computeScores(vector, index);
		
		////////////////END OUR CODE/////////////////

		return scores; // return results
	}


	protected ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector, Index index)
		{
			ArrayList<Tuple<Integer, Double>> results;
			HashMap<Integer, Tuple<Integer, Double>> checkDocs = new HashMap<>();
			double queryWeight = 0, sumNum = 0, termWeight= 0, termNorm = 0, queryNorm = 0;
			queryNorm = calculateNormQuery(queryVector);

			//Iterate over terms in query
			for (Tuple<Integer, Double> term: queryVector) {
				int termID = term.item1;
				queryWeight = term.item2;
				//using invertedIndex we can determine which documents contains the term of the queries.
				//Each posting is a Tuple containing a docID and the weight of the term in that document.
				ArrayList<Tuple<Integer, Double>> ListDoc = index.invertedIndex.get(termID);
				System.err.println("List of docs with score" +ListDoc);
				//Iterate over docs associated with terms and calculate de similarity score
				for (Tuple<Integer, Double> docs : ListDoc) {
					termWeight = docs.item2;
					sumNum = termWeight * queryWeight;
					termNorm  = index.documents.get(docs.item1).item2;

					//Check if the doc has already been computated
					if (checkDocs.get(docs.item1) != null){
						//update acc similarity of doc already visited
						checkDocs.get(docs.item1).item2 += sumNum/(termNorm * queryNorm);

					}else{ //New doc in results
						checkDocs.put(docs.item1, new Tuple(docs.item1,sumNum/(termNorm * queryNorm)));
					}

				}

			}

			//Transform hash into an arraylist
			results = new ArrayList(checkDocs.values());

			// Sort documents by similarity and return the ranking
			Collections.sort(results, (o1,o2) -> o2.item2.compareTo(o1.item2));			return results;
		}

		
		protected double calculateNormQuery (ArrayList<Tuple<Integer, Double>> queryVector){
				double queryWeight = 0, queryNorms = 0;
				for (Tuple<Integer, Double> term: queryVector) {
					queryWeight = term.item2;
					queryNorms += Math.pow(queryWeight,2.0);
				}
				return Math.sqrt(queryNorms);
			}

	/**
	 * Returns the list of documents in the specified index sorted by similarity with the specified query vector.
	 *
	 * @param queryVector the vector with query term weights.
	 * @param index       the index to search in.
	 * @return a list of {@link Tuple}s where the first item is the {@code docID} and the second one the similarity score.
	 */
	/*protected ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector, Index index)
	{
		ArrayList<Tuple<Integer, Double>> results = new ArrayList<>();
		System.err.println("ComputeScores - QueryVector" +queryVector);

		//P1 ////////////OUR CODE IS HERE//////////////////////
		//This method Returns the list of documents in the specified index sorted by similarity with the specified query vector "queryVector" which has been returned from method computeVector().
		double freq_of_term_in_doc, nun_docs, num_docs_containing_term, tf_term_in_doc, id_freq_of_term;
		
	//		id_freq_of_term = Math.log(1+ nun_docs/num_docs_containing_term);

		//The similarity is calculated using the cosine similarity:

		double[] vectorA, vectorB;
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		double similarity;
		
		//cosine similarity formula
//		for (int i = 0; i < vectorA.length; i++) {
//			dotProduct += vectorA[i] * vectorB[i];
//			normA += Math.pow(vectorA[i], 2);
//			normB += Math.pow(vectorB[i], 2);
//		}
//		similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
		///////////////////END OF OUR CODE///////////////////
		// Sort documents by similarity and return the ranking
		Collections.sort(results, new Comparator<Tuple<Integer, Double>>()
		{
			@Override
			public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2)
			{
				return o2.item2.compareTo(o1.item2);
			}
		});
		return results;
	}*/
	
	

	/**
	 * Compute the vector of weights for the specified list of terms.
	 *
	 * @param terms the list of terms.
	 * @param index the index
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the weight as second one.
		This method receives a list of terms and returns the query vector, represented as tuples(termID, weight)
	 */
	protected ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();
		///P1////OUR CODE STARTS HERE/////////////
		//Another used structure is HashMap<K,V>fromJava, which maps keys of type K to values
		//of type V(they can be seen as tuplesTuple<K,V>). It is a very efficient data structure
		//with access costO(1), so they are used to know which value corresponds to a specific
		//key. note index.vocabulary is a hashMap
		//https://www.javatpoint.com/java-arraylist
		/////////////////////////////////////////////
		
		//turn terms into a hashlist
		HashSet<String> uniqueTerms = new HashSet<>(terms);
		Tuple<Integer, Double> tid_idf;
		Tuple<Integer, Double> tid_weight;
		Double tf_term_in_doc, weights_TFxIDF;
		
		//freq_of_term_in_doc = is the frequence of the term in the document. 
		//num_docs  = the number of documents; 
		//num_docs_containing_term  = the number of documents containing the term. 
		//The weights equation followsTFxIDF:
		//Therefore, the weights are formulated as Wtd = tFtd * idFt

		for(String term : uniqueTerms)
		{
			//vocabulary takes [term] and returns (termID, IDF)
			//so basically it maps one term to one tuple
			int freq_of_term_in_doc = Collections.frequency(terms, term);
			tid_idf = index.vocabulary.get(term);
			//The weights equation follows TFxIDF:
			//Therefore, the weights are formulated as Wtd = tFtd * idFt
			tf_term_in_doc = 1 + Math.log(freq_of_term_in_doc);					
			weights_TFxIDF = tf_term_in_doc * tid_idf.item2;			
			tid_weight = new Tuple(tid_idf.item1, weights_TFxIDF);
			vector.add(tid_weight);		
		}			
		//Note: This method receives a list of terms and should returns 
		//the query vector, represented as tuples(termID, weight)			
		return vector;
		////OUR CODE ENDS HERE/////////////
	}
}



