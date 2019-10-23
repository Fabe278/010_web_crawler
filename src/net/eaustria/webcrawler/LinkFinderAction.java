/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 *
 * @author bmayr
 */

// Recursive Action for forkJoinFramework from Java7

public class LinkFinderAction extends RecursiveAction {

    private String url;
    private ILinkHandler cr;
    /**
     * Used for statistics
     */
    private static final long t0 = System.nanoTime();

    public LinkFinderAction(String url, ILinkHandler cr) {
        this.url = url;
        this.cr = cr;
    }

    @Override
    public void compute() {
        if (cr.size() >= 500) {
            System.out.println(System.nanoTime() - t0);
            System.out.println(cr.size());
            System.exit(0);
        }
        NodeFilter hrefNodeFilter = (Node node) -> node.getText().contains("href=\"http");
            if (!cr.visited(url)) {
            cr.addVisited(url);
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
//                    cr.queueLink(temp);
                    }catch(Exception e){
                        
                    }
                }
            } catch (Exception ex) {}
        }      
        // ToDo:
        // 1. if crawler has not visited url yet:
        // 2. Create new list of recursiveActions
        // 3. Parse url
        // 4. extract all links from url
        // 5. add new Action for each sublink
        // 6. if size of crawler exceeds 500 -> print elapsed time for statistics
        // -> Do not forget to call ìnvokeAll on the actions!      
    }
}

