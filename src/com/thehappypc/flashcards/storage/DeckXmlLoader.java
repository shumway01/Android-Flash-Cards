package com.thehappypc.flashcards.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.thehappypc.cards.Card;
import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.data.TagList;

import android.content.Context;
import android.net.Uri;
import android.util.Xml;

/**
 * Read/write a FlashCardDeck from a file containing an XML representation.
 * The XML grammar is as follows:
 * 
 *  <?xml version="1.0" encoding="utf-8"?>
 *  <deck
 *    name="Name"
 *    version="Version"
 *    encoding="Encoding"
 *    uri="Uri" >
 *    	<deck-taglist>
 *    		<deck-tag>Tag 1
 *    		</deck-tag>
 *    		...
 *    		<deck-tag>Tag N
 *    		</deck-tag>
 *    	</deck-taglist>
 *    
 * 		<card>
 * 			<question>The flash card's question.
 * 			</question>
 * 			<answer>The flash card's answer.
 * 			</answer>
 * 			<card-taglist>
 * 				<card-tag>Tag 1
 * 				</card-tag>
 * 				...
 * 				<card-tag>Tag N
 * 				</card-tag>
 * 			</card-taglist>
 *  	</card>
 *  	<card>
 *  		...
 *  	</card>
 *  </deck>
 *  
 * @author Steve
 *
 */

/**
 * TODO Version control
 * If deck version > program version do not allow save (loses information)
 *    possibly add member variable to deck to store unimplemented attributes
 * if deck version < program version save using only attributes available in old version
 * @author Steve
 *
 */

public class DeckXmlLoader extends DeckLoader {
	
	private static final String XML_TAG_DECK = "card_deck";
	private static final String XML_TAG_NAME = "name";
	private static final String XML_TAG_VERSION = "version";
	private static final String XML_TAG_ENCODING = "encoding";
	private static final String XML_TAG_URI = "uri";
	private static final String XML_TAG_DECK_TAGLIST = "deck-taglist";
	private static final String XML_TAG_DECK_TAG = "deck-tag";
	
	private static final String XML_TAG_CARD = "card";
	private static final String XML_TAG_QUESTION = "question";
	private static final String XML_TAG_HINT = "hint";
	private static final String XML_TAG_ANSWER = "answer";
	private static final String XML_TAG_EXPLANATION = "explanation";
	private static final String XML_TAG_CARD_TAGLIST = "card-taglist";
	private static final String XML_TAG_CARD_TAG = "card-tag";
	
	public static final String ENCODING_XML = "xml";
	public static final String CURRENT_VERSION = "0.1";
	public static final String DECKNAME_PREFIX = "Deck_";
	
	/**
	 * The deck files are XML files but we store them with the extension
	 * .fcd in hopes that someday that will make it easier to write a
	 * ContentResolver for importing decks via email attachments.
	 */
	public static final String XML_FILE_EXT = ".fcd";
	
	private static DeckXmlLoader mSingleton = null;
	
	private DeckXmlLoader() {
		super();
	}
	
	public static DeckXmlLoader getInstance() {
		if (mSingleton == null)
			mSingleton = new DeckXmlLoader();
		return mSingleton;
	}
	
	@Override
	public boolean init(Context context, FlashCardDeck deck) {
		boolean initOk = false;
		String fileName = new String(DECKNAME_PREFIX +
							  String.valueOf(deck.getId()) +
							  XML_FILE_EXT);
		File file = new File(context.getFilesDir(), fileName);
		initOk = file != null;
		
		URI uri = file.toURI();
		deck.setUri(uri.toString());
		deck.setEncoding(ENCODING_XML);
		deck.setVersion(CURRENT_VERSION);
		deck.setLoader(this);
		
		return initOk;
	}
    
	/**
	 * Read the contents of the deck from its backing store. In the case of the XML loader,
	 * "inflate" the deck from an XML representation read from a file.
	 *
	 * @param deck
	 */
	@Override
	public boolean load(FlashCardDeck deck) {
		boolean loaded = false;
		URI uri;
		try {
			uri = new URI(deck.getUri());
			if (!uri.getScheme().equals("file"))
				throw new URISyntaxException(uri.toString(), "Expected file scheme");
			loaded = load(deck, uri.getPath());
		} catch (Exception URISyntaxException) {
			FlashCardApplication.log("Bad URI string: " + deck.getUri());
		}
		return loaded;
	}
	
