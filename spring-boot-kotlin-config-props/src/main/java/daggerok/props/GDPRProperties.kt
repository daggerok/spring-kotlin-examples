package daggerok.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "gdpr")
data class GDPRProperties(val users: Map<String, User>? = mutableMapOf()) {

    data class User(var username: String? = null,
                    var password: String? = null,
                    val roles: List<String> = mutableListOf())
}
