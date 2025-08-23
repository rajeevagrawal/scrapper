package com.capitalgroup.scraper.scrapper.controller;

import com.capitalgroup.scraper.scrapper.service.WebScraperService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@Slf4j
public class ScrapperController {
    @Autowired
    private WebScraperService webScraperService;

    @GetMapping("/api/scrape")
    public ResponseEntity<String> scrape(@RequestParam String audience) {
        String csvFile = "REQUSTURL.csv";
        String audienceL=audience.toLowerCase();
        String host = "https://www.capitalgroup.com/" + audienceL;
        List<String> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;
                String uri = parts[0];
                String type = parts[1];
                String fund = parts[2];
                String name = parts[3];
                String url = host + uri;
                
                Callable<String> task = () -> {
                    return webScraperService.scrapeToCsv(url, audienceL+"_"+fund+"_"+type+"_"+name, type, fund);
                    
                };
                futures.add(executor.submit(task));
            }
            for (Future<String> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException ee) {
                    results.add("Error: " + ee.getCause().getMessage());
                }
            }
            executor.shutdown();
            return ResponseEntity.ok("Scraping complete for audience: " + audience + ". Files: " + String.join("; ", results));
        } catch (Exception e) {
            executor.shutdown();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
