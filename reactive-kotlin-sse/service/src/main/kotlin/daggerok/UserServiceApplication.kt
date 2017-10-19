package daggerok

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import java.time.LocalDateTime

@Document
data class User(@Id var id: String? = null, var name: String? = null)

@Repository
interface UserRepository : ReactiveMongoRepository<User, String>

data class UserEvent(var userId: String? = null, var at: LocalDateTime? = LocalDateTime.now())

@Service
class UserService(val repository: UserRepository) {

  fun all() = repository.findAll()

  fun one(id: String) = repository.findById(id)

  fun stream(id: String): Flux<UserEvent> =
    Flux.generate { sink: SynchronousSink<UserEvent> -> sink.next(UserEvent(id)) }
        .delayElements(Duration.ofSeconds(1))

}

@Configuration
class WebFluxRoutesConfig(val service: UserService) {

  @Bean
  fun routes() = router {
    GET("/", { ok().body(service.all(), User::class.java) })
    GET("/{id}", { ok().body(service.one(it.pathVariable("id")), User::class.java) })
    GET("/{id}/events", { ok().contentType(TEXT_EVENT_STREAM).body(service.stream(it.pathVariable("id")), UserEvent::class.java) })
  }
}

@SpringBootApplication
class UserServiceApplication {

  @Bean
  fun runner(repository: UserRepository) = ApplicationRunner {

    val savePublisher = Flux.just("Administrator", "Useless")
        .map { User(name = it) }
        .flatMap { repository.save(it) }

    repository
        .deleteAll()
        .thenMany(savePublisher)
        .thenMany(repository.findAll())
        .subscribe({ println(it) })
  }
}

fun main(args: Array<String>) {
  SpringApplication.run(UserServiceApplication::class.java, *args)
}
