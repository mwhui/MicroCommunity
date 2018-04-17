package com.java110.center;

import com.java110.event.center.init.CenterServiceStartInit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;


/**
 * spring boot 初始化启动类
 *
 * @version v0.1
 * @auther com.java110.wuxw
 * @mail 928255095@qq.com
 * @date 2016年8月6日
 * @tag
 */
@SpringBootApplication(scanBasePackages={"com.java110.service","com.java110.order","com.java110.core","com.java110.event"})
@EnableDiscoveryClient
//@EnableConfigurationProperties(EventProperties.class)
public class CenterServiceApplicationStart {

    public static void main(String[] args) throws Exception{
        ApplicationContext context = SpringApplication.run(CenterServiceApplicationStart.class, args);

        CenterServiceStartInit centerServiceStartInit = new CenterServiceStartInit();

        centerServiceStartInit.initSystemConfig(context);
    }
}