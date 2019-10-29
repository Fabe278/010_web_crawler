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
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class LinkFinder implements Runnable {

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
        getSimpleLinks(url);
    }

    private void getSimpleLinks(String url) {
        if (linkHandler.size() >= 500) {
            System.out.println("Laufzeit: " + (System.nanoTime() - t0));
            System.out.println("Anzahl der Links" + linkHandler.size());
            System.exit(0);
        }
        if (!linkHandler.visited(url)) {

            try {
                URL nurl = new URL(url);
                Parser parser = new Parser(nurl.openConnection());
                NodeList nodes = parser.extractAllNodesThatMatch((Node node) -> node.getText().contains("href=\"http"));
                NodeIterator it = nodes.elements();
                while (it.hasMoreNodes()) {
                    Node node = it.nextNode();
                    String[] parts = node.getText().split("\"http");
                    String newurl = "http" + parts[1];
                    newurl = newurl.split("\"")[0];
                    try {
                        linkHandler.queueLink(newurl);
                    } catch (Exception ex) {
                    }
                }
                linkHandler.addVisited(url);
                System.out.println(url);
            } catch (IOException | ParserException ex) {
            }
        }
    }
}
