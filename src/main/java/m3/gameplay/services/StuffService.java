package m3.gameplay.services;

public interface StuffService {
    void giveAHummer(Long userId, Long quantity);

    void giveALightning(Long userId, Long quantity);

    void giveAShuffle(Long userId, Long quantity);

    void giveAGold(Long userId, Long quantity);

    void spendAHummer(Long userId, Long quantity);

    void spendALightning(Long userId, Long quantity);

    void spendAShuffle(Long userId, Long quantity);

    void spendAGold(Long userId, Long quantity);
}