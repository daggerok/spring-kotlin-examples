package daggerok

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.time.LocalDateTime

@Configuration
class WebClientConfiguration {

  @Value("\${app.user-service.host}")
  var serviceHost: String? = null

  @Bean
  fun userWebClient() = WebClient
      .create("http://$serviceHost:8080")
      .mutate()
      .build()
}

data class User(var id: String? = null, var name: String? = null)
data class UserEvent(var userId: String? = null, var at: LocalDateTime? = null)

@SpringBootApplication
class ClientApplication {

  @Bean
  fun runner(userWebClient: WebClient) = ApplicationRunner {

    userWebClient
        .get()
        // get all users: `http :8080`
        .uri("")
        .retrieve()
        .bodyToFlux(User::class.java)
        // service start delay on `./gradlew bootRun --parallel`
        .delayElements(Duration.ofSeconds(3))
        .flatMap {
          userWebClient
              .get()
              // get event stream for particular user: `http --stream :8080/$userId/events`
              .uri("/{id}/events", it.id)
              .retrieve()
              .bodyToFlux(UserEvent::class.java)
        }
        .doOnError { println("f*ck: ${it.localizedMessage}") }
        .subscribe({ println("subscribe: $it\n") })
  }
}

fun main(args: Array<String>) {
  SpringApplication.run(ClientApplication::class.java, *args)
}
