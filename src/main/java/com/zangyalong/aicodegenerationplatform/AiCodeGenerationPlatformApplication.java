package com.zangyalong.aicodegenerationplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.zangyalong.aicodegenerationplatform.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class AiCodeGenerationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodeGenerationPlatformApplication.class, args);
	}

}
