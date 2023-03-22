package br.com.marcieldeg;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class FlexfilterArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(Flexfilter.class);
	}

	@Override
	public Flexfilter<?> resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		final Map<String, String[]> params = new HashMap<>(webRequest.getParameterMap());

		// processa a parte de paginação
		final Pageable pageable = FlexfilterPaginationHelper.fromFilters(params);

		// processa a parte de especificação
		final Specification<?> specification = FlexfilterSpecification.fromFilters(params);

		return new Flexfilter<>(specification, pageable);
	}
}
