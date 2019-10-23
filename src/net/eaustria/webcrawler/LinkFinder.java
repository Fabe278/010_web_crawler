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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
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
            System.out.println(System.nanoTime() - t0);
            System.out.println(linkHandler.size());
            System.exit(0);
        }
        NodeFilter hrefNodeFilter = (Node node) -> node.getText().contains("href=\"http");
            if (!linkHandler.visited(url)) {
            linkHandler.addVisited(url);
            System.out.println(url);
            try {
                URL nurl = new URL(url);
                Parser parser = new Parser(nurl.openConnection());
                NodeList nodes = parser.extractAllNodesThatMatch(hrefNodeFilter);

                SimpleNodeIterator it = nodes.elements();
                while (it.hasMoreNodes()) {
                    Node node = it.nextNode();
                    try{
                        String[] parts = node.getText().split("\"http");
                    String temp = "http" + parts[1];
                    temp = temp.split("\"")[0];
//                    System.out.println(temp);
                    linkHandler.queueLink(temp);
                    }catch(Exception e){
                        
                    }
                }
            } catch (Exception ex) {}
        }      
    }
}
