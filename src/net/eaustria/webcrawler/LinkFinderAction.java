/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

/**
 *
 * @author bmayr
 */
// Recursive Action for forkJoinFramework from Java7
public class LinkFinderAction extends RecursiveAction {

    private String url;
    private ILinkHandler cr;
    private List<RecursiveAction> list = new ArrayList<>();
    /**
     * Used for statistics
     */
    private static final long t0 = System.currentTimeMillis();

    public LinkFinderAction(String url, ILinkHandler cr) {
        this.url = url;
        this.cr = cr;
    }

    @Override
    public void compute() {
        if (cr.size() >= 1500) {
            System.out.println("Laufzeit: " + (System.currentTimeMillis()- t0));
            System.out.println("Anzahl der Links: " + cr.size());
            System.exit(0);
        }
        NodeFilter hrefNodeFilter = (Node node) -> node.getText().contains("a href=\"http");
        if (!cr.visited(url)) {
            cr.addVisited(url);
//            System.out.println(url);
            try {
                URL nurl = new URL(url);
                Parser parser = new Parser(nurl.openConnection());
                NodeList nodes = parser.extractAllNodesThatMatch(hrefNodeFilter);
                NodeIterator it = nodes.elements();
                while (it.hasMoreNodes()) {
                    Node node = it.nextNode();
                        String[] parts = node.getText().split("\"http");
                        String newurl = "http" + parts[1];
                        newurl = newurl.split("\"")[0];
                        list.add(new LinkFinderAction(newurl, cr));
                }
            } catch (IOException | ParserException ex) {}
            invokeAll(list);
        }   
    }
}
