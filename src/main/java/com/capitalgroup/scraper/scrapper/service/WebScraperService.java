package com.capitalgroup.scraper.scrapper.service;

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
    public String scrapeToCsv(String url, String csvPath, String type, String fundType) throws IOException {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.get(url);
            Thread.sleep(5000);
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);
            List<String[]> data = new ArrayList<>();
            if ("literature".equalsIgnoreCase(type)) {
                Elements links = doc.select("a[href]");
                data.add(new String[]{"Link Text", "URL"});
                for (Element link : links) {
                    String linkText = link.text();
                    String urlHref = link.absUrl("href");
                    data.add(new String[]{linkText, urlHref});
                }
            } else {
                Elements tableRows = doc.select("table tr");
                for (Element row : tableRows) {
                    Elements cols = row.select("th, td");
                    String[] rowData = cols.stream().map(Element::text).toArray(String[]::new);
                    if (rowData.length > 0) {
                        data.add(rowData);
                    }
                }
            }
            // Ensure 'data/yyyyMMdd' directory exists
            String dateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
            File datedDir = new File("data" + File.separator + dateFolder);
            if (!datedDir.exists()) {
                datedDir.mkdirs();
            }
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String baseName = csvPath.replaceFirst("\\.csv$", "");
            String newCsvPath = "data" + File.separator + dateFolder + File.separator +  baseName + "_" + timestamp + ".csv";
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
