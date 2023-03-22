package br.com.marcieldeg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public class FlexfilterPaginationHelper {
	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_SIZE = 50;

	public static Pageable fromFilters(Map<String, String[]> filters) {
		final int page = filters.containsKey("page") ? Integer.parseInt(filters.remove("page")[0]) : DEFAULT_PAGE;
		final int size = filters.containsKey("size") ? Integer.parseInt(filters.remove("size")[0]) : DEFAULT_SIZE;

		if (page < 1)
			throw new IllegalStateException("Invalid default page size. Must not be less than one");

		if (!filters.containsKey("sort"))
			return PageRequest.of(page, size);

		final List<Order> orderList = new ArrayList<>();
		for (String sortValue : filters.remove("sort")) {
			if (sortValue.contains(",")) {
				String[] sortParts = sortValue.split(",");
				orderList.add(new Order(Direction.valueOf(sortParts[1].toUpperCase()), sortParts[0]));
			} else {
				orderList.add(Order.asc(sortValue));
			}
		}

		return PageRequest.of(page, size, Sort.by(orderList));
	}
}
