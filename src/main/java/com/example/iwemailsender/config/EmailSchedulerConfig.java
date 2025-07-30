package com.example.iwemailsender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailSchedulerConfig {

    private Retry retry;
    private Admin admin;

    public Retry getRetry()
    {
        return retry;
    }

    public void setRetry(Retry retry)
    {
        this.retry=retry;
    }
    public Admin getAdmin()
    {
        return admin;
    }
    public void setAdmin(Admin admin)
    {
        this.admin=admin;
    }
}


