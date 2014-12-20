Compare-different-searching-algorithms---Information-Retrieval-
===============================================================

Testing the following algorithms

In this we will test the following algorithms
1. Vector Space Model 
2. BM25 
3. Language Model with Dirichlet Smoothing
4. Language Model with Jelinek Mercer Smoothing
 (set λ to 0.7)

We will need to compare the performance of those algorithms (and my search algorithm
implemented in other project) with the TREC topics. 
For each topic, there are two types of queries: short query (<title> field), and long query (<desc> field). So, for each search
method, we will need to generate two separate result files, i.e., for BM25, you will need
to generate BM25longQuery.txt and BM25shortQuery.txt
