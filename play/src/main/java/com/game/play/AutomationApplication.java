package com.game.play;

import com.game.core.utils.SpringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


/**
 * 电商自动化程序
 */

@SpringBootApplication
@Import(SpringUtils.class)
@MapperScan("com.game.play.mapper")
public class AutomationApplication {


    public static void main(String[] args) {
        SpringApplication.run(AutomationApplication.class, args);
    }


}
