/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

/**
 *
 * @author bmayr
 */
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LinkFinder implements Runnable {

    static IamAuthenticator authenticator = new IamAuthenticator("LDHnZKnog4dDXKkyal2jfQUvFlBeWW_dAoeGTr4O31qb");
    static NaturalLanguageUnderstanding naturalLanguageUnderstanding = new NaturalLanguageUnderstanding("2019-07-12", authenticator);

    private String url;
    private ILinkHandler linkHandler;
    /**
     * Used fot statistics
     */
    private static final long t0 = System.nanoTime();

    public LinkFinder(String url, ILinkHandler handler) {
        this.url = url;
        this.linkHandler = handler;
    }

    @Override
    public void run() {
        naturalLanguageUnderstanding.setServiceUrl("https://gateway-fra.watsonplatform.net/natural-language-understanding/api");
        getSimpleLinks(url);
    }

    private void getSimpleLinks(String url) {
        if (linkHandler.size() >= 5) {
            System.out.println("Laufzeit: " + (System.nanoTime() - t0));
            System.out.println("Anzahl der Links" + linkHandler.size());
            System.exit(0);
        }
        if (!linkHandler.visited(url)) {
            try {
                URL uriLink = new URL(url);
                Parser parser = new Parser(uriLink.openConnection());
                NodeList list = parser.extractAllNodesThatMatch(new NodeClassFilter(LinkTag.class));
                List<String> urls = new ArrayList<String>();
                getBody(parser, url);
                for (int i = 0; i < list.size(); i++) {
                    LinkTag extracted = (LinkTag) list.elementAt(i);

                    if (!extracted.getLink().isEmpty()
                            && !linkHandler.visited(extracted.getLink())) {

                        urls.add(extracted.getLink());
                    }

                }
                //we visited this url
                linkHandler.addVisited(url);

                if (linkHandler.size() == 5) {
                    System.out.println("Time to visit 500 distinct links = " + (System.nanoTime() - t0));
                    System.exit(0);
                }

                for (String l : urls) {
                    linkHandler.queueLink(l);
                }

            } catch (Exception e) {
                //ignore all errors for now
            }
        }
    }

    public void getBody(Parser parser, String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element erg = doc.body();
            String body = erg.text();

            CategoriesOptions categories = new CategoriesOptions.Builder()
                    .limit(3)
                    .build();

            Features features = new Features.Builder()
                    .categories(categories)
                    .build();

            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                    .url(url)
                    .features(features)
                    .build();

            AnalysisResults response = naturalLanguageUnderstanding
                    .analyze(parameters)
                    .execute()
                    .getResult();
            String meta = response.toString().replace("\n", " ");
            synchronized (this) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(linkHandler.getCsvDateiName(), true))) {
                    writer.append(url);
                    System.out.println(url);
                    writer.append(";");
                    String rightBody = body.replace(";", " ");
                    writer.append(rightBody);
                    writer.append(";");
                    writer.append(meta);
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LinkFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
