package com.thinkeep.global.config;

import io.github.cdimascio.dotenv.Dotenv;

public class OpenAiConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")  // 이 줄 추가해주는 게 좋음 (루트 명시)
            .ignoreIfMissing()
            .load();

    public static String getOpenAiKey() {
        String key = dotenv.get("OPENAI_API_KEY");  // key 변수 선언
        System.out.println("🔍 불러온 API 키: " + key);  // 콘솔 확인용
        return key;
    }
}

