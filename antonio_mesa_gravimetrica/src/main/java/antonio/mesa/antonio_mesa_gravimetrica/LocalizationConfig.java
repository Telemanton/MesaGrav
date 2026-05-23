package antonio.mesa.antonio_mesa_gravimetrica;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * LocalizationConfig: Configuration class for implementing internationalization (i18n) in the application.
 * 
 * This configuration enables multi-language support throughout the entire application.
 * It uses cookies to persist the user's language preference across sessions.
 * 
 * Features:
 * - Locale resolution based on cookies
 * - Default locale is Spanish (es)
 * - Language can be changed via query parameter "lang"
 * - Language preference persists for 1 year
 * - Message source configured for resource bundle message resolution
 */
@Configuration
public class LocalizationConfig implements WebMvcConfigurer {

    /**
     * Configures the MessageSource to load messages from properties files.
     * This is required for Thymeleaf to resolve #{key} expressions.
     * 
     * @return ResourceBundleMessageSource configured with UTF-8 encoding
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages"); // Base name for message properties files (e.g., messages_en.properties, messages_es.properties)
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Configures the LocaleResolver to use cookies for storing the user's language preference.
     * 
     * @return CookieLocaleResolver configured with Spanish as default language
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("app_language");
        resolver.setDefaultLocale(Locale.of("es")); // Default language: Spanish
        return resolver;
    }

    /**
     * Configures the LocaleChangeInterceptor to handle language changes via query parameter.
     * 
     * When a request includes the parameter "lang=en" or "lang=es", the language will be changed.
     * 
     * @return LocaleChangeInterceptor configured to listen for "lang" parameter
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // URL parameter name for language change
        return interceptor;
    }
}
