package com.skyways.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SkyscannerService {

    private static final Logger logger =
        LoggerFactory.getLogger(SkyscannerService.class);

    @Value("${skyscanner.api.key}")
    private String apiKey;

    @Value("${skyscanner.api.host}")
    private String apiHost;

    @Value("${skyscanner.api.url}")
    private String apiUrl;

    private RestTemplate getRestTemplate() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        X509Certificate[] certs, 
                        String authType) {}
                    public void checkServerTrusted(
                        X509Certificate[] certs, 
                        String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                (hostname, session) -> true);
            return new RestTemplate();
        } catch (Exception e) {
            logger.error("SSL setup failed: {}", e.getMessage());
            return new RestTemplate();
        }
    }

    private HttpEntity<String> getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", apiHost);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    private Object callApi(String url) {
        try {
            ResponseEntity<Object> response = 
                getRestTemplate().exchange(
                    url, HttpMethod.GET, 
                    getHeaders(), Object.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("API call failed: {}", e.getMessage());
            throw new RuntimeException(
                "API call failed: " + e.getMessage());
        }
    }

    // 1. Search Flights
    public Object searchFlights(String originSkyId,
            String destinationSkyId, String originEntityId,
            String destinationEntityId, String date,
            String returnDate, String cabinClass,
            int adults, int childrens, int infants) {

        logger.info("Searching flights: {} to {}",
            originSkyId, destinationSkyId);

        String url = apiUrl + "/flights/searchFlights" +
            "?originSkyId=" + originSkyId +
            "&destinationSkyId=" + destinationSkyId +
            "&originEntityId=" + originEntityId +
            "&destinationEntityId=" + destinationEntityId +
            "&date=" + date +
            (returnDate != null ? "&returnDate=" + returnDate : "") +
            "&cabinClass=" + cabinClass.toLowerCase() +
            "&adults=" + adults +
            "&childrens=" + childrens +
            "&infants=" + infants +
            "&currency=INR&market=IN&countryCode=IN";

        return callApi(url);
    }

    // 2. Search Flight Everywhere
    public Object searchFlightEverywhere(String originSkyId,
            String originEntityId, int adults,
            String cabinClass) {

        logger.info("Search everywhere from: {}", originSkyId);

        String url = apiUrl + "/flights/searchFlightEverywhere" +
            "?originSkyId=" + originSkyId +
            "&originEntityId=" + originEntityId +
            "&adults=" + adults +
            "&cabinClass=" + cabinClass.toLowerCase() +
            "&currency=INR&market=IN";

        return callApi(url);
    }

    // 3. Search Incomplete
    public Object searchIncomplete(String sessionId,
            String currency) {

        logger.info("Fetching incomplete results: {}", sessionId);

        String url = apiUrl + "/flights/searchIncomplete" +
            "?sessionId=" + sessionId +
            "&currency=" + currency +
            "&countryCode=IN";

        return callApi(url);
    }

    // 4. Get Flight Details
    public Object getFlightDetails(String sessionId,
            String itineraryId, String currency) {

        logger.info("Getting flight details: {}", itineraryId);

        String url = apiUrl + "/flights/getFlightDetails" +
            "?sessionId=" + sessionId +
            "&itineraryId=" + itineraryId +
            "&currency=" + currency +
            "&countryCode=IN";

        return callApi(url);
    }

    // 5. Get Cheapest Oneway
    public Object getCheapestOneway(String originSkyId,
            String destinationSkyId, String month) {

        logger.info("Cheapest oneway: {} to {}",
            originSkyId, destinationSkyId);

        String url = apiUrl + "/flights/getCheapestOneway" +
            "?originSkyId=" + originSkyId +
            "&destinationSkyId=" + destinationSkyId +
            "&month=" + month +
            "&currency=INR&market=IN";

        return callApi(url);
    }

    // 6. Get Price Calendar
    public Object getPriceCalendar(String originSkyId,
            String destinationSkyId, String fromDate,
            String toDate, String cabinClass) {

        logger.info("Price calendar: {} to {}",
            originSkyId, destinationSkyId);

        String url = apiUrl + "/flights/getPriceCalendar" +
            "?originSkyId=" + originSkyId +
            "&destinationSkyId=" + destinationSkyId +
            "&fromDate=" + fromDate +
            "&toDate=" + toDate +
            "&cabinClass=" + cabinClass.toLowerCase() +
            "&currency=INR&market=IN";

        return callApi(url);
    }

    // 7. Get Price Calendar Return
    public Object getPriceCalendarReturn(String originSkyId,
            String destinationSkyId, String fromDate,
            String returnFromDate) {

        logger.info("Price calendar return: {} to {}",
            originSkyId, destinationSkyId);

        String url = apiUrl + "/flights/getPriceCalendarReturn" +
            "?originSkyId=" + originSkyId +
            "&destinationSkyId=" + destinationSkyId +
            "&fromDate=" + fromDate +
            "&returnFromDate=" + returnFromDate +
            "&currency=INR&market=IN";

        return callApi(url);
    }

    // 8. Search Airport
    public Object searchAirport(String query) {

        logger.info("Searching airport: {}", query);

        String url = apiUrl + "/flights/searchAirport" +
            "?query=" + query +
            "&market=IN&locale=en-IN";

        return callApi(url);
    }

    // 9. Search Flights Multi Stops
    public Object searchFlightsMultiStops(String legs,
            int adults, String cabinClass) {

        logger.info("Multi-stop flight search");

        String url = apiUrl + "/flights/searchFlightsMultiStops" +
            "?legs=" + legs +
            "&adults=" + adults +
            "&cabinClass=" + cabinClass.toLowerCase() +
            "&currency=INR";

        return callApi(url);
    }
}