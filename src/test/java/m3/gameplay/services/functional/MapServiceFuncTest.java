package m3.gameplay.services.functional;

import m3.gameplay.BaseSpringBootTest;
import m3.gameplay.dto.rs.GotScoresRsDto;
import m3.gameplay.dto.rs.GotStuffRsDto;
import m3.gameplay.dto.rs.ScoreRsDto;
import m3.gameplay.services.MapService;
import m3.lib.enums.ObjectEnum;
import m3.lib.repositories.UserRepository;
import m3.lib.repositories.UserStuffRepository;
import m3.lib.store.ShopStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "logging.level.org.hibernate=DEBUG",
        //        "logging.level.logger.org.springframework.transaction=TRACE",
        "logging.level.logger.org.apache.kafka=FATAL",
        "logging.level.org.springframework.orm.jpa=DEBUG",
        "logging.level.org.springframework.transaction=DEBUG",
        "spring.jpa.show-sql=true"
})
public class MapServiceFuncTest extends BaseSpringBootTest {

    @Autowired
    MapService mapService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserStuffRepository userStuffRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void getScores() {
        // given
        Long userId = 100L;
        List<Long> uids = List.of(1001L, 1002L, 1003L);
        List<Long> pids = List.of(1101L, 1102L, 1103L);

        var expectedRs = GotScoresRsDto.builder()
                .userId(userId)
                .rows(List.of(
                        createScoreRsDto(1001L, 1101L, 1201L),
                        createScoreRsDto(1002L, 1102L, 1202L),
                        createScoreRsDto(1003L, 1103L, 1203L)
                ))
                .build();

        deleteAllUsersPoints();
        jdbcTemplate.update("INSERT INTO users_points " +
                "(userId, pointId, score)" +
                " VALUES" +
                " (1001, 1101, 1201), " +
                " (1002, 1102, 1202), " +
                " (1003, 1103, 1203) "
        );

        // when
        GotScoresRsDto rs = mapService.getScores(userId, pids, uids);

        // then
        assertThat(rs).isEqualTo(expectedRs);
    }

    @Test
    void getUserStuffCreateNew() {
        // given
        Long userId = 101L;
        var expectedRs = buildGotStuffRSDto(userId, 500L, 2L, 1L, 1L);

        jdbcTemplate.update("DELETE FROM users_stuff WHERE userId = ?", userId);

        // when
        GotStuffRsDto factRs = mapService.getUserStuffRsDto(userId);

        // then
        assertThat(factRs).isEqualTo(expectedRs);
        assertDbUserStuff(userId, 500L, 2L, 1L, 1L);
    }

    @Test
    void getUserStuff() {
        // given
        Long userId = 102L;
        var expectedRs = buildGotStuffRSDto(userId, 1000L, 10L, 20L, 30L);
        setUserStuff(userId, 1000L, 10L, 20L, 30L);

        // when
        GotStuffRsDto factRs = mapService.getUserStuffRsDto(userId);

        // then
        assertThat(factRs).isEqualTo(expectedRs);
        assertDbUserStuff(userId, 1000L, 10L, 20L, 30L);
    }

    @Test
    void spendCoinsForTurns() {
        // given
        Long userId = 103L;
        var expectedRs = buildGotStuffRSDto(userId, 1000L - ShopStore.turnsUp.getPriceGold(), 10L, 20L, 30L);
        setUserStuff(userId, 1000L, 10L, 20L, 30L);

        // when
        GotStuffRsDto actualRs = mapService.spendCoinsForTurns(userId);

        // then
        assertThat(actualRs).isEqualTo(expectedRs);
        assertDbUserStuff(userId, 1000L - ShopStore.turnsUp.getPriceGold(), 10L, 20L, 30L);
    }

    @Test
    void buyProduct() {
        // given
        Long userId = 104L;
        setUserStuff(userId, 5000L, 10L, 20L, 30L);
        var expectedRs = buildGotStuffRSDto(userId, 4000L, 16L, 20L, 30L);

        // when
        GotStuffRsDto actualRs = mapService.buyProduct(userId, 1L, ObjectEnum.STUFF_HUMMER);

        // then
        assertThat(actualRs).isEqualTo(expectedRs);
        assertDbUserStuff(userId, 4000L, 16L, 20L, 30L);
    }

    @Test
    void spendProduct() {
        // given
        Long userId = 105L;
        setUserStuff(userId, 5000L, 10L, 20L, 30L);
        var expectedRs = buildGotStuffRSDto(userId, 5000L, 9L, 20L, 30L);

        // when
        GotStuffRsDto actualRs = mapService.spendProduct(userId, ObjectEnum.STUFF_HUMMER);

        // then
        assertThat(actualRs).isEqualTo(expectedRs);
        assertDbUserStuff(userId, 5000L, 9L, 20L,30L);
    }

    private void setUserStuff(Long userId, Long gold, Long hummer, Long lightning, Long shuffle) {
        jdbcTemplate.update("DELETE FROM users_stuff WHERE userId = ?", userId);
        jdbcTemplate.update("INSERT INTO users_stuff (userId, goldQty, hummerQty, lightningQty, shuffleQty) " +
                "VALUE (?, ? ,? ,? ,?)", userId, gold, hummer, lightning, shuffle);
    }

    @Test
    @Disabled
    void existsMap() {
        // given

        // when

        // then
    }

    @Test
    @Disabled
    void getById() {
        // given

        // when

        // then
    }

    @Test
    @Disabled
    void getMapInfo() {
        // given

        // when

        // then
    }

    @Test
    @Disabled
    void gotPointTopScore() {
    }

    @Test
    @Disabled
    void onFinishWithNoUserExists() {

    }

    @Test
    @Disabled
    void onFinishWithNoChest() {
    }

    @Test
    @Disabled
    void onFinishWithPrizes() {
    }

    private void deleteAllUsersPoints() {
        jdbcTemplate.update("DELETE FROM users_points WHERE userId > 0");
    }

    private ScoreRsDto createScoreRsDto(long userId, long pointId, long score) {
        return ScoreRsDto.builder().userId(userId).pointId(pointId).score(score).build();
    }

    private GotStuffRsDto buildGotStuffRSDto(Long userId, Long gold, Long hummer, Long lightning, Long shuffle) {
        return GotStuffRsDto.builder()
                .userId(userId)
                .goldQty(gold)
                .hummerQty(hummer)
                .lightningQty(lightning)
                .shuffleQty(shuffle)
                .build();
    }

    private void assertDbUserStuff(Long userId, Long gold, Long hummer, Long lightning, Long shuffle) {
        Map<String, Object> dbState = jdbcTemplate.queryForMap("SELECT * FROM users_stuff WHERE userId = ?", userId);
        assertThat(dbState.get("userId")).isEqualTo(userId);
        assertThat(dbState.get("goldQty")).isEqualTo(gold);
        assertThat(dbState.get("hummerQty")).isEqualTo(hummer);
        assertThat(dbState.get("lightningQty")).isEqualTo(lightning);
        assertThat(dbState.get("shuffleQty")).isEqualTo(shuffle);
    }

}