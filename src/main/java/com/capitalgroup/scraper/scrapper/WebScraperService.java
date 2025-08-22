package com.capitalgroup.scraper.scrapper;

import com.opencsv.CSVWriter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class WebScraperService {
    public String scrapeToCsv(String url, String csvPath) throws IOException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.get(url);
            // Wait for table or main content to load (customize selector as needed)
            Thread.sleep(5000); // Simple wait, replace with WebDriverWait for production
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);
            Elements tableRows = doc.select("table tr");
            List<String[]> data = new ArrayList<>();
            for (Element row : tableRows) {
                Elements cols = row.select("th, td");
                String[] rowData = cols.stream().map(Element::text).toArray(String[]::new);
                if (rowData.length > 0) {
                    data.add(rowData);
                }
            }
            // Add timestamp to file name
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String baseName = csvPath.replaceFirst("\\.csv$", "");
            String newCsvPath = baseName + "_" + timestamp + ".csv";
            
            try (CSVWriter writer = new CSVWriter(new FileWriter(newCsvPath))) {
                for (String[] row : data) {
                    writer.writeNext(row);
                }
            }
            File file = new File(newCsvPath);
            System.out.println("CSV file stored at: " + file.getAbsolutePath());
            return newCsvPath;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for page to load", e);
        } finally {
            driver.quit();
        }
    }
}
