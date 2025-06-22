package com.karaoke.karaoke.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class LoggingConfig {

    /**
     * Logger를 주입받는 클래스에 맞춰 적절한 Logger 인스턴스를 생성하는 Bean.
     * @param injectionPoint Logger가 주입되는 지점의 정보 (클래스, 필드 등)
     * @return 해당 클래스에 맞는 Logger 인스턴스
     */
    @Bean
    @Scope("prototype") // Logger는 주입되는 곳마다 새로 생성되어야 하므로 prototype 스코프를 사용합니다.
    fun logger(injectionPoint: InjectionPoint): Logger {
        return LoggerFactory.getLogger(
            // 생성자 주입 시, 해당 생성자가 속한 클래스의 정보를 가져옵니다.
            injectionPoint.methodParameter?.containingClass
                ?: throw IllegalStateException("Cannot determine logger class")
        )
    }
}
