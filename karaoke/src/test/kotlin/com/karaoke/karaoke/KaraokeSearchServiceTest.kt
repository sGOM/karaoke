package com.karaoke.karaoke

import com.karaoke.karaoke.provider.SearchProvider
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class KaraokeSearchServiceTest {

    @MockK
    private lateinit var tjProvider: SearchProvider

    @MockK
    private lateinit var kyProvider: SearchProvider

    private lateinit var karaokeSearchService: KaraokeSearchService

    @BeforeEach
    fun setUp() {
        every { tjProvider.providerName } returns "TJ"
        every { kyProvider.providerName } returns "KY"

        karaokeSearchService = KaraokeSearchService(listOf(tjProvider, kyProvider))
    }

    @Test
    fun `search - 지원하는 provider 이름으로 검색 시 해당 provider의 search 메소드를 호출한다`() {
        // Arrange
        val providerName = "TJ"
        val keyword = "아이유"
        val expectedSongs = listOf(SongDto("12345", "좋은날", "아이유", "", ""))
        every { tjProvider.search(keyword) } returns expectedSongs

        // Act
        val actualSongs = karaokeSearchService.search(providerName, keyword)

        // Assert
        assertEquals(expectedSongs, actualSongs, "Provider가 반환한 결과와 일치해야 합니다.")

        // tjProvider.search가 정확히 1번 호출되었는지 확인
        verify(exactly = 1) { tjProvider.search(keyword) }
        // kyProvider.search는 호출되지 않았는지 확인
        verify(exactly = 0) { kyProvider.search(any()) }
    }

    @Test
    fun `search - 대소문자 구분 없이 provider를 찾아 검색을 수행한다`() {
        // Arrange
        val providerName = "tj" // 소문자
        val keyword = "아이유"
        every { tjProvider.search(keyword) } returns emptyList()

        // Act
        karaokeSearchService.search(providerName, keyword)

        // Assert
        verify(exactly = 1) { tjProvider.search(keyword) }
    }

    @Test
    fun `search - 지원하지 않는 provider 이름으로 검색 시 IllegalArgumentException을 던진다`() {
        // Arrange
        val providerName = "INVALID_PROVIDER"
        val keyword = "아무노래"

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            karaokeSearchService.search(providerName, keyword)
        }
        assertEquals("지원하지 않는 노래방 브랜드입니다: $providerName", exception.message)
    }
}