	public boolean load(FlashCardDeck deck, String fileName) {
		boolean loaded = false;
		
        XmlPullParserFactory factory;
        XmlPullParser xpp;
		try {
			factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			FlashCardApplication.log("Can't create XmlPullParser");
			e.printStackTrace();
			return loaded;
		}
                
		FileInputStream fis;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Handle file not found exception on load()
			FlashCardApplication.log("Card deck file " + fileName + " does not exist");
			e.printStackTrace();
			return loaded;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        
        int eventType;
		try {
			FlashCard card = null;
        	String xmlTagName = null;
        	String text = null;
			
			xpp.setInput(br);
			eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {        	
	        		        	
	        	switch (eventType) {
	        	case XmlPullParser.START_DOCUMENT:
	        		break;
	        	case XmlPullParser.START_TAG:
	        		xmlTagName = xpp.getName();
	        		if (xmlTagName.equals(XML_TAG_DECK)) {
	        			// When we start processing a deck, initialize it
	        			// with default version and encoding values
	        			deck.setVersion(CURRENT_VERSION);
	        			deck.setEncoding(ENCODING_XML);
	        			// Process attributes
	        			for (int i = 0; i < xpp.getAttributeCount(); i++) {
	        				String attrName = xpp.getAttributeName(i);
	        				String attrVal = xpp.getAttributeValue(i);
	        				if (attrName.equals(XML_TAG_NAME)) {
	        					deck.setName(attrVal);
			        		} else if (xmlTagName.equals(XML_TAG_VERSION)) {
			        			deck.setVersion(attrVal);
			        		} else if (xmlTagName.equals(XML_TAG_ENCODING)) {
			        			deck.setEncoding(attrVal);
		        			}
	        			}
	        		} else if (xmlTagName.equals(XML_TAG_CARD)) {
	        			// When we start processing an individual card,
	        			// allocate it.
	        			card = new FlashCard();	        			
	        		}
	        		// NB: there is no processing required for starting card
	        		// or deck tag lists
	        		break;
	        	case XmlPullParser.END_TAG:
	        		xmlTagName = xpp.getName();
	        		if (xmlTagName == null || xmlTagName.equals("")) {
	        			FlashCardApplication.log("Unexpected empty XML end tag!");
	        		} else if (xmlTagName.equals(XML_TAG_CARD)) {
        				// When we finish processing an individual card,
        				// add it to the current card deck.
	        			if (card != null) { // sanity check for missing start <card> tag
	        				deck.addCard(card);
	        				card = null;
	        			}
	        		} else if (xmlTagName.equals(XML_TAG_QUESTION)) {
	        			if (card != null)	// sanity check for missing start <card> tag
	        				card.setQuestion(text);
	        		} else if (xmlTagName.equals(XML_TAG_HINT)) {
	        			if (card != null)	// sanity check for missing start <card> tag
	        				card.setHint(text);
	        		} else if (xmlTagName.equals(XML_TAG_ANSWER)) {
	        			if (card != null)	// sanity check for missing start <card> tag
	        				card.setAnswer(text);
	        		} else if (xmlTagName.equals(XML_TAG_EXPLANATION)) {
	        			if (card != null)	// sanity check for missing start <card> tag
	        				card.setExplanation(text);
	        		} else if (xmlTagName.equals(XML_TAG_CARD_TAGLIST)) {
        				// Card tags are added as they are encountered, so no
	        			// processing is necessary when we reach the end of the list
	        		} else if (xmlTagName.equals(XML_TAG_CARD_TAG)) {
	        			if (card != null)	// sanity check for missing start <card> tag
	        				card.addTag(text);
	        		} else if (xmlTagName.equals(XML_TAG_NAME)) {
	        			deck.setName(text);
	        		} else if (xmlTagName.equals(XML_TAG_VERSION)) {
	        			deck.setVersion(text);
	        		} else if (xmlTagName.equals(XML_TAG_ENCODING)) {
	        			deck.setEncoding(text);
	        		} else if (xmlTagName.equals(XML_TAG_DECK)) {
	        			// Nothing to do
	        		} else if (xmlTagName.equals(XML_TAG_URI)) {
	        			// We ignore URI in the XML file -- we won't allow the
	        			// XML file to override the actual file location
	        		} else if (xmlTagName.equals(XML_TAG_DECK_TAG)) {
	        			deck.addTag(text);
	        		} else if (xmlTagName.equals(XML_TAG_DECK_TAGLIST)) {
        				// Deck tags are added as they are encountered, so no
	        			// processing is necessary when we reach the end of the list
	        		}
	        		// Handle cases where there's no text between the start and end
	        		// XML tags by resetting text variable
	        		text = null;
	        		break;
	        	case XmlPullParser.TEXT:
	        		text = xpp.isWhitespace() ? "" : xpp.getText();
	        		break;
	        	}	
				eventType = xpp.next();
	        }
		} catch (XmlPullParserException e) {
			FlashCardApplication.log("Error parsing XML stream");
			e.printStackTrace();
		} catch (IOException e) {
			FlashCardApplication.log("Error reading XML stream");
			e.printStackTrace();
        }
        
        try {
        	br.close();
			fis.close();
			loaded = true;
		} catch (IOException e) {
			FlashCardApplication.log("Error closing XML input stream");
			e.printStackTrace();
		}
        
        return loaded;        
	}
	
