package com.dp_ua.iksparser.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Configuration properties for Telegram Bot
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {

    /**
     * Bot API token from BotFather
     */
    @NotBlank(message = "Telegram bot token must not be blank")
    private String token;

    /**
     * Bot username (without @)
     */
    @NotBlank(message = "Telegram bot name must not be blank")
    private String name;

    /**
     * Visible name for bot in UI
     */
    @NotBlank(message = "Telegram bot visible name must not be blank")
    private String visibleName;

    /**
     * Reconnect timeout in milliseconds
     */
    @Positive(message = "Reconnect timeout must be positive")
    private Long reconnectTimeout = 10000L;

    /**
     * Admin user ID for administrative operations
     */
    @NotBlank(message = "Admin ID must not be blank")
    private String adminId;

    /**
     * Check if user is admin
     */
    public boolean isAdmin(String userId) {
        return adminId != null && adminId.equals(userId);
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin(Long userId) {
        return userId != null && isAdmin(String.valueOf(userId));
    }

}
