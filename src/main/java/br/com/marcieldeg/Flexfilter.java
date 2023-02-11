package br.com.marcieldeg;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public class Flexfilter<T> {
	private Specification<T> specification;
	private Pageable pageable;

	public Flexfilter(Specification<T> specification, Pageable pageable) {
		this.specification = specification;
		this.pageable = pageable;
	}

	public static <U> Flexfilter<U> of(Specification<U> specification, Pageable pageable) {
		return new Flexfilter<U>(specification, pageable);
	}

	public Specification<T> getSpecification() {
		return specification;
	}

	public void setSpecification(Specification<T> specification) {
		this.specification = specification;
	}

	public Pageable getPageable() {
		return pageable;
	}

	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}
}
