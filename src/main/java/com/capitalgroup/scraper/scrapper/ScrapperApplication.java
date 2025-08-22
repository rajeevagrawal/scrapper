package com.capitalgroup.scraper.scrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.io.IOException;

@SpringBootApplication
public class ScrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrapperApplication.class, args);
	}

}

@RestController
@RequestMapping("/api/scrape")
class WebScraperController {
    @Autowired
    private WebScraperService webScraperService;

    @PostMapping
    public ResponseEntity<String> scrapeUrl(@RequestParam String url) {
        String csvPath = "scraped_data.csv";
        try {
            webScraperService.scrapeToCsv(url, csvPath);
            return ResponseEntity.ok("Data scraped and saved to " + csvPath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
