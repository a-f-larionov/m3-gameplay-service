package m3.gameplay.services.impl;

import lombok.RequiredArgsConstructor;
import m3.gameplay.dto.rs.DoOrderChangeAnswerRsDto;
import m3.gameplay.dto.vk.rs.VKResponseDoOrderErrorRsDto;
import m3.gameplay.dto.vk.rs.VKResponseDoOrderSuccessRsDto;
import m3.gameplay.mappers.PaymentMapper;
import m3.gameplay.services.PaymentService;
import m3.gameplay.services.StuffService;
import m3.lib.dto.ProductDto;
import m3.lib.entities.PaymentEntity;
import m3.lib.enums.ClientLogLevels;
import m3.lib.enums.SocNetType;
import m3.lib.kafka.sender.CommonSender;
import m3.lib.repositories.PaymentRepository;
import m3.lib.repositories.UserRepository;
import m3.lib.store.ShopStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.lang.String.format;
import static m3.lib.enums.StatisticEnum.ID_BUY_VK_MONEY;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    //@todo thread safe counter!
    private static Long lastTid = 0L;

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StuffService stuffService;
    private final CommonSender commonSender;

    private static final VKResponseDoOrderErrorRsDto vkErrorCommon = VKResponseDoOrderErrorRsDto
            .builder()
            .errorCode(1L)
            .errorMsg("общая ошибка")
            .crtitcal(false)
            .build();

    private static final VKResponseDoOrderErrorRsDto vkErrorItemPriceNotFound = VKResponseDoOrderErrorRsDto
            .builder()
            .errorCode(1L)
            .errorMsg("нет такого товара")
            .crtitcal(true)
            .build();

    private static final VKResponseDoOrderErrorRsDto vkErrorSign = VKResponseDoOrderErrorRsDto
            .builder()
            .errorCode(10L)
            .errorMsg("несовпадение вычисленной и переданной подписи.")
            .crtitcal(true)
            .build();

    @Override
    public DoOrderChangeAnswerRsDto doOrderChange(Long tid, Long socNetUserId, Long extOrderId, Long itemPrice, SocNetType socNetType) {

        var userOptional = userRepository.findBySocNetTypeIdAndSocNetUserId(socNetType.getId(), socNetUserId);
        if (userOptional.isEmpty()) {
            commonSender.log(null, format("Order failed. Not user found.:\r\n" +
                            "tid: %s, userId: %s, orderId: %s, appOrderId: %s", tid, "-", extOrderId, "-"),
                    ClientLogLevels.INFO, true);
            return DoOrderChangeAnswerRsDto.builder()
                    .tid(tid)
                    .response(vkErrorCommon)
                    .build();
        }
        var user = userOptional.get();

        if (!ShopStore.goldProductByPriceExists(itemPrice)) {
            commonSender.log(null, format("Order failed. Not product found.:\r\n" +
                            "tid: %s, userId: %s, orderId: %s, appOrderId: %s", tid, user.getId(), extOrderId, "-"),
                    ClientLogLevels.INFO, true);
            return DoOrderChangeAnswerRsDto.builder()
                    .tid(tid)
                    .response(vkErrorItemPriceNotFound)
                    .build();
        }

        ProductDto product = ShopStore.getGoldProductByPrice(itemPrice);

        Optional<PaymentEntity> payment = paymentRepository.findByOrderId(extOrderId);
        if (payment.isPresent()) {
            commonSender.log(null, format("Order failed. Not user found.:\r\n" +
                            "tid: %s, userId: %s, orderId: %s, appOrderId: %s", tid, user.getId(), extOrderId, payment.get().getId()),
                    ClientLogLevels.INFO, true);
            return DoOrderChangeAnswerRsDto.builder()
                    .tid(tid)
                    .response(vkErrorCommon)
                    .build();
        }

        //@todo transaction analyzing. payment saved, but user stuff throw exception!
        PaymentEntity entity = paymentMapper.toEntity(System.currentTimeMillis() / 1000L, user.getId(), extOrderId, itemPrice);
        PaymentEntity newOrder = paymentRepository.save(entity);

        stuffService.giveAGold(user.getId(), product.getQuantity());

        stuffService.sendToUser(user.getId());

        commonSender.statistic(user.getId(), ID_BUY_VK_MONEY, newOrder.getId().toString(), itemPrice.toString());
        //@todo write to file necessary
        commonSender.log(user.getId(), format("Order successed:\r\n" +
                        "tid: %s, userId: %s, orderId: %s, appOrderId: %s", tid, user.getId(), extOrderId, newOrder.getId()),
                ClientLogLevels.INFO, true);

        return DoOrderChangeAnswerRsDto.builder()
                .tid(tid)
                .response(VKResponseDoOrderSuccessRsDto.builder()
                        .orderId(extOrderId)
                        .appOrderId(newOrder.getId())
                        .build())
                .build();
    }

    @Override
    public DoOrderChangeAnswerRsDto standaloneBuy(Long socNetUserId, Long orderId, Long itemPrice) {
        var tid = lastTid++;

        commonSender.log(null,
                format("Standalone pay request incoming: \r\n" +
                                "socNetUserId: %s orderId: %s itemPrice: %s",
                        socNetUserId, orderId, itemPrice),
                ClientLogLevels.INFO, true);

        return doOrderChange(tid, socNetUserId, orderId, itemPrice, SocNetType.Standalone);
    }

    @Override
    public DoOrderChangeAnswerRsDto vkBuy(){

//        /**
//         * @see https://vk.com/dev/payments_callbacks?f=3.%20%D0%9F%D1%80%D0%BE%D0%B2%D0%B5%D1%80%D0%BA%D0%B0%20%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D0%B8
//         * @param callback
//         * @param request
//         * @constructor
//         */
//        this.VKbuy = function (callback, request) {
//            let body = '', buyPrefix = 'vk_buy';
//            let tid;
//            tid = getOne();
//
//            Logs.log(buyPrefix + " REQUEST", Logs.LEVEL_TRACE, undefined, Logs.TYPE_VK_PAYMENTS);
//
//            /** Собираем тело */
//            request.on('data', function (data) {
//                body += data;
//            });
//
//            /** Завершили сбор тела */
//            request.on('end', function () {
//                onVKbuyReady(
//                        request.url,
//                        body,
//                        QUERYSTRING.decode(body),
//                        tid,
//                        callback,
//                        buyPrefix
//                );
//            });
//        };
        return  null;
    }

}