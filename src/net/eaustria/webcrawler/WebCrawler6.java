/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.eaustria.webcrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bmayr
 */
public class WebCrawler6 implements ILinkHandler {

    private String csvdateiname;
    private final Collection<String> visitedLinks = Collections.synchronizedSet(new HashSet<String>());
//    private final Collection<String> visitedLinks = Collections.synchronizedList(new ArrayList<String>());
    private String url;
    private ExecutorService execService;

    public WebCrawler6(String startingURL, int maxThreads) {
        this.url = startingURL;
        execService = Executors.newFixedThreadPool(maxThreads);
    }

    @Override
    public void queueLink(String link) throws Exception {
        startNewThread(link);
    }

    @Override
    public int size() {
        return visitedLinks.size();
    }

    @Override
    public void addVisited(String s) {
        visitedLinks.add(s);
    }

    @Override
    public boolean visited(String s) {
        return visitedLinks.contains(s);
    }

    private void startNewThread(String link) throws Exception {
        execService.submit(new LinkFinder(link, this));
        // ToDo: Use executer Service to start new LinkFinder Task!
    }

    private void startCrawling() throws Exception {
        createFile();
        startNewThread(this.url);

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        new WebCrawler6("https://www.orf.at/", 64).startCrawling();
    }

    public void createFile() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        Date date = new Date();
        csvdateiname = "CrawlerRun_" + df.format(date) + ".csv";
        
        File file = new File(csvdateiname);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(WebCrawler6.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String getCsvDateiName(){
        return csvdateiname;
    }
}
