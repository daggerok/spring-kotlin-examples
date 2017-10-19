package daggerok

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime

@Configuration
class WebClientConfiguration {

  @Value("\${app.user-service.host}")
  var serviceHost: String? = null

  @Value("\${app.user-service.username}")
  var serviceUsername: String? = null

  @Value("\${app.user-service.password}")
  var servicePassword: String? = null

  @Bean
  fun userWebClient() = WebClient
      .create("http://$serviceHost:8080")
      .mutate()
      .filter(ExchangeFilterFunctions.basicAuthentication(serviceUsername!!, servicePassword!!))
      .build()
}

data class User(var id: String? = null, var name: String? = null)
data class UserEvent(var userId: String? = null, var at: LocalDateTime? = null)

@SpringBootApplication
class ClientApplication {

  @Bean
  fun runner(userWebClient: WebClient) = ApplicationRunner {

    val delayStartup = Mono.delay(Duration.ofSeconds(5))
    val eventStream = userWebClient
        .get()
        .uri("")
        .retrieve()
        .bodyToFlux(User::class.java)
        .flatMap {
          userWebClient
              .get()
              .uri("/{id}/events", it.id)
              .retrieve()
              .bodyToFlux(UserEvent::class.java)
        }
        .doOnError { println("f*ck: ${it.localizedMessage}") }

    Flux.concat(delayStartup, eventStream)
        .skip(1) // skip delayStartup subscription
        .subscribe({ println("subscribe: $it\n") })
  }
}

fun main(args: Array<String>) {
  SpringApplication.run(ClientApplication::class.java, *args)
}
