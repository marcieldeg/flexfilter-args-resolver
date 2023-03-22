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
	public static Pageable fromFilters(Map<String, String[]> filters) {
		final int page = filters.containsKey("page") ? Integer.parseInt(filters.remove("page")[0]) : 0;
		final int size = filters.containsKey("size") ? Integer.parseInt(filters.remove("size")[0]) : 0;

		final List<Order> orderList = new ArrayList<>();
		if (filters.containsKey("sort")) {
			for (String sortValue : filters.remove("sort")) {
				if (sortValue.contains(",")) {
					String[] sortParts = sortValue.split(",");
					orderList.add(new Order(Direction.valueOf(sortParts[1].toUpperCase()), sortParts[0]));
				} else {
					orderList.add(Order.asc(sortValue));
				}
			}
		}

		return orderList.isEmpty() ? PageRequest.of(page, size) : PageRequest.of(page, size, Sort.by(orderList));
	}
}
