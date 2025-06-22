package com.karaoke.karaoke

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Tag("integration")
@SpringBootTest
class KaraokeSearchServiceIntegrationTest {

    @Autowired
    private lateinit var karaokeSearchService: KaraokeSearchService

    @Test
    fun `search - TJ미디어에서 '아이유' 검색 시 결과 반환`() {
        // Arrange
        val provider = "TJ"
        val keyword = "아이유"

        // Act
        val songs = karaokeSearchService.search(provider, keyword)

        // Assert
        assertNotNull(songs, "노래 목록은 null이 아니어야 합니다.")
        assertTrue(songs.isNotEmpty(), "키워드 '아이유'(가수)에 대한 검색 결과가 있어야 합니다.")

        songs.forEach { song ->
            assertFalse(song.songId.isBlank(), "곡 ID는 비어있지 않아야 합니다: ${song.title}")
            assertFalse(song.title.isBlank(), "곡 제목은 비어있지 않아야 합니다: ${song.songId}")
            assertTrue(
                song.singer.contains("IU") || song.singer.contains("아이유"),
                "가수 정보에 'IU' 또는 '아이유'가 포함되어야 합니다: ${song.singer}"
            )
        }
    }

    @Test
    fun `search - TJ미디어에서 존재하지 않는 가수로 검색 시 빈 목록 반환`() {
        // Arrange
        val provider = "TJ"
        val keyword = "asdfqwerzxcvnonexistentsinger12345"

        // Act
        val songs = karaokeSearchService.search(provider, keyword)

        // Assert
        assertNotNull(songs, "노래 목록은 null이 아니어야 합니다.")
        assertTrue(songs.isEmpty(), "존재하지 않는 가수명 키워드에 대한 검색 결과는 비어 있어야 합니다.")
    }

    @Test
    fun `search - KY미디어에서 '아이유' 검색 시 결과 반환`() {
        // Arrange
        val provider = "KY"
        val keyword = "아이유"

        // Act
        val songs = karaokeSearchService.search(provider, keyword)

        // Assert
        assertNotNull(songs, "노래 목록은 null이 아니어야 합니다.")
        assertTrue(songs.isNotEmpty(), "키워드 '아이유'(가수)에 대한 검색 결과가 있어야 합니다.")

        songs.forEach { song ->
            assertFalse(song.songId.isBlank(), "곡 ID는 비어있지 않아야 합니다: ${song.title}")
            assertFalse(song.title.isBlank(), "곡 제목은 비어있지 않아야 합니다: ${song.songId}")
            // 금영의 경우 가수 정보에 '아이유' 또는 'IU'가 포함될 수 있습니다.
            assertTrue(
                song.singer.contains("아이유") || song.singer.contains("IU"),
                "가수 정보에 '아이유' 또는 'IU'가 포함되어야 합니다: ${song.singer}"
            )
        }
    }

    @Test
    fun `search - KY미디어에서 존재하지 않는 가수로 검색 시 빈 목록 반환`() {
        // Arrange
        val provider = "KY"
        val keyword = "nonexistentkyartist12345" // 금영에서 검색되지 않을 만한 고유한 키워드

        // Act
        val songs = karaokeSearchService.search(provider, keyword)

        // Assert
        assertNotNull(songs, "노래 목록은 null이 아니어야 합니다.")
        assertTrue(songs.isEmpty(), "존재하지 않는 가수명 키워드에 대한 검색 결과는 비어 있어야 합니다.")
    }

    @Test
    fun `search - 지원하지 않는 provider로 검색 시 IllegalArgumentException 발생`() {
        // Arrange
        val provider = "INVALID_BRAND"
        val keyword = "아무노래"

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            karaokeSearchService.search(provider, keyword)
        }
        assertEquals("지원하지 않는 노래방 브랜드입니다: $provider", exception.message)
    }
}
