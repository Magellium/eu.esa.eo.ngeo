package int_.esa.eo.ngeo.dmtu.model.dao;

import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.model.Product;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DataAccessRequestDAO {

	@Autowired
	private SessionFactory sessionFactory;
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<DataAccessRequest> loadVisibleDars() {
	    Query query = sessionFactory.getCurrentSession().createQuery(String.format("FROM DataAccessRequest as dar WHERE visible = TRUE"));
		List<DataAccessRequest> list = query.list();
		return list;
	}
	
	@Transactional(readOnly = true)
	public DataAccessRequest getDarByMonitoringUrl(String monitoringURL) {
		Query query = sessionFactory.getCurrentSession().createQuery(String.format("FROM DataAccessRequest as dar WHERE monitoringURL = :monitoringURL"));
		query.setString("monitoringURL", monitoringURL);
		
		Object uniqueResult = query.uniqueResult();
		return (DataAccessRequest) uniqueResult;

	}
	
	public void updateDataAccessRequest(DataAccessRequest dataAccessRequest) {
		sessionFactory.getCurrentSession().saveOrUpdate(dataAccessRequest);
	}

	public void updateProduct(Product product) {
		sessionFactory.getCurrentSession().saveOrUpdate(product);
	}
}