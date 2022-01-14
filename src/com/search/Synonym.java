package com.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.ConnectionManager;
import com.indexer.Indexer;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;

public class Synonym {

	private Dictionary dictionary;
	
	public Synonym() {
		try {
			this.dictionary = Dictionary.getDefaultResourceInstance();
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void bootstrap() throws SQLException, FileNotFoundException, IOException {
		Connection conn = (new ConnectionManager()).getConnection();
		
		try(BufferedReader br = new BufferedReader(new FileReader("./openthesaurus.txt"))) {
			PreparedStatement pstmtInsert = conn.prepareStatement("INSERT INTO german_synonyms (term, synonym) VALUES (?,?)");
			PreparedStatement pstmtFind = conn.prepareStatement("SELECT * FROM german_synonyms WHERE (term = ? AND synonym = ?)");
		    for(String line; (line = br.readLine()) != null; ) {
		        
		        line = line.replaceAll("\\(.+?\\)", "");
		        String[] synonyms = line.split(";");
		        
		        for(String term: synonyms) {
	        		term = term.trim().toLowerCase();
		        	for (String synonym: synonyms) {
		        		synonym = synonym.trim().toLowerCase();
		        		
		        		if (term.equals(synonym)) {
		        			continue;
		        		}

		        		pstmtFind.setString(1, term);
		        		pstmtFind.setString(2, synonym);

		        		ResultSet rs = pstmtFind.executeQuery();
		        		
		        		if (!rs.next()) {
		        			pstmtInsert.setString(1, term);
		        			pstmtInsert.setString(2, synonym);
		        			pstmtInsert.executeUpdate();
		        			conn.commit();
		        		}
		        	}
		        }
		    }
		}
		
		conn.close();
	}
	
	public List<String> getEnglishSynonyms(String inputWord) throws JWNLException{
		IndexWord indexNoun = this.dictionary.lookupIndexWord(POS.NOUN, inputWord);
		IndexWord indexVerb = this.dictionary.lookupIndexWord(POS.VERB, inputWord);
		IndexWord indexAdjective = this.dictionary.lookupIndexWord(POS.ADJECTIVE, inputWord);
		
		List<Synset> wordSenses = new ArrayList<Synset>();
		
		if (indexNoun != null) {
			wordSenses = Stream.concat(wordSenses.stream(), indexNoun.getSenses().stream()).collect(Collectors.toList());
		}
		
		if (indexVerb != null) {
			wordSenses = Stream.concat(wordSenses.stream(), indexVerb.getSenses().stream()).collect(Collectors.toList());
		}
		
		if (indexAdjective != null) {
			wordSenses = Stream.concat(wordSenses.stream(), indexAdjective.getSenses().stream()).collect(Collectors.toList());
		}

		List<String> synonyms = new ArrayList<String>();
		
		for (Synset wordSense: wordSenses) {
			List<Word> words = wordSense.getWords();
			
			for (Word word: words) {
				synonyms.add(Indexer.stem_word(word.getLemma().toLowerCase()));
			}
		}
		
		return synonyms.stream()
			     .distinct()
			     .collect(Collectors.toList());
		
	}
	
	public List<String> getGermanSynonyms(String inputWord) throws JWNLException, SQLException{
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmtFind = conn.prepareStatement("SELECT synonym FROM german_synonyms WHERE term = ?");
		pstmtFind.setString(1, inputWord);
		ResultSet rs = pstmtFind.executeQuery();
		
		List<String> synonyms = new ArrayList<String>();
		while(rs.next()) {
			synonyms.add(rs.getString(1));
		}
		
		conn.close();

		return synonyms;
	}	
}
