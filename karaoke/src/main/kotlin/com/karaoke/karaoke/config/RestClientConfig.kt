package com.karaoke.karaoke.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    /**
     * TJ미디어 검색을 위한 RestClient Bean.
     * Bean의 이름을 "tjRestClient"로 명시하여 다른 RestClient와 구분합니다.
     */
    @Bean("tjRestClient")
    fun tjRestClient(builder: RestClient.Builder): RestClient {
        return builder
            .baseUrl("https://www.tjmedia.com/song/accompaniment_search")
            .build()
    }

    /**
     * KY미디어 검색을 위한 RestClient Bean.
     * Bean의 이름을 "kyRestClient"로 명시하여 다른 RestClient와 구분합니다.
     */
    @Bean("kyRestClient")
    fun kyRestClient(builder: RestClient.Builder): RestClient {
        return builder
            .baseUrl("https://kysing.kr/search/")
            .build()
    }
}
