package com.wang.config;

import com.wang.model.DbConnectionInfo;
import com.wang.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Configuration(proxyBeanMethods = false)
public class WebConfig {

    /**
     * WebMvcConfigurer定制化SpringMVC的功能
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {

            /**
             * 自定义内容协商策略, 需要在配置中开启spring.mvc.contentnegotiation.favor-parameter=true
             *  这样在请求参数中带有format=wang时，就会使用application/x-wang这种类型作为资源类型，如：http://localhost:8080/user?format=wang
             * @param configurer
             */
//            @Override
//            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//                //Map<String, MediaType> mediaTypes
//                Map<String, MediaType> mediaTypes = new HashMap<>();
//                mediaTypes.put("json", MediaType.APPLICATION_JSON);
//                mediaTypes.put("xml",MediaType.APPLICATION_XML);
//                mediaTypes.put("wang",MediaType.parseMediaType("application/x-wang"));
//                //指定支持解析哪些参数对应的哪些媒体类型
//                ParameterContentNegotiationStrategy parameterStrategy = new ParameterContentNegotiationStrategy(mediaTypes);
////                parameterStrategy.setParameterName("ff");
//
//                HeaderContentNegotiationStrategy headeStrategy = new HeaderContentNegotiationStrategy();
//
//                configurer.strategies(Arrays.asList(parameterStrategy,headeStrategy));
//            }


            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                UrlPathHelper urlPathHelper = new UrlPathHelper();
                // 不移除；后面的内容。矩阵变量功能就可以生效
                urlPathHelper.setRemoveSemicolonContent(false);
                configurer.setUrlPathHelper(urlPathHelper);
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new Converter<String, User>() {
                    @Override
                    public User convert(String source) {
                        // zhangsan,22
                        if(!StringUtils.isEmpty(source)){
                            User user = new User();
                            String[] split = source.split(",");
                            user.setName(split[0]);
                            user.setAge(Integer.parseInt(split[1]));
                            return user;
                        }
                        return null;
                    }
                });
            }


            /**
             * 自定义的Converter
             * @param converters the list of configured converters to extend.
             */
            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                converters.add(new HttpMessageConverter<DbConnectionInfo>() {
                    @Override
                    public boolean canRead(Class<?> clazz, MediaType mediaType) {
                        return false;
                    }

                    @Override
                    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
                        return clazz.isAssignableFrom(User.class);
                    }

                    /**
                     * 服务器要统计所有MessageConverter都能写出哪些内容类型
                     *
                     * application/x-guigu
                     * @return
                     */
                    @Override
                    public List<MediaType> getSupportedMediaTypes() {
                        return MediaType.parseMediaTypes("application/x-wang");
                    }

                    @Override
                    public DbConnectionInfo read(Class<? extends DbConnectionInfo> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                        return null;
                    }

                    @Override
                    public void write(DbConnectionInfo dbConnectionInfo, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                        //自定义协议数据的写出
                        String data = dbConnectionInfo.toString();
                        //写出去
                        OutputStream body = outputMessage.getBody();
                        body.write(data.getBytes());
                    }
                });
            }
        };
    }
}
