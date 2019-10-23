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
        NodeFilter hrefNodeFilter = (Node node) -> node.getText().contains("a href=\"http");
        
        if(!linkHandler.visited(url)){
            try {
                linkHandler.queueLink(url);
                URL nurl = new URL(url);
                Parser parser = new Parser(nurl.openConnection());
                NodeList nodes = parser.extractAllNodesThatMatch(hrefNodeFilter);
                
                SimpleNodeIterator it = nodes.elements();
                while(it.hasMoreNodes()){
                    Node node = it.nextNode();
                    System.out.println(node.getText());
                }
                
            } catch (Exception ex) {
                Logger.getLogger(LinkFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        linkHandler.addVisited(url);
        if(linkHandler.size() >= 500){
            System.out.println(System.nanoTime() - t0);
        }
        // ToDo: Implement
        // 1. if url not already visited, visit url with linkHandler
        // 2. get url and Parse Website
        // 3. extract all URLs and add url to list of urls which should be visited
        //    only if link is not empty and url has not been visited before
        // 4. If size of link handler equals 500 -> print time elapsed for statistics               
        
    }
}

