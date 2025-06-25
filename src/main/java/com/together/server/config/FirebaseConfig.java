package com.together.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.base64:}")
    private String firebaseConfigBase64;

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                InputStream serviceAccount = getFirebaseCredentialsStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);

                System.out.println("Firebase 초기화 성공");
            } catch (Exception e) {
                System.err.println("Firebase 초기화 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private InputStream getFirebaseCredentialsStream() throws IOException {
        // 운영환경 (Render 등)에서는 환경변수 사용
        if (firebaseConfigBase64 != null && !firebaseConfigBase64.isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
            return new ByteArrayInputStream(decodedBytes);
        }

        // 로컬 개발환경에서는 파일 사용
        InputStream localFile = getClass().getResourceAsStream("/firebase/firebase-adminsdk.json");
        if (localFile != null) {
            return localFile;
        }

        throw new IOException("Firebase 설정을 찾을 수 없습니다. 환경변수 FIREBASE_CONFIG_BASE64 또는 로컬 파일을 확인해주세요.");
    }
}