package com.capitalgroup.scraper.scrapper;

import org.junit.jupiter.api.Test;

import com.capitalgroup.scraper.scrapper.service.WebScraperService;

import java.io.File;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebScraperServiceTest {
    @Test
    public void testScrapeToCsv() throws Exception {
        WebScraperService webScraperService = new WebScraperService();
        //String url = "https://www.capitalgroup.com/individual/investments/americanfunds?updated=quarterly&pricing=NAV";
        String url = "https://www.capitalgroup.com/individual/investments/literature";
        String csvPath = "test_scraped_data.csv";
        webScraperService.scrapeToCsv(url, csvPath, "literature","AF");
        File csvFile = new File(csvPath);
        assertTrue(csvFile.exists() && csvFile.length() > 0, "CSV file should be created and not empty");
        // Clean up
        //csvFile.delete();
    }
}
