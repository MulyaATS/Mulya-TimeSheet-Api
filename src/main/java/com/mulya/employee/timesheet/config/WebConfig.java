package com.mulya.employee.timesheet.config;


import com.mulya.employee.timesheet.tenant.TenantContext;
import com.mulya.employee.timesheet.tenant.TenantInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    public WebConfig(TenantInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")               // Allow CORS for all endpoints
                .allowedOrigins(
                        "http://35.188.150.92",  // First IP
                        "http://192.168.0.140:3000",  // Second IP
                        "http://192.168.0.139:3000", // Third IP
                        "https://mymulya.com", // Forth IP
                        "https://portal.aventrainc.ai",
                        "http://localhost:3000", // Fifth IP
                        "http://192.168.0.135/",
                        "http://192.168.0.135:80","http://182.18.177.16:443",
                        "http://mymulya.com:443",
                        "http://localhost/",
                        "http://154.210.288.26",
                        "http://192.168.0.203:3000",
                        "http://192.168.0.167:3000",
                        "http://192.168.0.182:3000"
                )

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allowed HTTP methods
                .allowedHeaders("*")                      // Allow all headers
                .allowCredentials(true);                  // Allow credentials (cookies, headers, etc.)
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add((request, body, execution) -> {
            request.getHeaders().set(TenantContext.HEADER_NAME, TenantContext.getTenantId());
            return execution.execute(request, body);
        });
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}