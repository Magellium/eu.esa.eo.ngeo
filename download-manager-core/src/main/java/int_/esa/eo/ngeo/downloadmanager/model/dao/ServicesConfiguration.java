package int_.esa.eo.ngeo.downloadmanager.model.dao;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/* 
 * This class is used to handle transaction management. Further details required here.
 */

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {DataAccessRequestDaoImpl.class})
public class ServicesConfiguration {

}
