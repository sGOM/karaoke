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
class TJSearchProviderTest {

    @MockK
    private lateinit var mockRestClient: RestClient

    // relaxed = true 옵션은 logger의 모든 메소드(info, warn, error 등) 호출을 일일이 설정하지 않아도 되게 해줍니다.
    @MockK(relaxed = true)
    private lateinit var mockLogger: Logger

    @InjectMockKs
    private lateinit var tjSearchProvider: TJSearchProvider

    // --- HTML 응답 생성 헬퍼 함수 (변경 없음) ---
    private fun createHtmlResponseWithSongs(songs: List<Map<String, String>>): String {
        val songListItems = songs.joinToString("") { song ->
            """
            <li>
                <ul class="grid-container list ico">
                    <li class="grid-item center pos-type"><p class="count"><span class="num2">${song["songId"]}</span></p></li>
                    <li class="grid-item title3"><div class="flex-box"><p><span>${song["title"]}</span></p></div></li>
                    <li class="grid-item title4 singer"><p><span>${song["singer"]}</span></p></li>
                    <li class="grid-item title5"><p><span>${song["lyricist"]}</span></p></li>
                    <li class="grid-item title6"><p><span>${song["composer"]}</span></p></li>
                </ul>
            </li>
            """.trimIndent()
        }
        return """<html><body><ul class="chart-list-area music type-a type-b">$songListItems</ul></body></html>"""
    }

    private fun createHtmlResponseNoResults(): String {
        return """
            <html><body>
                <div class="music-search-list type2 pd-120">
                    <ul class="chart-list-area music type-a type-b">
                        <li><p class="no-date">검색 결과를 찾을 수 없습니다.</p></li>
                    </ul>
                </div>
            </body></html>
        """.trimIndent()
    }

    // --- RestClient 모의(Mock) 설정을 위한 헬퍼 함수 (변경 없음) ---
    private fun mockRestClientChain(responseBody: String?) {
        val requestHeadersUriSpec: RestClient.RequestHeadersUriSpec<*> = mockk()
        val requestHeadersSpec: RestClient.RequestHeadersSpec<*> = mockk()
        val responseSpec: RestClient.ResponseSpec = mockk()

        every { mockRestClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.body(String::class.java) } returns responseBody
    }

    private fun mockRestClientException(exception: RestClientException) {
        val requestHeadersUriSpec: RestClient.RequestHeadersUriSpec<*> = mockk()
        val requestHeadersSpec: RestClient.RequestHeadersSpec<*> = mockk()

        every { mockRestClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } throws exception
    }

    // --- 테스트 메소드들 (내용 변경 없음) ---
    @Test
    fun `search - 성공적으로 노래 목록을 파싱하여 반환한다`() {
        // Arrange
        val songData = listOf(
            mapOf(
                "songId" to "85842",
                "title" to "Love wins all",
                "singer" to "IU",
                "composer" to "서동환",
                "lyricist" to "아이유"
            ),
            mapOf("songId" to "33393", "title" to "좋은날", "singer" to "IU", "composer" to "이민수", "lyricist" to "김이나")
        )
        val htmlResponse = createHtmlResponseWithSongs(songData)
        mockRestClientChain(htmlResponse)

        // Act
        val result = tjSearchProvider.search("아이유")

        // Assert
        assertEquals(2, result.size)
        assertEquals(SongDto("85842", "Love wins all", "IU", "서동환", "아이유"), result[0])
    }

    @Test
    fun `search - 검색 결과가 없으면 빈 목록을 반환하고 info 로그를 남긴다`() {
        // Arrange
        mockRestClientChain(createHtmlResponseNoResults())

        // Act
        val result = tjSearchProvider.search("없는노래")

        // Assert
        assertTrue(result.isEmpty())
        verify { mockLogger.info("TJ미디어 검색 결과 없음 메시지 확인됨.") }
    }

    @Test
    fun `search - API 응답이 null이면 빈 목록을 반환하고 warn 로그를 남긴다`() {
        // Arrange
        mockRestClientChain(null)

        // Act
        val result = tjSearchProvider.search("아이유")

        // Assert
        assertTrue(result.isEmpty())
        verify { mockLogger.warn("TJ미디어 응답 본문이 비어있습니다.") }
    }

    @Test
    fun `search - RestClient 예외 발생 시 빈 목록을 반환하고 error 로그를 남긴다`() {
        // Arrange
        val keyword = "오류유발"
        val exception = RestClientException("KY 서버 접속 오류")
        mockRestClientException(exception)

        // Act
        val result = tjSearchProvider.search("오류유발")

        // Assert
        assertTrue(result.isEmpty())
        verify { mockLogger.error(any<String>(), eq(keyword), any(), any<RestClientException>()) }
    }
}
