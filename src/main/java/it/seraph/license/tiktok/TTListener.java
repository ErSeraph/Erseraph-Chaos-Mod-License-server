package it.seraph.license.tiktok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import it.seraph.license.services.UserService;

@Component
public class TTListener {

	@Autowired
	private UserService userService;
	
	@TikTokEventObserver
    public void onLike(LiveClient liveClient, TikTokLikeEvent event) {
    }

    @TikTokEventObserver
    public void onError(LiveClient liveClient, TikTokErrorEvent event) {
    }

    @TikTokEventObserver
    public void onComment(LiveClient liveClient, TikTokCommentEvent event) {
    }

    @TikTokEventObserver
    public void onGift(LiveClient liveClient, TikTokGiftEvent event) {
    	Long userId = event.getToUser().getId();
    	if (userService.isLoggable(userId)) {
    		Long liveId = event.getRoomId();
    		Long timestamp = event.getTimeStamp();
    		Long diamanti = Long.valueOf(event.getGift().getDiamondCost() * event.getCombo());
    		userService.saveLog(userId, liveId, timestamp, diamanti);
		}
    }
	
}
