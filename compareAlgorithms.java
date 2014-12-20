import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

public class compareAlgorithms 
{
	static String extract(StringBuilder buf, String startTag, String endTag)
	{
		String stringBetweenTags = new String();
		int k1 = buf.indexOf(startTag);
		while(k1 > 0)    
		{
		   k1 += startTag.length();
		   int k2 = buf.indexOf(endTag,k1);
		      
		   if (k2>=0)
		   {
			   stringBetweenTags +=(" " + buf.substring(k1,k2).trim());  
		   }
		   
		   k1 = buf.indexOf(startTag, k2);
		}
		return stringBetweenTags;	  
	}
	
	static String readFile(String file) throws IOException 
	{
		FileReader fileReader = new FileReader (file);
		BufferedReader reader = new BufferedReader(fileReader);
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while((line = reader.readLine()) != null ) 
	    {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }

	    return stringBuilder.toString();
	}
	
	static void collectQuery(String filePath, List<String> shortQuery,List<String> longQuery) throws IOException
	{
		String readBuffer = readFile(filePath);
		StringBuilder builder = new StringBuilder(readBuffer);
		
		String startDocTag = "<top>";
		String endDocTag = "</top>";
		int docStart = builder.indexOf(startDocTag);
		while(docStart != -1)    
		{
		   docStart += startDocTag.length();
		   int docEnd = builder.indexOf(endDocTag,docStart);
		   
		   if(docEnd > 0)
		   {
			   StringBuilder document = new StringBuilder(builder.substring(docStart,docEnd).trim());
			   String doctitle = extract(document,"<title>", "<desc>");
			   String dateDesc = extract(document,"<desc>", "<smry>");
			   shortQuery.add(doctitle);
			   longQuery.add(dateDesc);
		   }
		   docStart = builder.indexOf(startDocTag, docEnd);
		}
	}
	
	static void parseQuery(String indexpath, List<String> queries, String outputFilePath,Boolean lon) throws IOException, ParseException
	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexpath)));
		IndexSearcher searcher = new IndexSearcher(reader);
				
		FileWriter fileWriter = null;
				
		for(int type = 0; type < 4 ; ++type)
		{
			switch(type)
			{
			case 0:
				searcher.setSimilarity(new BM25Similarity());
				if(!lon)
					fileWriter = new FileWriter(outputFilePath+"//BM25shortQuery.txt");
				else
					fileWriter = new FileWriter(outputFilePath+"//BM25longQuery.txt");
				break;
			case 1:
				searcher.setSimilarity(new DefaultSimilarity());
				if(!lon)
					fileWriter = new FileWriter(outputFilePath+"//VectorSpaceModelshortQuery.txt");
				else
					fileWriter = new FileWriter(outputFilePath+"//VectorSpaceModellongQuery.txt");
				break;
			case 2:
				searcher.setSimilarity(new LMDirichletSimilarity());
				if(!lon)
					fileWriter = new FileWriter(outputFilePath+"//LMDirichletshortQuery.txt");
				else
					fileWriter = new FileWriter(outputFilePath+"//LMDirichletlongQuery.txt");
				break;
			case 3:
				float lambda  = (float) 0.7;
				searcher.setSimilarity(new LMJelinekMercerSimilarity(lambda));
				if(!lon)
					fileWriter = new FileWriter(outputFilePath+"//LMJelinekMercershortQuery.txt");
				else
					fileWriter = new FileWriter(outputFilePath+"//LMJelinekMercerlongQuery.txt");
				break;
			};
			//queries.size()
			for(int queryID = 0 ; queryID < queries.size() ; ++queryID)
			{			
				Analyzer analyzer = new StandardAnalyzer();
				
				QueryParser parser = new QueryParser("TEXT", analyzer);
				
				Query query = parser.parse(queries.get(queryID).replace("Topic:", "").replace("/", "").replace("\\", ""));
				TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
				searcher.search(query, collector);
				
				//System.out.println("Collection of Ranked Document(short Query) for "+searcher.getSimilarity());
				ScoreDoc[] docs = collector.topDocs().scoreDocs;
				for (int i = 0; i < docs.length; i++) 
				{
					Document doc = searcher.doc(docs[i].doc);
					fileWriter.write((51+queryID)+"    Q0      "+doc.get("DOCNO")+"     "+(i+1)+"  "+docs[i].score+"        run-l\n");
					//System.out.println(doc.get("DOCNO")+" "+docs[i].score);
				}
			}
			fileWriter.close();
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException
	{
		String filePath = args[0];
		String indexPath = args[1];
		String outputFilePath = args[2];
		
		List<String> shortQuery = new ArrayList<String>();
		List<String> longQuery = new ArrayList<String>();
		
		
		collectQuery(filePath,shortQuery,longQuery);
		
		parseQuery(indexPath,shortQuery,outputFilePath,false);
		parseQuery(indexPath,longQuery,outputFilePath,true);							
	}
}
