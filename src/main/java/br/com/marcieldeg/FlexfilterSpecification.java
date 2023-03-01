package br.com.marcieldeg;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

class FlexfilterSpecification<T> implements Specification<T> {
	private static final long serialVersionUID = 3621985225091023586L;

	private List<Filter> filters = new ArrayList<Filter>();

	private FlexfilterSpecification() {
	}

	public static <U> FlexfilterSpecification<U> fromFilters(Map<String, String[]> filters) {
		FlexfilterSpecification<U> specification = new FlexfilterSpecification<U>();

		for (Entry<String, String[]> filter : filters.entrySet()) {
			final Pattern pattern = Pattern.compile("^(?<field>[A-Z0-9_]+)(!(?<op>"
					+ Stream.of(Operation.values()).map(Operation::name).collect(Collectors.joining("|")) + "))?$",
					Pattern.CASE_INSENSITIVE);
			final Matcher matcher = pattern.matcher(filter.getKey());
			if (!matcher.matches())
				throw new UnsupportedOperationException("Invalid key " + filter.getKey());

			for (String value : filter.getValue()) {
				final Filter f = new Filter(//
						matcher.group("field"), //
						matcher.group("op") == null ? Operation.EQ
								: Operation.valueOf(matcher.group("op").toUpperCase()), //
						value);
				specification.filters.add(f);
			}
		}

		return specification;
	}

	@SuppressWarnings("rawtypes")
	private Predicate createPredicate(Filter filter, Root<T> root,
			BiFunction<Path<? extends Comparable>, Comparable, Predicate> f) {
		Class<?> clazz = root.get(filter.field).getJavaType();
		if (clazz == String.class)
			return f.apply(root.<String>get(filter.field), filter.value);
		else if (clazz == Long.class)
			return f.apply(root.<Long>get(filter.field), Long.getLong(filter.value));
		else if (clazz == BigDecimal.class)
			return f.apply(root.<BigDecimal>get(filter.field), new BigDecimal(filter.value));
		else if (clazz == LocalDate.class)
			return f.apply(root.<LocalDate>get(filter.field), LocalDate.parse(filter.value));
		else if (clazz == LocalDateTime.class)
			return f.apply(root.<LocalDateTime>get(filter.field), LocalDateTime.parse(filter.value));

		throw new UnsupportedOperationException(
				filter.operation + " in datatype " + root.get(filter.field).getJavaType().getSimpleName());
	}

	private Predicate createPredicateIn(Filter filter, Root<T> root, CriteriaBuilder builder) {
		Class<?> clazz = root.get(filter.field).getJavaType();
		if (clazz == String.class) {
			In<String> in = builder.in(root.<String>get(filter.field));
			for (String v : filter.value.split(","))
				in.value(v);
			return in;
		} else if (clazz == Long.class) {
			In<Long> in = builder.in(root.<Long>get(filter.field));
			for (String v : filter.value.split(","))
				in.value(Long.parseLong(v));
			return in;
		} else if (clazz == BigDecimal.class) {
			In<BigDecimal> in = builder.in(root.<BigDecimal>get(filter.field));
			for (String v : filter.value.split(","))
				in.value(new BigDecimal(v));
			return in;
		}

		throw new UnsupportedOperationException(
				filter.operation + " in datatype " + root.get(filter.field).getJavaType().getSimpleName());
	}