	/**
	 * Write the contents of the deck to its backing store. In the case of the XML loader,
	 * "deflate" the deck into an XML representation and write it to a file.
	 *
	 * @param deck
	 */
	@Override
	public boolean store(FlashCardDeck deck) {
		boolean stored = false;
		
		URI uri;
		try {
			uri = new URI(deck.getUri());
			if (!uri.getScheme().equals("file"))
				throw new URISyntaxException(uri.toString(), "Expected file scheme");
			stored = store(deck, uri.getPath());
		} catch (Exception URISyntaxException) {
			FlashCardApplication.log("Bad URI string: " + deck.getUri());
		}
		
		return stored;
	}
	
	/**
	 * Write the contents of the deck to its backing store. In the case of the XML loader,
	 * "deflate" the deck into an XML representation and write it to a file.
	 *
	 * @param deck
	 * @param fileName
	 * @return 
	 */
	public boolean store(FlashCardDeck deck, String fileName) {
		boolean stored = false;
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			FlashCardApplication.log("Cannot open card deck file " + fileName + " for output");
			e.printStackTrace();
			return stored;
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		Iterator<? extends Card> iterator = deck.iterator();
		FlashCard card;

        XmlSerializer serializer = Xml.newSerializer();
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);	
        
        try {
            serializer.setOutput(bw);
            
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", XML_TAG_DECK);
            serializer.attribute("", XML_TAG_NAME, deck.getName());
            if (deck.getVersion() != null)
            	serializer.attribute("", XML_TAG_VERSION, deck.getVersion());
            if (deck.getEncoding() != null)
            	serializer.attribute("", XML_TAG_ENCODING, deck.getEncoding());
            if (deck.getUri() != null)
            	serializer.attribute("", XML_TAG_URI, deck.getUri());
            
            if (deck.countTags() > 0) {
            	serializer.startTag("",  XML_TAG_DECK_TAGLIST);
            	TagList tagList = deck.cloneTags();
            	Iterator<String> i = tagList.iterator();
            	while (i.hasNext()) {
                    serializer.startTag("", XML_TAG_DECK_TAG);
                    serializer.text(i.next());
                    serializer.endTag("", XML_TAG_DECK_TAG);               		
            	}
            	serializer.endTag("",  XML_TAG_DECK_TAGLIST);
            }                
            
            while (iterator.hasNext()) {
            	card = (FlashCard) iterator.next();
            	String s;
            	
                serializer.startTag("", XML_TAG_CARD);
                serializer.startTag("", XML_TAG_QUESTION);
                serializer.text(card.getQuestion());
                serializer.endTag("", XML_TAG_QUESTION);
                if ((s = card.getHint()) != null) {
	                serializer.startTag("", XML_TAG_HINT);
	                serializer.text(s);
	                serializer.endTag("", XML_TAG_HINT);
                }
                serializer.startTag("", XML_TAG_ANSWER);
                serializer.text(card.getAnswer());
                serializer.endTag("", XML_TAG_ANSWER);
                if ((s = card.getExplanation()) != null) {
	                serializer.startTag("", XML_TAG_EXPLANATION);
	                serializer.text(card.getExplanation());
	                serializer.endTag("", XML_TAG_EXPLANATION);
                }
                
                TagList tagList = card.cloneTags();
                if (tagList.size() > 0) {
                	serializer.startTag("",  XML_TAG_CARD_TAGLIST);
                	Iterator<String> i = tagList.iterator();
                	while (i.hasNext()) {	
                        serializer.startTag("", XML_TAG_CARD_TAG);
                        serializer.text(i.next());
                        serializer.endTag("", XML_TAG_CARD_TAG);               		
                	}
                	serializer.endTag("",  XML_TAG_CARD_TAGLIST);
                }                
                serializer.endTag("", XML_TAG_CARD);
            }
            
            serializer.endTag("", XML_TAG_DECK);
            serializer.endDocument();
            
        } catch (Exception e) {
        	FlashCardApplication.log("Error writing XML stream");
            e.printStackTrace();
        } 
                
        try {
        	bw.close();
			fos.close();
			stored = true;
		} catch (IOException e) {
			FlashCardApplication.log("Error closing XML output stream");
			e.printStackTrace();
		}   	
        
        return stored;
	}
	
	/**
	 * Remove the deck's backing store [XML] file.
	 * @param deck The deck to be deleted.
	 * @return true if the deck was deleted, false otherwise.
	 */
	@Override
	public boolean delete(FlashCardDeck deck) {
		boolean deleted = false;
		
		Uri uri = Uri.parse(deck.getUri());
		File file = new File(uri.getPath());
		deleted = file.delete();
		if (!deleted)
			FlashCardApplication.log("Error deleting deck file: " + uri.getPath());
		
		return deleted;
	}
}
