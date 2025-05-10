package me.abouabra.zovo.utils;

import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class AvatarGenerator {
    private final AvatarStorageService avatarStorageService;
    private final RestTemplate restTemplate;
    private static final String DICEBEAR_ACCOUNT_URL = "https://api.dicebear.com/9.x/fun-emoji/png" +
                    "?seed=%s&backgroundType=gradientLinear,solid" +
                    "&eyes=closed,closed2,cute,glasses,love,pissed,plain,sad,shades,sleepClose,stars,wink,wink2" +
                    "&mouth=cute,kissHeart,lilSmile,plain,shy,smileLol,smileTeeth,tongueOut,wideSmile";
    private static final String DICEBEAR_GROUP_URL = "https://api.dicebear.com/9.x/icons/png?seed=%s&backgroundType=gradientLinear,solid";

    public AvatarGenerator(AvatarStorageService avatarStorageService) {
        this.avatarStorageService = avatarStorageService;
        this.restTemplate = new RestTemplate();
    }

    public InputStream fetchAvatarStream(String seed, boolean isForGroup) throws IOException {
        String url = String.format(isForGroup ? DICEBEAR_GROUP_URL : DICEBEAR_ACCOUNT_URL, seed);

        ResponseEntity<Resource> resp = restTemplate
                .exchange(url, HttpMethod.GET, null, Resource.class);
        if (resp.getStatusCode().isError())
            throw new IOException("Failed to fetch avatar stream from DiceBear");
        if (resp.getBody() == null)
            throw new IOException("Received null body from DiceBear");
        return resp.getBody().getInputStream();
    }

    public String createAvatar(String key, boolean isForGroup) {
        try {
            InputStream avatarStream = fetchAvatarStream(key, isForGroup);
            String completeKey = key + ".png";
            avatarStorageService.uploadAvatar(completeKey, avatarStream, avatarStream.available(), "image/png");
            return completeKey;

        } catch (Exception e) {
            log.error("Error fetching avatar for user: {}", key, e);
        }
        return null;
    }
}
