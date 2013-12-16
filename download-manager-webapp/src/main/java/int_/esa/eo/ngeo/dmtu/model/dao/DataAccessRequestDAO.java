package int_.esa.eo.ngeo.dmtu.model.dao;

import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
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
	public DataAccessRequest getDarByMonitoringUrl(String darURL) {
		Query query = sessionFactory.getCurrentSession().createQuery(String.format("FROM DataAccessRequest as dar WHERE darURL = :darURL"));
		query.setString("darURL", darURL);
		
		Object uniqueResult = query.uniqueResult();
		return (DataAccessRequest) uniqueResult;

	}
	
	public synchronized void updateDataAccessRequest(DataAccessRequest dataAccessRequest) {
		sessionFactory.getCurrentSession().saveOrUpdate(dataAccessRequest);
	}

	public void updateProduct(Product product) {
		sessionFactory.getCurrentSession().saveOrUpdate(product);
	}
}