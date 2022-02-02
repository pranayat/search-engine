package com.adplacement;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleAds {
	
	public static void insertAd(String firstname, String lastname, String url, String text, String iurl, float budget, float onclick, 
			Set<String> setngrams, String language) {
		AdCustomer c = new AdCustomer(firstname,lastname);
		int customerid = c.registerCustomer();
		if (iurl == "") {
			Ad a = new Ad(url, text, budget, onclick, setngrams, language);
			a.registerAd(customerid);
		}else {
			Ad a = new Ad(url, text, iurl, budget, onclick,  setngrams, language);
			a.registerAd(customerid);
		}
	}
	
	public static void insertExampleAds() {
		//Ad1
		String firstname = "Christoph";
		String lastname = "Lossen";
		String url = "https://www.mathematik.uni-kl.de/organisation/dekanat/lossen";
		String text = "Unser �bungsbetrieb funktioniert nur, wenn wir gen�gend motivierte und gut qualifizierte �bungsleiter an unserem Fachbereich haben. Deshalb suchen wir Sie! Bewerben Sie sich jetzt!";
		String iurl = "https://www.mathematik.uni-kl.de/fileadmin/_processed_/a/4/csm_Lossen_1_zu_1_9ea7dd430c.jpg";
		Set <String> ngrams = Stream.of("�bungsleiter", "lehre","�bungsbetrieb","job","bewerben")
		         .collect(Collectors.toCollection(HashSet::new)); 
		float budget = (float)12.0;
		float onclick = (float)0.05;
		String language = "ger";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		//Ad2
		firstname = "Christoph";
		lastname = "Lossen";
		url = "https://www.mathematik.uni-kl.de/en/organisation/dean-office/lossen";
		text = "Our exercise operation only works if we have enough motivated and well-qualified exercise instructors on our faculty. That is why we are looking for you! Apply now!";
		iurl = "https://www.mathematik.uni-kl.de/fileadmin/_processed_/a/4/csm_Lossen_1_zu_1_9ea7dd430c.jpg";
		ngrams = Stream.of("instructor", "teaching","apply","job","offers")
		         .collect(Collectors.toCollection(HashSet::new)); 
		budget = (float)6.0;
		onclick = (float)0.05;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad3
		firstname = "TU";
		lastname = "KL";
		url = "https://modhb.uni-kl.de/";
		text = "Check out our new digital module handbook. There you can find all necessary information about your study.";
		iurl = "";
		ngrams = Stream.of("module handbook", "teaching","study","degree")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)20.0;
		onclick = (float)0.01;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad4
		firstname = "SCI";
		lastname = "KL";
		url = "https://www.cs.uni-kl.de/en/studium/studierende/hiwis/";
		text = "The department of computer science is offering student assistant jobs. Let�s check them out.";
		iurl = "";
		ngrams = Stream.of("computer science", "job","sci")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)5.0;
		onclick = (float)0.01;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad5
		firstname = "Top";
		lastname = "Universities";
		url = "https://www.topuniversities.com/courses/computer-science-information-systems/guide";
		text = "What is a computer science degree? What do you learn? Why should you apply? And which are the best universities? Find out more on our page.";
		iurl = "";
		ngrams = Stream.of("computer science", "degree")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)1;
		onclick = (float)0.2;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad6
		firstname = "Best";
		lastname = "Gear";
		url = "https://compscicentral.com/best-gear-for-computer-science-students/";
		text = "If you want to study computer science you have to think about your equipment. We have complied the most important stuff.";
		iurl = "https://compscicentral.com/wp-content/uploads/2019/11/BestGear-1.jpg?ezimgfmt=ng%3Awebp%2Fngcb1%2Frs%3Adevice%2Frscb1-1";
		ngrams = Stream.of("computer science", "equipment")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)1;
		onclick = (float)0.1;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad7
		firstname = "Sebastian";
		lastname = "Michel";
		url = "https://dbis.informatik.uni-kl.de/index.php/en/teaching/winter-21-22/is-project-21-22";
		text = "In this project, we consider building a full-fledged Web Search engine. If you are interested apply until tomorrow.";
		iurl = "";
		ngrams = Stream.of("computer science", "project", "db", "dbislab")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)6;
		onclick = (float)0.2;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad8
		firstname = "Sebastian";
		lastname = "Michel";
		url = "https://nlp.stanford.edu/IR-book/";
		text = "You want to learn the basics of information systems. Check out this book. Also consider other literature.";
		iurl = "https://nlp.stanford.edu/IR-book/iir.jpg";
		ngrams = Stream.of("computer science", "literature","information systems", "dbislab")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)1.0;
		onclick = (float)0.3;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		
		//Ad9
		firstname = "Sebastian";
		lastname = "Michel";
		url = "https://dbis.informatik.uni-kl.de/index.php/en/people/michel";
		text = "If you are a student at TU Kaiserslautern and in the phase of looking for possible Bachelor's or Master's thesis topics,"
				+ "please do not hesitate to contacting me directly.";
		iurl = "https://dbis.informatik.uni-kl.de/images/michel.png";
		ngrams = Stream.of("computer science", "database systems", "information systems", "dbislab", "teaching", "bachelor thesis", "master thesis")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)10.0;
		onclick = (float)0.01;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		//Ad10
		firstname = "Uni";
		lastname = "Sport";
		url = "https://www.unisport.uni-kl.de/en/sports-program/sports-offers-a-z/swimming-ws21/22";
		text = "It is important to do sports. Swimming is a very healthy sport. Try it out on Mondays and Thursdays.";
		iurl = "https://www.unisport.uni-kl.de/fileadmin/_processed_/a/f/csm_Schwimmen_66fab68411.jpg";
		ngrams = Stream.of("afterwork","sport","swimming", "unisport", "tukl")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)10.0;
		onclick = (float)0.05;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		
		//Ad11
		firstname = "Uni";
		lastname = "Sport";
		url = "https://www.unisport.uni-kl.de/en/sports-program/sports-offers-a-z/e-sports";
		text = "E-Sports: Motor skills, tactical understanding and distinct problem-solving abilities, team spirit and communicative skills are the most important factors (and, of course, having a lot of fun!). "
				+ "These skills must be understood in theory and developed in active gameplay.";
		iurl = "https://www.unisport.uni-kl.de/fileadmin/_processed_/5/5/csm_E-Sports_10706bc684.jpg";
		ngrams = Stream.of("computer science", "e sports", "free time", "afterwork", "fun", "unisport")
		         .collect(Collectors.toCollection(HashSet::new));
		budget = (float)2.0;
		onclick = (float)0.5;
		language = "eng";
		insertAd(firstname, lastname, url, text, iurl,budget, onclick, ngrams, language);
		

	}
}
;