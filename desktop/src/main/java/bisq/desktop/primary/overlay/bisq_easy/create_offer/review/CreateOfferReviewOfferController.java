/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.primary.overlay.bisq_easy.create_offer.review;

import bisq.application.DefaultApplicationService;
import bisq.chat.ChatService;
import bisq.chat.bisqeasy.channel.BisqEasyChatChannelSelectionService;
import bisq.chat.bisqeasy.channel.priv.BisqEasyPrivateTradeChatChannelService;
import bisq.chat.bisqeasy.channel.pub.BisqEasyPublicChatChannel;
import bisq.chat.bisqeasy.channel.pub.BisqEasyPublicChatChannelService;
import bisq.chat.bisqeasy.message.BisqEasyPublicChatMessage;
import bisq.common.currency.Market;
import bisq.common.monetary.Monetary;
import bisq.desktop.common.threading.UIThread;
import bisq.desktop.common.view.Controller;
import bisq.desktop.common.view.Navigation;
import bisq.desktop.common.view.NavigationTarget;
import bisq.desktop.primary.overlay.OverlayController;
import bisq.desktop.primary.overlay.bisq_easy.take_offer.TakeOfferController;
import bisq.i18n.Res;
import bisq.offer.Direction;
import bisq.offer.amount.AmountUtil;
import bisq.offer.amount.OfferAmountFormatter;
import bisq.offer.amount.spec.AmountSpec;
import bisq.offer.amount.spec.MinMaxAmountSpec;
import bisq.offer.bisq_easy.BisqEasyOffer;
import bisq.offer.price.spec.FixPriceSpec;
import bisq.offer.price.spec.FloatPriceSpec;
import bisq.offer.price.spec.PriceSpec;
import bisq.offer.settlement.SettlementFormatter;
import bisq.offer.settlement.SettlementUtil;
import bisq.oracle.marketprice.MarketPriceService;
import bisq.presentation.formatters.PercentageFormatter;
import bisq.presentation.formatters.QuoteFormatter;
import bisq.settings.SettingsService;
import bisq.support.MediationService;
import bisq.user.identity.UserIdentity;
import bisq.user.identity.UserIdentityService;
import bisq.user.profile.UserProfile;
import bisq.user.profile.UserProfileService;
import bisq.user.reputation.ReputationService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class CreateOfferReviewOfferController implements Controller {
    private final CreateOfferReviewOfferModel model;
    @Getter
    private final CreateOfferReviewOfferView view;
    private final ReputationService reputationService;
    private final Runnable resetHandler;
    private final SettingsService settingsService;
    private final UserIdentityService userIdentityService;
    private final BisqEasyPublicChatChannelService bisqEasyPublicChatChannelService;
    private final UserProfileService userProfileService;
    private final BisqEasyChatChannelSelectionService bisqEasyChatChannelSelectionService;
    private final Consumer<Boolean> mainButtonsVisibleHandler;
    private final BisqEasyPrivateTradeChatChannelService bisqEasyPrivateTradeChatChannelService;
    private final MediationService mediationService;
    private final ChatService chatService;
    private final MarketPriceService marketPriceService;

    public CreateOfferReviewOfferController(DefaultApplicationService applicationService,
                                            Consumer<Boolean> mainButtonsVisibleHandler,
                                            Runnable resetHandler) {
        this.mainButtonsVisibleHandler = mainButtonsVisibleHandler;
        chatService = applicationService.getChatService();
        bisqEasyPublicChatChannelService = chatService.getBisqEasyPublicChatChannelService();
        bisqEasyChatChannelSelectionService = chatService.getBisqEasyChatChannelSelectionService();
        reputationService = applicationService.getUserService().getReputationService();
        settingsService = applicationService.getSettingsService();
        userIdentityService = applicationService.getUserService().getUserIdentityService();
        userProfileService = applicationService.getUserService().getUserProfileService();
        bisqEasyPrivateTradeChatChannelService = chatService.getBisqEasyPrivateTradeChatChannelService();
        mediationService = applicationService.getSupportService().getMediationService();
        marketPriceService = applicationService.getOracleService().getMarketPriceService();
        this.resetHandler = resetHandler;

        model = new CreateOfferReviewOfferModel();
        view = new CreateOfferReviewOfferView(model, this);
    }

    public void setDirection(Direction direction) {
        model.setDirection(direction);
    }

    public void setMarket(Market market) {
        if (market != null) {
            model.setMarket(market);
        }
    }

    public void setSettlementMethodNames(List<String> settlementMethodNames) {
        if (settlementMethodNames != null) {
            model.setSettlementMethodNames(settlementMethodNames);
        }
    }

    public void setAmountSpec(AmountSpec amountSpec) {
        if (amountSpec != null) {
            model.setAmountSpec(amountSpec);
        }
    }

    public void setPriceSpec(PriceSpec priceSpec) {
        model.setPriceSpec(priceSpec);
    }

    public void setShowMatchingOffers(boolean showMatchingOffers) {
        model.setShowMatchingOffers(showMatchingOffers);
    }

    public void setIsMinAmountEnabled(boolean isMinAmountEnabled) {
        model.setMinAmountEnabled(isMinAmountEnabled);
    }

    public void reset() {
        model.reset();
    }

    @Override
    public void onActivate() {
        BisqEasyPublicChatChannel channel = bisqEasyPublicChatChannelService.findChannel(model.getMarket()).orElseThrow();
        model.setSelectedChannel(channel);

        model.getShowCreateOfferSuccess().set(false);

        UserIdentity userIdentity = checkNotNull(userIdentityService.getSelectedUserIdentity());


        String priceInfo;
        PriceSpec priceSpec = model.getPriceSpec();
        Direction direction = model.getDirection();
        if (direction.isSell()) {
            if (priceSpec instanceof FixPriceSpec) {
                FixPriceSpec fixPriceSpec = (FixPriceSpec) priceSpec;
                String price = QuoteFormatter.formatWithQuoteCode(fixPriceSpec.getQuote());
                priceInfo = Res.get("createOffer.bisqEasyOffer.chatMessage.fixPrice", price);
            } else if (priceSpec instanceof FloatPriceSpec) {
                FloatPriceSpec floatPriceSpec = (FloatPriceSpec) priceSpec;
                String percent = PercentageFormatter.formatToPercentWithSymbol(floatPriceSpec.getPercentage());
                priceInfo = Res.get("createOffer.bisqEasyOffer.chatMessage.floatPrice", percent);
            } else {
                priceInfo = Res.get("createOffer.bisqEasyOffer.chatMessage.marketPrice");
            }
        } else {
            priceInfo = "";
        }

        String directionString = Res.get(direction.name().toLowerCase()).toUpperCase();
        AmountSpec amountSpec = model.getAmountSpec();
        boolean hasAmountRange = amountSpec instanceof MinMaxAmountSpec;
        String amountString = OfferAmountFormatter.formatQuoteAmount(marketPriceService, amountSpec, model.getPriceSpec(), model.getMarket(), hasAmountRange, true);
        String chatMessageText = Res.get("createOffer.bisqEasyOffer.chatMessage",
                directionString,
                amountString,
                SettlementFormatter.asQuoteSideSettlementMethodsString(model.getSettlementMethodNames()),
                priceInfo);

        model.setMyOfferText(chatMessageText);

        BisqEasyOffer bisqEasyOffer = new BisqEasyOffer(
                userIdentity.getUserProfile().getNetworkId(),
                direction,
                model.getMarket(),
                amountSpec,
                priceSpec,
                new ArrayList<>(model.getSettlementMethodNames()),
                userIdentity.getUserProfile().getTerms(),
                settingsService.getRequiredTotalReputationScore().get(),
                chatMessageText);

        bisqEasyPublicChatChannelService.joinChannel(channel);
        bisqEasyChatChannelSelectionService.selectChannel(channel);

        BisqEasyPublicChatMessage myOfferMessage = new BisqEasyPublicChatMessage(channel.getId(),
                userIdentity.getUserProfile().getId(),
                Optional.of(bisqEasyOffer),
                Optional.empty(),
                Optional.empty(),
                new Date().getTime(),
                false);
        model.setMyOfferMessage(myOfferMessage);

        model.getMatchingOffers().setAll(channel.getChatMessages().stream()
                .map(chatMessage -> new CreateOfferReviewOfferView.ListItem(chatMessage.getBisqEasyOffer().orElseThrow(),
                        userProfileService,
                        reputationService,
                        marketPriceService))
                .filter(getTakeOfferPredicate())
                .sorted(Comparator.comparing(CreateOfferReviewOfferView.ListItem::getReputationScore))
                .limit(3)
                .collect(Collectors.toList()));

        model.getMatchingOffersVisible().set(model.isShowMatchingOffers() && !model.getMatchingOffers().isEmpty());
    }

    @Override
    public void onDeactivate() {
    }

    void onTakeOffer(CreateOfferReviewOfferView.ListItem listItem) {
        OverlayController.hide(() -> {
                    TakeOfferController.InitData initData = new TakeOfferController.InitData(listItem.getBisqEasyOffer(),
                            Optional.of(model.getAmountSpec()),
                            model.getSettlementMethodNames());
                    Navigation.navigateTo(NavigationTarget.TAKE_OFFER, initData);
                    resetHandler.run();
                }
        );
    }

    void onCreateOffer() {
        UserIdentity userIdentity = checkNotNull(userIdentityService.getSelectedUserIdentity());
        bisqEasyPublicChatChannelService.publishChatMessage(model.getMyOfferMessage(), userIdentity)
                .thenAccept(result -> UIThread.run(() -> {
                    model.getShowCreateOfferSuccess().set(true);
                    mainButtonsVisibleHandler.accept(false);
                }));
    }

    void onOpenBisqEasy() {
        close();
        Navigation.navigateTo(NavigationTarget.BISQ_EASY_CHAT);
    }

    private void close() {
        resetHandler.run();
        OverlayController.hide();
    }

    @SuppressWarnings("RedundantIfStatement")
    private Predicate<? super CreateOfferReviewOfferView.ListItem> getTakeOfferPredicate() {
        return item ->
        {
            try {
                if (item.getAuthorUserProfileId().isEmpty()) {
                    return false;
                }
                UserProfile authorUserProfile = item.getAuthorUserProfileId().get();
                if (userProfileService.isChatUserIgnored(authorUserProfile)) {
                    return false;
                }
                if (userIdentityService.getUserIdentities().stream()
                        .map(userIdentity -> userIdentity.getUserProfile().getId())
                        .anyMatch(userProfileId -> userProfileId.equals(authorUserProfile.getId()))) {
                    return false;
                }
                if (model.getMyOfferMessage() == null) {
                    return false;
                }
                if (model.getMyOfferMessage().getBisqEasyOffer().isEmpty()) {
                    return false;
                }

                BisqEasyOffer bisqEasyOffer = model.getMyOfferMessage().getBisqEasyOffer().get();
                BisqEasyOffer peersOffer = item.getBisqEasyOffer();

                if (peersOffer.getDirection().equals(bisqEasyOffer.getDirection())) {
                    return false;
                }

                if (!peersOffer.getMarket().equals(bisqEasyOffer.getMarket())) {
                    return false;
                }
                Optional<Monetary> myMinOrFixQuoteAmount = AmountUtil.findMinOrFixQuoteAmount(marketPriceService, bisqEasyOffer);
                Optional<Monetary> peersMaxOrFixQuoteAmount = AmountUtil.findMaxOrFixQuoteAmount(marketPriceService, peersOffer);
                if (myMinOrFixQuoteAmount.orElseThrow().getValue() > peersMaxOrFixQuoteAmount.orElseThrow().getValue()) {
                    return false;
                }

                Optional<Monetary> myMaxOrFixQuoteAmount = AmountUtil.findMaxOrFixQuoteAmount(marketPriceService, bisqEasyOffer);
                Optional<Monetary> peersMinOrFixQuoteAmount = AmountUtil.findMinOrFixQuoteAmount(marketPriceService, peersOffer);
                if (myMaxOrFixQuoteAmount.orElseThrow().getValue() < peersMinOrFixQuoteAmount.orElseThrow().getValue()) {
                    return false;
                }

                List<String> settlementMethods = SettlementUtil.getQuoteSideSettlementMethodNames(peersOffer);
                if (SettlementUtil.getQuoteSideSettlementMethodNames(bisqEasyOffer).stream().noneMatch(settlementMethods::contains)) {
                    return false;
                }

                //todo
           /* if (reputationService.getReputationScore(senderUserProfile).getTotalScore() < myChatOffer.getRequiredTotalReputationScore()) {
                return false;
            }*/

                return true;
            } catch (Throwable t) {
                log.error("Error at TakeOfferPredicate", t);
                return false;
            }
        };
    }
}