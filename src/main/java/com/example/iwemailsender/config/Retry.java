package com.example.iwemailsender.config;

import org.springframework.beans.factory.annotation.Value;

public class Retry{

    @Value("${retry.max-attempts}")
    private int maxAttempts;
    @Value("${retry.delay-seconds}")
    private int delaySeconds;

    public int getMaxAttempts()
    {
        return maxAttempts;
    }
    public void setMaxAttempts(int maxAttempts)
    {
        this.maxAttempts = maxAttempts;
    }
    public int getDelaySeconds()
    {
        return delaySeconds;
    }
    public void setDelaySeconds(int delaySeconds)
    {
        this.delaySeconds=delaySeconds;
    }
}