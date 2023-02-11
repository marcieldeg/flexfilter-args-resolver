package br.com.marcieldeg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class FlexfilterArgumentResolver implements HandlerMethodArgumentResolver {
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(Flexfilter.class);
	}

	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Map<String, String[]> params = new HashMap<>(webRequest.getParameterMap());

		// processa a parte de paginação
		Integer page = params.containsKey("page") ? Integer.parseInt(params.remove("page")[0]) : 0;
		Integer size = params.containsKey("size") ? Integer.parseInt(params.remove("size")[0]) : 0;

		List<Order> orderList = new ArrayList<>();
		if (params.containsKey("sort")) {
			for (String sortValue : params.remove("sort")) {
				if (sortValue.contains(",")) {
					String[] sortParts = sortValue.split(",");
					orderList.add(new Order(Direction.valueOf(sortParts[1].toUpperCase()), sortParts[0]));
				} else {
					orderList.add(Order.asc(sortValue));
				}
			}
		}
		Pageable pageable = orderList.isEmpty() ? PageRequest.of(page, size)
				: PageRequest.of(page, size, Sort.by(orderList));

		// processa a parte de especificação
		Specification<?> specification = FlexfilterSpecification.fromFilters(params);

		return new Flexfilter<>(specification, pageable);
	}
}
