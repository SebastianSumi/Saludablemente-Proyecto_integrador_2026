package upeu.edu.pe.Saludabemente.social;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Clock;

@Configuration(proxyBeanMethods=false)
@EnableScheduling
public class SocialModuleConfiguration {
    @Bean public Clock socialClock() { return Clock.system(java.time.ZoneId.of("America/Lima")); }
}
