package com.karaoke.karaoke.provider

import com.karaoke.karaoke.SongDto
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.util.function.Function

@ExtendWith(MockKExtension::class)
class KYSearchProviderTest {

    @MockK
    private lateinit var kyRestClient: RestClient

    @MockK(relaxed = true)
    private lateinit var mockLogger: Logger

    @InjectMockKs
    private lateinit var kySearchProvider: KYSearchProvider

    // --- HTML 응답 생성을 위한 헬퍼 함수 ---
    private fun createValidHtmlResponse(songs: List<Map<String, String>>): String {
        val songListItems = songs.joinToString("") { song ->
            """
            <ul class="search_chart_list clear">
                <input type="checkbox" name="songseq[]" value="${song["songId"]}">
                <li class="search_chart_num">${song["songId"]}</li>
                <li class="search_chart_tit clear"><span class="tit">${song["title"]}</span></li>
                <li class="search_chart_sng">${song["singer"]}</li>
                <li class="search_chart_cmp">${song["composer"]}</li>
                <li class="search_chart_wrt">${song["lyricist"]}</li>
            </ul>
            """.trimIndent()
        }
        return """<html><body><div class="search_daily_chart_wrap">$songListItems</div></body></html>"""
    }

    private fun createNoResultHtmlResponse(): String {
        return """<html><body><div class="search_result_empty"><p>검색된 결과가 없습니다.</p></div></body></html>"""
    }

    // --- RestClient 모의(Mock) 설정을 위한 헬퍼 함수 ---
    private fun mockRestClientChain(responseBody: String?) {
        val requestHeadersUriSpec: RestClient.RequestHeadersUriSpec<*> = mockk()
        val requestHeadersSpec: RestClient.RequestHeadersSpec<*> = mockk()
        val responseSpec: RestClient.ResponseSpec = mockk()

        every { kyRestClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(String::class.java) } returns responseBody
    }

    private fun mockRestClientException(exception: RestClientException) {
        val requestHeadersUriSpec: RestClient.RequestHeadersUriSpec<*> = mockk()
        val requestHeadersSpec: RestClient.RequestHeadersSpec<*> = mockk()

        every { kyRestClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws exception
    }

    // --- 테스트 메소드 ---
    @Test
    fun `search - 성공적으로 노래 목록을 파싱하여 반환한다`() {
        // Arrange
        val songData = listOf(
            mapOf(
                "songId" to "56478",
                "title" to "10월 4일",
                "singer" to "아이유(IU)",
                "composer" to "서태지",
                "lyricist" to "서태지"
            ),
            mapOf(
                "songId" to "48447",
                "title" to "가을 아침",
                "singer" to "아이유(IU)",
                "composer" to "이병우",
                "lyricist" to "이병우"
            )
        )
        val htmlResponse = createValidHtmlResponse(songData)
        mockRestClientChain(htmlResponse)

        // Act
        val result = kySearchProvider.search("아이유")

        // Assert
        assertEquals(2, result.size)
        assertEquals(SongDto("56478", "10월 4일", "아이유(IU)", "서태지", "서태지"), result[0])
        assertEquals(SongDto("48447", "가을 아침", "아이유(IU)", "이병우", "이병우"), result[1])
    }

    @Test
    fun `search - 검색 결과가 없으면 빈 목록을 반환하고 info 로그를 남긴다`() {
        // Arrange
        mockRestClientChain(createNoResultHtmlResponse())

        // Act
        val result = kySearchProvider.search("없는노래")

        // Assert
        assertTrue(result.isEmpty())
        verify { mockLogger.info("금영(KY) 검색 결과 없음 메시지 확인됨.") }
    }

    @Test
    fun `search - API 응답이 null이면 빈 목록을 반환하고 warn 로그를 남긴다`() {
        // Arrange
        mockRestClientChain(null)

        // Act
        val result = kySearchProvider.search("아이유")

        // Assert
        assertTrue(result.isEmpty())
        verify { mockLogger.warn("금영(KY) 응답 본문이 비어있습니다.") }
    }

    @Test
    fun `search - RestClient 예외 발생 시 빈 목록을 반환하고 error 로그를 남긴다`() {
        // Arrange
        val keyword = "오류유발"
        val exception = RestClientException("KY 서버 접속 오류")
        mockRestClientException(exception)

        // Act
        val result = kySearchProvider.search(keyword)

        // Assert
        assertTrue(result.isEmpty())
        verify { mockLogger.error(any<String>(), eq(keyword), any(), any<RestClientException>()) }
    }
}
