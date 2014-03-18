package int_.esa.eo.ngeo.downloadmanager.model.dao;

import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DataAccessRequestDaoImpl implements DataAccessRequestDao {

    private SessionFactory sessionFactory;

    public DataAccessRequestDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    public List<DataAccessRequest> loadVisibleDars() {
        Query query = sessionFactory.getCurrentSession().createQuery(String.format("FROM DataAccessRequest as dar WHERE visible = TRUE"));
        return query.list();
    }

    @Transactional(readOnly = true)
    @Override
    public DataAccessRequest searchForDar(DataAccessRequest searchDar) {
        Query query = sessionFactory.getCurrentSession().createQuery(String.format("FROM DataAccessRequest as dar WHERE darURL = :darURL OR darName = :darName"));
        query.setString("darURL", searchDar.getDarURL());
        query.setString("darName", searchDar.getDarName());

        return (DataAccessRequest) query.uniqueResult();
    }

    @Override
    public synchronized void updateDataAccessRequest(DataAccessRequest dataAccessRequest) {
        sessionFactory.getCurrentSession().saveOrUpdate(dataAccessRequest);
    }

    @Override
    public void updateProduct(Product product) {
        sessionFactory.getCurrentSession().saveOrUpdate(product);
    }
}