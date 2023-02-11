# flexfilter-args-resolver

FlexfilterArgumentResolver is a tool to help create dynamic filters in Spring web applications. With it, you can use url parameters to create routes with complex and flexible filters, in a totally dynamic way. You can apply filters on the result by any field of the return model (except exotic data types). It also already includes pagination parameters.


Example:

```
http://localhost:8080/api/persons?page=0&size=10&sort=birthDate,DESC&name#START=Marciel&age#GE=35&age#LE=45&country#LIKE=BRASIL
```

### 1- Usage
i- Create a configuration class and register the module:
```
@Configuration
public class ResolverConfig extends WebMvcConfigurationSupport {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new FlexfilterArgumentResolver());
	}
}
```
ii- In your repository, create a method like this: 
```
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
NULL - is null<br/>
NNULL - is not null<br/>
LIKE - like (anywhere in the string -> `LIKE '%VALUE%'`)<br/>
NLIKE - not like (anywhere in the string -> `NOT LIKE '%VALUE%'`)<br/>
START - starts with (start of the string -> `LIKE 'VALUE%'`)<br/>
NSTART - not starts with (start of the string -> `NOT LIKE 'VALUE%'`)<br/>
END - ends with (end of the string -> `LIKE '%VALUE'`)<br/>
NEND - not ends with (end of the string -> `NOT LIKE '%VALUE'`)
