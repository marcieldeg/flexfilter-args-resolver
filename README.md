# flexfilter-args-resolver

FlexfilterArgumentResolver is a tool to help create dynamic filters in Spring web applications. With it, you can use url parameters to create routes with complex and flexible filters, in a totally dynamic way. You can apply filters on the result by any field of the return model (except exotic data types). It also already includes pagination parameters.


Example:

```
http://localhost:8080/api/persons?page=0&size=10&sort=birthDate,DESC&name!START=Marciel&age!GE=35&age!LE=45&country!LIKE=BRASIL
```

### 1- Usage
i- Create a configuration class and register the module:
```java
@Configuration
public class ResolverConfig extends WebMvcConfigurationSupport {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new FlexfilterArgumentResolver());
	}
}
```
ii- In your repository, create a method like this: 
```java
@Repository
public interface PersonRepository<PersonModel> extends JpaRepository<PersonModel, UUID>, JpaSpecificationExecutor<PersonModel> {
	//
	default Page<PersonModel> findList(Flexfilter<PersonModel> filters) {
		return findList(filters.getSpecification(), filters.getPageable());
	}
}
```
iii- Propagate the method to your service and your controller.


That's all.


### Predefined suffix modifiers:
EQ - equals (default)<br/>
NE - not equals<br/>
GT - greather than<br/>
GE - greather or equal<br/>
LT - lower than<br/>
LE - lower or equal<br/>
IN - in (`IN ('list', 'of', 'values')`). Use commas to separate values.<br/>
NIN - not in (`NOT IN ('list', 'of', 'values')`). Use commas to separate values.<br/>
NL - is null<br/>
NNL - is not null<br/>
LK - like (anywhere in the string -> `LIKE '%VALUE%'`)<br/>
NLK - not like (anywhere in the string -> `NOT LIKE '%VALUE%'`)<br/>
SW - starts with (start of the string -> `LIKE 'VALUE%'`)<br/>
NSW - not starts with (start of the string -> `NOT LIKE 'VALUE%'`)<br/>
EW - ends with (end of the string -> `LIKE '%VALUE'`)<br/>
NEW - not ends with (end of the string -> `NOT LIKE '%VALUE'`)

### Examples
1- 
```
http://localhost:8080/api/persons?name!SW=Marciel&age!BW=35,45&country!LK=BRASIL
```
Result SQL:
```sql
select *
  from persons
 where name like 'Marciel%'
   and age between 35 and 45
   and country like '%BRASIL%'
```

2- 
```
http://localhost:8080/api/cities?name=VITORIA&zipcode!NULL&country!NIN=BRASIL,ITALIA&population!GT=20000
```
Result SQL:
```sql
select *
  from cities
 where name = 'VITORIA'
   and zipcode is null
   and country not in ('BRASIL','ITALIA')
   and population > 20000
```
