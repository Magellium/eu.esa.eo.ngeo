package int_.esa.eo.ngeo.dmtu.model.dao;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/* 
 * This class is used to handle transaction management. Further details required here.
 */

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {DataAccessRequestDAO.class})
public class ServicesConfiguration {

}
