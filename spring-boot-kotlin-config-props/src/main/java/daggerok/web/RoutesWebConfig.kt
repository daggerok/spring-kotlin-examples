package daggerok.web

import daggerok.props.GDPRProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Configuration
class RoutesWebConfig {

  @Bean
  fun routes(gdprProps: GDPRProperties) = router {
    ("/").nest {
      "/api".nest {
        contentType(APPLICATION_JSON_UTF8)
        GET("/**") {
          ok().body(Mono.just(gdprProps), gdprProps.javaClass)
        }
      }
      contentType(TEXT_HTML)
      GET("/") {
        ok().render("index.html", mapOf(
                "localDateTime" to LocalDateTime.now(),
                "text" to "hola!",
                "users" to gdprProps.users.orEmpty().values
        ))
      }
    }
  }
}
