package int_.esa.eo.ngeo.dmtu.observer;

import int_.esa.eo.ngeo.dmtu.model.Product;

public interface ProductSubject {
	 void registerObserver(ProductObserver o);
	 void removeObserver(ProductObserver o);
	 void notifyObservers(Product product);
}
