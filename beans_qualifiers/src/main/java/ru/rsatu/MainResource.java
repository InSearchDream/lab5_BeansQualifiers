package ru.rsatu;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.impl.ArcContainerImpl;
import io.quarkus.vertx.http.runtime.devmode.Json;
import io.quarkus.vertx.http.runtime.devmode.Json.JsonArrayBuilder;
import io.quarkus.vertx.http.runtime.devmode.Json.JsonObjectBuilder;

@ApplicationScoped
@Path("/main")
@ManagedBean
public class MainResource {

	@Inject @Default
    private Generator8 gen8def;
    
    @Inject @Pass8
    private PasswordGenerator gen8;
    
    @Inject @Pass16
    private PasswordGenerator gen16;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String main() {

    	  return "Генерация пароля: \nInject Default: "+ gen8def.genPass()+"\nInject Pass8: "+ gen8.genPass()+"\nInject Pass16: "+ gen16.genPass()
    	  +"\n\nru.rsatu\n"+ getBeans("ru.rsatu")+ "\nru.rsatu.Generator8\n"+ getBeans("ru.rsatu.Generator8")+ "\nru.rsatu.Generator16\n"+ getBeans("ru.rsatu.Generator16");
    }
    
    // получение содержимого
    public String getBeans(String beanClass) {
    	JsonArrayBuilder JsonAr = Json.array();

        ArcContainerImpl container = ArcContainerImpl.instance();
        List<InjectableBean<?>> beans = container.getBeans();
        beans.addAll(container.getInterceptors());


        String kindParam = null;
        InjectableBean.Kind kind = kindParam != null ? InjectableBean.Kind.from(kindParam.toUpperCase()) : null;

        
        String scopeEndsWith = null;
        String beanClassStartsWith = beanClass;

        for (Iterator<InjectableBean<?>> it = beans.iterator(); it.hasNext();) {
            InjectableBean<?> injectableBean = it.next();
            if (kind != null && !kind.equals(injectableBean.getKind())) {
                it.remove();
            }
            if (scopeEndsWith != null && !injectableBean.getScope().getName().endsWith(scopeEndsWith)) {
                it.remove();
            }
            if (beanClassStartsWith != null
                    && !injectableBean.getBeanClass().getName().startsWith(beanClassStartsWith)) {
                it.remove();
            }
        }

        for (InjectableBean<?> injectableBean : beans) {
            JsonObjectBuilder bean = Json.object();
            bean.put("id", injectableBean.getIdentifier());
            bean.put("kind", injectableBean.getKind().toString());
            bean.put("generatedClass", injectableBean.getClass().getName());
            bean.put("beanClass", injectableBean.getBeanClass().getName());
            JsonArrayBuilder types = Json.array();
            for (Type beanType : injectableBean.getTypes()) {
                types.add(beanType.getTypeName());
            }
            bean.put("types", types);
            JsonArrayBuilder qualifiers = Json.array();
            for (Annotation qualifier : injectableBean.getQualifiers()) {
                if (qualifier.annotationType().equals(Any.class) || qualifier.annotationType().equals(Default.class)) {
                    qualifiers.add("@" + qualifier.annotationType().getSimpleName());
                } else {
                    qualifiers.add(qualifier.toString());
                }
            }
            bean.put("qualifiers", qualifiers);
            bean.put("scope", injectableBean.getScope().getName());

            if (injectableBean.getDeclaringBean() != null) {
                bean.put("declaringBean", injectableBean.getDeclaringBean().getIdentifier());
            }
            if (injectableBean.getName() != null) {
                bean.put("name", injectableBean.getName());
            }
            if (injectableBean.isAlternative()) {
                bean.put("alternativePriority", injectableBean.getAlternativePriority());
            }
            if (injectableBean.isDefaultBean()) {
                bean.put("isDefault", true);
            }
            JsonAr.add(bean);
        }
     return JsonAr.build();
    }
}