	private Predicate createPredicateBw(Filter filter, Root<T> root, CriteriaBuilder builder) {
		String[] values = filter.value.split(",");

		if (values.length != 2)
			throw new UnsupportedOperationException(filter.operation + " expects two values");

		Class<?> clazz = root.get(filter.field).getJavaType();
		if (clazz == String.class)
			return builder.between(root.<String>get(filter.field), values[0], values[1]);
		else if (clazz == Long.class)
			return builder.between(root.<Long>get(filter.field), Long.parseLong(values[0]), Long.parseLong(values[1]));
		else if (clazz == BigDecimal.class)
			return builder.between(root.<BigDecimal>get(filter.field), new BigDecimal(values[0]),
					new BigDecimal(values[1]));
		else if (clazz == LocalDateTime.class)
			return builder.between(root.<LocalDateTime>get(filter.field), LocalDateTime.parse(values[0]),
					LocalDateTime.parse(values[1]));

		throw new UnsupportedOperationException(
				filter.operation + " in datatype " + root.get(filter.field).getJavaType().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();

		for (Filter filter : filters)
			switch (filter.operation) {
			case EQ:
				predicates.add(createPredicate(filter, root, builder::equal));
				break;
			case NE:
				predicates.add(createPredicate(filter, root, builder::notEqual));
				break;
			case GE:
				predicates.add(createPredicate(filter, root, builder::greaterThanOrEqualTo));
				break;
			case GT:
				predicates.add(createPredicate(filter, root, builder::greaterThan));
				break;
			case LE:
				predicates.add(createPredicate(filter, root, builder::lessThanOrEqualTo));
				break;
			case LT:
				predicates.add(createPredicate(filter, root, builder::lessThan));
				break;
			case NL:
				predicates.add(builder.isNull(root.get(filter.field)));
				break;
			case NNL:
				predicates.add(builder.isNotNull(root.get(filter.field)));
				break;
			case IN:
				predicates.add(createPredicateIn(filter, root, builder));
				break;
			case NIN:
				predicates.add(createPredicateIn(filter, root, builder).not());
				break;
			case BW:
				predicates.add(createPredicateBw(filter, root, builder));
				break;
			case NBW:
				predicates.add(createPredicateBw(filter, root, builder).not());
				break;
			case LK:
				if (root.get(filter.field).getJavaType() == String.class)
					predicates.add(builder.like(root.<String>get(filter.field), "%" + filter.value + "%"));
				else
					throw new UnsupportedOperationException(
							"LIKE in datatype " + root.get(filter.field).getJavaType().getSimpleName());
				break;
			case NLK:
				if (root.get(filter.field).getJavaType() == String.class)
					predicates.add(builder.notLike(root.<String>get(filter.field), "%" + filter.value + "%"));
				else
					throw new UnsupportedOperationException(
							"NLIKE in datatype " + root.get(filter.field).getJavaType().getSimpleName());
				break;
			case SW:
				if (root.get(filter.field).getJavaType() == String.class)
					predicates.add(builder.like(root.<String>get(filter.field), filter.value + "%"));
				else
					throw new UnsupportedOperationException(
							"START in datatype " + root.get(filter.field).getJavaType().getSimpleName());
				break;
			case NSW:
				if (root.get(filter.field).getJavaType() == String.class)
					predicates.add(builder.notLike(root.<String>get(filter.field), filter.value + "%"));
				else
					throw new UnsupportedOperationException(
							"NSTART in datatype " + root.get(filter.field).getJavaType().getSimpleName());
				break;
			case EW:
				if (root.get(filter.field).getJavaType() == String.class)
					predicates.add(builder.like(root.<String>get(filter.field), "%" + filter.value));
				else
					throw new UnsupportedOperationException(
							"END in datatype " + root.get(filter.field).getJavaType().getSimpleName());
				break;
			case NEW:
				if (root.get(filter.field).getJavaType() == String.class)
					predicates.add(builder.notLike(root.<String>get(filter.field), "%" + filter.value));
				else
					throw new UnsupportedOperationException(
							"NEND in datatype " + root.get(filter.field).getJavaType().getSimpleName());
				break;
			}

		return builder.and(predicates.stream().toArray(Predicate[]::new));
	}

	public static enum Operation {
		EQ, NE, GT, GE, LT, LE, IN, NIN, BW, NBW, NL, NNL, LK, NLK, SW, NSW, EW, NEW
	}

	public static class Filter {
		public final String field;
		public final Operation operation;
		public final String value;

		public Filter(String field, Operation operation, String value) {
			super();
			this.field = field;
			this.operation = operation;
			this.value = value;
		}
	}
}
