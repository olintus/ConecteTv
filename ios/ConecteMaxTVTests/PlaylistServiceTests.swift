import XCTest
@testable import ConecteMaxTV

final class PlaylistServiceTests: XCTestCase {
    func testParsesAbsoluteAndRelativeChannels() throws {
        let playlist = """
        #EXTM3U
        #EXTINF:-1 tvg-id="record" tvg-name="Fallback" tvg-logo="logos/record.png",Record News
        http://stream.example.com/record/index.m3u8
        #EXTINF:-1 tvg-id="local" tvg-name="Canal Local",Canal Local
        streams/local.m3u8
        """

        let channels = PlaylistService().parse(
            playlist,
            relativeTo: try XCTUnwrap(URL(string: "http://example.com/hls/playlist.m3u"))
        )

        XCTAssertEqual(channels.count, 2)
        XCTAssertEqual(channels[0].name, "Record News")
        XCTAssertEqual(channels[0].logoURL?.absoluteString, "http://example.com/hls/logos/record.png")
        XCTAssertEqual(channels[1].streamURL.absoluteString, "http://example.com/hls/streams/local.m3u8")
    }

    func testCommaInsideQuotedAttributeDoesNotBreakName() throws {
        let playlist = """
        #EXTINF:-1 group-title="Notícias, Brasil" tvg-name="News",Canal de Notícias
        news.m3u8
        """
        let channels = PlaylistService().parse(
            playlist,
            relativeTo: try XCTUnwrap(URL(string: "http://example.com/list.m3u"))
        )
        XCTAssertEqual(channels.first?.name, "Canal de Notícias")
    }

    func testLogoUsesPlaylistHostLikeAndroidImplementation() throws {
        let playlist = """
        #EXTINF:-1 tvg-logo="http://cdn.example.com/images/logo.png?size=2",Canal
        stream.m3u8
        """
        let channels = PlaylistService().parse(
            playlist,
            relativeTo: try XCTUnwrap(URL(string: "http://138.0.212.26/hls/playlist.m3u"))
        )
        XCTAssertEqual(
            channels.first?.logoURL?.absoluteString,
            "http://138.0.212.26/images/logo.png?size=2"
        )
    }
}
