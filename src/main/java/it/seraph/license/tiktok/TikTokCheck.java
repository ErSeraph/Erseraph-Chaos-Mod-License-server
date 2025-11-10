package it.seraph.license.tiktok;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import it.seraph.license.entities.Users;
import it.seraph.license.repositories.UserRepository;
import it.seraph.license.services.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@EnableScheduling
public class TikTokCheck {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TTListener listener;

    private final Map<String, LiveClient> liveClients = new ConcurrentHashMap<>();

    @PostConstruct
    private void avvia() {
        startAll();
    }

    @PreDestroy
    private void stop() {
        stopAll();
    }

    public void startAll() {
        List<Users> users = userRepository.findAllTikTokUsernames();
        users.forEach(u -> startClientIfNotExists(u.getUsername(), u.getLicenza()));
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void syncUsersFromDatabase() {
        List<Users> users = userRepository.findAllTikTokUsernames();
        users.forEach(u -> startClientIfNotExists(u.getUsername(), u.getLicenza()));

        liveClients.keySet().removeIf(hostId -> {
            Users utente = userRepository.findByLicenza(hostId).orElse(null);
            if (utente == null || utente.getBannato()) {
                LiveClient client = liveClients.get(hostId);
                System.out.println("🚫 " + (utente != null ? utente.getUsername() : hostId) + " bannato o rimosso.");
                if (client != null) client.disconnect();
                return true;
            }
            return false;
        });
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 10000)
    public void checkLiveStatus() {
        liveClients.forEach((hostId, client) -> {
            try {
                boolean isConnected = client.getRoomInfo() != null &&
                        client.getRoomInfo().getConnectionState() == ConnectionState.CONNECTED;

                if (!isConnected) {
                    System.out.println("❌ Client " + hostId + " non più in live. Disconnessione...");
                    client.disconnect();
                    liveClients.remove(hostId);
                }
            } catch (Exception e) {
                System.err.println("Errore controllo live per " + hostId + ": " + e.getMessage());
                client.disconnect();
                liveClients.remove(hostId);
            }
        });
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void syncNicknamesFromTikTok() {
        List<Users> users = userRepository.findAllTikTokUsernames();

        for (Users u : users) {
            if (u.getLicenza() == null || u.getLicenza().isBlank()) continue;

            if (liveClients.containsKey(u.getLicenza())) continue;

            try {
                String newUsername = getTikTokUsernameFromHostId(u.getLicenza());
                if (newUsername != null && !newUsername.equalsIgnoreCase(u.getUsername())) {
                    System.out.println("🔄 Username aggiornato: " + u.getUsername() + " ➜ " + newUsername);
                    userService.updateUsername(u.getLicenza(), newUsername);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Errore sync username per hostId " + u.getLicenza() + ": " + e.getMessage());
            }
        }
    }


    private void startClientIfNotExists(String username, String hostId) {
        if (hostId != null && liveClients.containsKey(hostId)) return;

        try {
            LiveClient client = TikTokLive.newClient(username)
                    .addListener(listener)
                    .buildAndConnect();

            String realHostId = String.valueOf(client.getRoomInfo().getHost().getId());
            String realUsername = client.getRoomInfo().getHost().getName();

            userService.assignTTID(realUsername, Long.parseLong(realHostId));
            liveClients.put(realHostId, client);

            System.out.println("✅ Connesso alla live di " + realUsername + " (hostId=" + realHostId + ")");
        } catch (Exception e) {
            //System.err.println("⚠️ Errore connettersi a " + username + ": " + e.getMessage());
        }
    }

    public void stopAll() {
        liveClients.values().forEach(LiveClient::disconnect);
        liveClients.clear();
    }

    private String getTikTokUsernameFromHostId(String hostId) throws Exception {
    	WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        try {
            String url = "https://www.tikvib.com/profile/" + hostId;
            driver.get(url);

            int maxTries = 20;
            int tries = 0;
            while (!driver.getTitle().contains("view and download") && tries < maxTries) {
                Thread.sleep(1000); 
                tries++;
            }

            String title = driver.getTitle();
            if (title.startsWith("@")) {
                String username = title.substring(1).split(" ")[0]; 
                return username;
            }
            return null;

        } catch (Exception e) {
            System.err.println("Errore fetch username per hostId " + hostId + ": " + e.getMessage());
            return null;
        } finally {
            driver.quit(); 
        }
    }

}
