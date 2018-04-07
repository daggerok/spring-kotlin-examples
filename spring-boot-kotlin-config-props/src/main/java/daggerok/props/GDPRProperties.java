/*
package daggerok.props;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

@Data
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "gdpr")
public class GDPRProperties {

  Map<String, User> users;

  @Data
  @Accessors(chain = true)
  static class User {
    String username, password;
    List<String> roles = newArrayList();
  }
}
*/
