package io.technical_tasks.spring_web_app_for_payment_processing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeoIPService {

    private static final String GEO_IP_API_URL = "http://ip-api.com/json/{ip}";
    private static final Logger logger = LoggerFactory.getLogger(GeoIPService.class);

    private final RestTemplate restTemplate;

    public GeoIPService() {
        this.restTemplate = new RestTemplate();
    }

    public String getCountryCode(String ip) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GEO_IP_API_URL)
                .queryParam("fields", "countryCode");

        logger.info("Requesting country code for IP: {}", ip);
        GeoIPResponse response = restTemplate.getForObject(uriBuilder.buildAndExpand(ip).toUriString(), GeoIPResponse.class);
        logger.info("Response from IP API: {}", response != null ? response.getCountryCode() : "null");
        return response != null ? response.getCountryCode() : null;
    }

    private static class GeoIPResponse {
        private String countryCode;

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }
    }
}